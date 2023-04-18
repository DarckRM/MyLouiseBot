package com.darcklh.louise.Api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.Louise.Role;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Messages.Node;
import com.darcklh.louise.Model.Messages.OutMessage;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Service.*;
import com.darcklh.louise.Utils.PluginManager;
import com.darcklh.louise.Utils.ReplyExceptionHandler;
import com.darcklh.louise.Utils.UniqueGenerator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.darcklh.louise.Utils.isEmpty.isEmpty;

@Slf4j
@RestController
public class MyLouiseApi implements ErrorController {
    Logger logger = LoggerFactory.getLogger(MyLouiseApi.class);

    @Autowired
    private SendPictureApi sendPictureApi;

    @Autowired
    private R r;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CBIRService cbirService;

    /**
     * 用于接收各类请求 但不做任何处理
     */
    @RequestMapping("/louise/*")
    public String handleRequest() {
        return "不存在的 URL";
    }

    /**
     * 插件调用中心
     * @param pluginId
     * @return
     */
    @RequestMapping("/louise/invoke/{pluginId}")
    public JSONObject pluginsCenter(@PathVariable Integer pluginId, @RequestBody InMessage inMessage) {
        PluginInfo pluginInfo = PluginManager.pluginInfos.get(pluginId);
        Thread t = new Thread(() -> {
            pluginInfo.getPluginService().service(inMessage);
        }, UniqueGenerator.uniqueThreadName("PLG", "Plugin Invoke"));
        t.setUncaughtExceptionHandler(new ReplyExceptionHandler());
        t.start();
        return null;
    }

    /**
     * 刷新配置
     */
    @RequestMapping("/louise/show")
    public String refreshConfig() {
        return "API: " + LouiseConfig.BOT_LOUISE_CACHE_IMAGE;
    }

    /**
     * 返回帮助信息
     * @return
     */
    @RequestMapping("louise/help")
    public JSONObject help(@RequestBody InMessage inMessage) {
        Message msg = Message.build(inMessage);
        String[] args = inMessage.getMessage().split(" ");
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
        msg.image(LouiseConfig.LOUISE_HELP_PAGE + page + ".png")
                .send();
        return returnJson;
    }

    /**
     * 封禁用户
     * @param message
     * @return
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

    @RequestMapping("louise/config/{type}")
    public JSONObject modifyConfig(@RequestBody JSONObject message, @PathVariable Integer type) {
        JSONObject reply = new JSONObject();
        String admin = message.getString("user_id");
        if (!admin.equals(LouiseConfig.LOUISE_ADMIN_NUMBER)) {
            reply.put("reply", "管理员限定");
            return reply;
        }
        if (type == 0) {
            LouiseConfig.BOT_LOUISE_CACHE_IMAGE = "../../../../MyLouise/cache/images/";
            logger.info("切换至 本地开发 配置");
            reply.put("reply", "切换到本地开发环境");
        } else {
            LouiseConfig.BOT_LOUISE_CACHE_IMAGE = "../../MyLouise/cache/images/";
            logger.info("切换至 线上部署 配置");
            reply.put("reply", "切换到服务部署环境");
        }
        return reply;
    }

    /**
     * 查询当前系统配置
     * @return
     */
    @RequestMapping("louise/config")
    public JSONObject queryConfig() {
        JSONObject reply = new JSONObject();
        reply.put("reply", "开发中");
        return reply;
    }

    @RequestMapping("louise/test")
    public void testRequestCenter(@RequestBody InMessage inMessage) {
        log.info(inMessage.toString());
    }

    @RequestMapping("louise/meta")
    public JSONObject metaRequestCenter() {
        return null;
    }

    /**
     * 注册新群组
     * @return
     */
    @RequestMapping("louise/group_join")
    public JSONObject groupJoin(@RequestBody InMessage inMessage) {

        Long group_id = inMessage.getGroup_id();
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
     * @param inMessage
     * @return
     */
    @RequestMapping("louise/group_update")
    public JSONObject groupUpdate(@RequestBody InMessage inMessage) {
        long group_id = inMessage.getGroup_id();
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
            if(admin == inMessage.getUser_id())
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
     * @param inMessage
     * @return
     */
    @RequestMapping("louise/join")
    public JSONObject join(@RequestBody InMessage inMessage) {

        long user_id = inMessage.getUser_id();
        long group_id = inMessage.getGroup_id();

        //快速返回
        JSONObject returnJson = new JSONObject();
        //注册用户
        //判断如果是私聊禁止注册
        if (group_id == -1) {
            //TODO go-cqhttp只能根据qq号和群号获取某个用户的信息 但是这会导致数据库中使用双主键 比较麻烦 后期解决一下这个问题
            returnJson.put("reply","露易丝不支持私聊注册哦，\n请在群聊里使用吧");
            return returnJson;
        }
        return userService.joinLouise(user_id, group_id);
    }

    /**
     * 发送随机色图
     * @param inMessage
     * @return JSONObject
     */
    @RequestMapping("louise/setu")
    private JSONObject sendRandomSetu(@RequestBody InMessage inMessage) {

        //获取请求元数据信息
        String message_type = inMessage.getMessage_type();
        long number;
        String nickname = inMessage.getSender().getNickname();
        //TODO 有待优化的变量
        long user_id = inMessage.getUser_id();

        //判断私聊或是群聊
        String senderType = "";
        if (message_type.equals("group")) {
            number = inMessage.getGroup_id();
            senderType = "group_id";

        } else {
            number = inMessage.getUser_id();
            senderType = "user_id";
        }

        //调用LoliconAPI随机或根据参数请求色图
        userService.updateCount(user_id,1);
        return sendPictureApi.sendPicture(number, nickname, senderType, inMessage);
    }

    @RequestMapping("louise/search")
    private JSONObject searchPicture(@RequestBody JSONObject message) {

        //返回值
        JSONObject returnJson = new JSONObject();
        //解析上传的信息 拿到图片URL还有一些相关参数
        String uri = message.getString("message");
        uri = uri.substring(uri.indexOf("url=")+4, uri.length()-1);
        //获取请求元数据信息

        URL url = null;
        String filePath = LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION + "/";
        String fileName = "Image_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + UniqueGenerator.generateShortUuid() + "." + "jpg";
        String imageName = filePath + fileName;
        try {
            log.info("开始下载" + imageName + " 图片地址: " + uri);
            url = new URL(uri);
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

    //@RequestMapping("louise/pid/{pixiv_id}")
    private JSONObject findPixivId(@PathVariable String pixiv_id, @RequestBody InMessage inMessage) {
        String nickname = inMessage.getSender().getNickname();
        OutMessage outMessage = new OutMessage(inMessage);
        String msg = nickname + "，你要的图片" + pixiv_id + "找到了" +
                "\n[CQ:image,file=" +LouiseConfig.PIXIV_PROXY_URL + pixiv_id + ".jpg]" +
                "\n如果未显示出图片请在pixiv_id后指定第几张作品";

        if (outMessage.getGroup_id() < 0)
            outMessage.setMessage(msg);
        else
            outMessage.getMessages().add(new Node(msg, inMessage.getSelf_id()));
        log.info(JSONObject.toJSONString(outMessage));
        r.sendMessage(outMessage);
        return null;
    }

    /**
     * 预处理Notice类型上报
     * @param notice
     * @return JSONObject
     */
    private JSONObject handleNoticePost(JSONObject notice) {

        //获取Notice上报元数据
        String notice_type = notice.getString("notice_type");
        String user_id = notice.getString("user_id");
        logger.info("提醒类上报的类型: " + notice_type);
        //判断Notice的类型
        switch (notice_type) {
            default: return null;
            case "group_upload": return initFileUploadInfo(notice, user_id);
        }
    }

    /**
     * 查询用户信息
     * @param inMessage
     * @return
     */
    @RequestMapping("louise/myinfo")
    public JSONObject myInfo(@RequestBody InMessage inMessage) {

        long user_id = inMessage.getUser_id();

        JSONObject returnJson = new JSONObject();

        User user = userService.selectById(user_id);
        Role role = roleService.selectById(user.getRole_id());
        if (isEmpty(user)) {
            returnJson.put("reply", "没有你的信息诶");
        } else {
            OutMessage out = new OutMessage(inMessage);
            String nickname = user.getNickname();
            Timestamp create_time = user.getCreate_time();
            Integer count_setu = user.getCount_setu();
            Integer count_upload = user.getCount_upload();

            String myInfos = nickname + "，你的个人信息" +
                    "\n总共请求功能次数：" + count_setu +
                    "\n总共上传文件次数：" + count_upload +
                    "\n在露易丝这里注册的时间；" + create_time +
                    "\n-----------DIVIDER LINE------------" +
                    "\n你的权限级别：<" + role.getRole_name() + ">" +
                    "\n剩余CREDIT：" + user.getCredit() +
                    "\nCREDIT BUFF：" + user.getCredit_buff();
            out.setMessage(myInfos);
            out.setMessage_type("private");
            r.sendMessage(out);
        }
        returnJson.put("reply", "[CQ:at,qq=" + inMessage.getUser_id() + "]已私聊发送你的个人信息");
        return returnJson;

    }

    /**
     * 初次上传文件写入记录
     * @param notice JSONObject
     * @return JSONObject
     */

    private JSONObject initFileUploadInfo(JSONObject notice, String user_id) {

        logger.info("进入初始化上传流程");
        String nickname = user_id;

        //构造快速操作返回信息
        JSONObject reply = new JSONObject();
        reply.put("reply", "你上传的文件已经记录下来了");
        return reply;
    }
}
