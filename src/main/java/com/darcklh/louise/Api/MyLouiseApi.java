package com.darcklh.louise.Api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.Louise.Role;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Service.*;
import com.darcklh.louise.Utils.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class MyLouiseApi implements ErrorController {
    private final Logger log = LoggerFactory.getLogger(MyLouiseApi.class);
    @Autowired
    GroupService groupService;
    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    CBIRService cbirService;

    /**
     * 返回帮助信息
     * @param message Message
     * @return JSONObject
     */
    @RequestMapping("louise/help")
    public JSONObject help(@RequestBody Message message) {
        String[] args = message.getRaw_message().split(" ");
        JSONObject returnJson = new JSONObject();
        int intPage = 1;
        String page = "1";
        if (args.length > 2)
            throw new ReplyException("过多的参数");
        if (args.length == 2)
            page = args[1];
        try {
            intPage = Integer.parseInt(page);
        } catch (NumberFormatException e) {
            throw new ReplyException("非法的参数格式");
        }
        if (intPage >= 3)
            throw new ReplyException("现在还没有那么多帮助页面");
        message.clear().image(LouiseConfig.LOUISE_HELP_PAGE + page + ".png").send();
        return returnJson;
    }

    /**
     * 封禁用户
     * @param message Message
     * @return JSONObject
     */
    @RequestMapping("louise/ban")
    public JSONObject banUser(@RequestBody JSONObject message) {
        JSONObject reply = new JSONObject();
        String admin = message.getString("user_id");
        if (!admin.equals(LouiseConfig.LOUISE_ADMIN_NUMBER)) {
            reply.put("reply", "管理员限定");
            return reply;
        }
        long user_id = Long.parseLong(message.getString("message").substring(5));
        reply.put("reply", userService.banUser(user_id));
        return reply;
    }

    /**
     * 注册新群组
     * @param message Message
     * @return JSONObject
     */
    @RequestMapping("louise/group_join")
    public JSONObject groupJoin(@RequestBody Message message) {

        Long group_id = message.getGroup_id();
        Group group = new Group();
        group.setGroup_id(group_id);
        //快速返回
        JSONObject returnJson = new JSONObject();

        //判断如果是私聊禁止注册
        if (group_id == -1) {
            returnJson.put("reply", "露易丝不支持私聊注册群组哦，请在群聊里使用吧");
            return returnJson;
        }

        returnJson.put("reply", groupService.add(group));
        return returnJson;
    }

    /**
     * 更新群组信息
     *
     * @param message Message
     * @return JSONObject
     */
    @RequestMapping("louise/group_update")
    public JSONObject groupUpdate(@RequestBody Message message) {
        long group_id = message.getGroup_id();
        Group group = new Group();
        group.setGroup_id(group_id);
        //快速返回
        JSONObject returnJson = new JSONObject();

        //判断如果是私聊禁止更新
        if (group_id == -1) {
            returnJson.put("reply", "露易丝不支持私聊更新群组哦，请在群聊里使用吧");
            return returnJson;
        }

        // TODO 需要处理管理员变动的上报事件，否则会造成数据库与 QQ 端数据不一致从而影响权限判断
        // 更新前校验管理员身份
        String group_admins = groupService.getGroupAdmin(group.getGroup_id());

        for (String s : group_admins.split(",")) {
            long admin = Long.parseLong(s);
            // 如果发言者是该群的管理员，那么允许更新群聊
            if (admin == message.getUser_id())
                break;
            else
                returnJson.put("reply", "露易丝只允许群管理员进行更新哦，请联系管理员吧");
            return returnJson;
        }

        returnJson.put("reply", groupService.update(group));
        return returnJson;
    }

    /**
     * 注册新用户
     *
     * @param message Message
     * @return JSONObject
     */
    @RequestMapping("louise/join")
    public JSONObject join(@RequestBody Message message) {
        long user_id = message.getUser_id();
        long group_id = message.getGroup_id();
        if (group_id == -1) {
            //判断如果是私聊禁止注册
            JSONObject returnJson = new JSONObject();
            //TODO)) go-cqhttp只能根据qq号和群号获取某个用户的信息 但是这会导致数据库中使用双主键 比较麻烦 后期解决一下这个问题
            returnJson.put("reply", "露易丝不支持私聊注册哦，\n请在群聊里使用吧");
            //快速返回
            return returnJson;
        }
        return userService.joinLouise(user_id, group_id);
    }


    /**
     * 内部实现的基于直方图信息的图片检索
     *
     * @param message JSONObject
     * @return JSONObject
     */
    @RequestMapping("louise/search")
    private JSONObject searchPicture(@RequestBody JSONObject message) {
        //返回值
        JSONObject returnJson = new JSONObject();
        //解析上传的信息 拿到图片URL还有一些相关参数
        String uri = message.getString("message");
        uri = uri.substring(uri.indexOf("url=") + 4, uri.length() - 1);
        //获取请求元数据信息

        URL url;
        String filePath = LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION + "/";
        String fileName = "Image_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + Tool.generateShortUuid() + "." + "jpg";
        String imageName = filePath + fileName;
        try {
            log.info("开始下载" + imageName + " 图片地址: " + uri);
            url = URI.create(uri).toURL();
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
            output.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            returnJson.put("reply", "下载图片失败了，请检查命令是否包含图片");
            return returnJson;
        } catch (IOException e) {
            log.info("下载图片" + imageName + "失败");
            returnJson.put("reply", "获取上传图片失败");
            return returnJson;
        }

        // 下载成功开始执行搜索任务
        try {
            returnJson.put("result", cbirService.compareImageCompress(imageName));
        } catch (Exception e) {
            returnJson.put("reply", "检索图片失败了");
            return returnJson;
        }
        StringBuilder hiList = new StringBuilder();
        JSONArray hiArray = returnJson.getJSONObject("result").getJSONArray("result_hiList");

        for (Object o : hiArray) {
            JSONObject jsonObj = (JSONObject) o;
            hiList.append("[CQ:image,file=http://127.0.0.1:8099/saito/image").append(jsonObj.getString("image_name")).append("]\n");
        }

        returnJson.put("reply", "搜索出来了，按准确度从高到低排列" +
                "\n" + hiList);

        return returnJson;
    }

    /**
     * 查询用户信息
     *
     * @param message Message
     * @return JSONObject
     */
    @RequestMapping("louise/myinfo")
    public JSONObject myInfo(@RequestBody Message message) {

        long user_id = message.getUser_id();
        JSONObject returnJson = new JSONObject();

        User user = userService.selectById(user_id);
        Role role = roleService.selectById(user.getRole_id());

        message.clear();
        message.setGroup_id(-1L);
        message.setMessage_type("privacy");
        String nickname = user.getNickname();
        Timestamp create_time = user.getCreate_time();
        int count_setu = user.getCount_setu();
        int count_upload = user.getCount_upload();

        String myInfos = nickname + "，你的个人信息" +
                "\n总共请求功能次数：" + count_setu +
                "\n总共上传文件次数：" + count_upload +
                "\n在露易丝这里注册的时间；" + create_time +
                "\n-----------DIVIDER LINE------------" +
                "\n你的权限级别：<" + role.getRole_name() + ">" +
                "\n剩余CREDIT：" + user.getCredit() +
                "\nCREDIT BUFF：" + user.getCredit_buff();
        message.text(myInfos).send();

        returnJson.put("reply", "[CQ:at,qq=" + user_id + "]已私聊发送你的个人信息");
        return returnJson;

    }
}
