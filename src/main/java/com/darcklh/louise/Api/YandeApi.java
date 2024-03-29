package com.darcklh.louise.Api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Enum.DownloadType;
import com.darcklh.louise.Model.Louise.BooruImages;
import com.darcklh.louise.Model.Louise.BooruTags;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Messages.Node;
import com.darcklh.louise.Model.MultiThreadTask.DownloadPicTask;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Service.BooruImagesService;
import com.darcklh.louise.Service.BooruTagsService;
import com.darcklh.louise.Service.MultiTaskService;
import com.darcklh.louise.Service.UserService;
import com.darcklh.louise.Utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

/**
 * @author DarckLH
 * @date 2022/4/13 20:21
 * @Description
 */
@Slf4j
@RestController
public class YandeApi {
    // 每页最大数
    private static final int LIMIT = 10;
    // 仅返回 Safe 评级且评分大于 15 分的结果
    private final String SAFE_KEYWORD = "rating:safe+score:>15";

    @Autowired
    FileControlApi fileControlApi;

    @Autowired
    UserService userService;

    @Autowired
    BooruTagsService booruTagsService;

    @Autowired
    BooruImagesService booruImagesService;

    @Autowired
    DragonflyUtils dragonflyUtils;

    /**
     * 向数据库追加一条图站词条对照记录
     */
    @RequestMapping("louise/yande/add")
    public JSONObject addBooruTag(@RequestBody InMessage inMessage) {

        JSONObject reply = new JSONObject();
        Message msg = Message.build(inMessage);
        long user_id = inMessage.getUser_id();
        // 解析命令
        String message = inMessage.getMessage();
        String[] tags = message.split(" ");
        ArrayList<String> tag_array = new ArrayList<>(Arrays.asList(tags));
        tag_array.removeIf(s -> s.equals(""));
        tags = tag_array.toArray(new String[0]);

        // 合法性校验
        if (tags.length <= 1)
            throw new ReplyException(msg.reply().text("你没有给出需要添加的词条哦 |д`)"));
        if (tags.length > 6)
            throw new ReplyException(msg.reply().text("太多词条别名啦，一次只能添加4个别名 (>д<)"));

        BooruTags booruTag = new BooruTags();
        // 写入创建人 QQ
        booruTag.setInfo(String.valueOf(user_id));
        if (tags[1].startsWith("--")) {
            booruTag.setOrigin_name(tags[1].substring(2));
            booruTag.setCn_name(tags[2]);
            if(booruTagsService.save(booruTag)) {
                msg.reply().text("写入标签成功 (´∀`)").send();
            } else msg.reply().text("写入失败了...").send();
            return null;
        } else {
            booruTag.setCn_name(tags[1]);
        }
        List<BooruTags> booruTags = booruTagsService.findBy(booruTag);

        // 判断是否有别名
        if (tags.length == 2) {

            if (!booruTags.isEmpty())
                throw new ReplyException("[CQ:at,qq=" + user_id + "]已经存在“" + tags[1] + "”这个标签了 (>д<)");
            // 没有词条则写入新词条
            if(booruTagsService.save(booruTag)) {
                reply.put("reply", "[CQ:at,qq=" + user_id + "]已经添加“" + tags[1] + "”标签，请为它添加需要的别名吧 （<ゝω・）☆");
                return reply;
            }
            else
                throw new ReplyException("[CQ:at,qq=" + user_id + "]添加“" + tags[1] + "”失败，请联系开发者 (´д`)");
        } else {
            // 取出查询到的记录，复写 cn_name 字段并追加到数据库
            int index = 2;

            if (!booruTags.isEmpty()) {
                booruTag = booruTags.get(0);
                booruTag.setTag_id(null);
                booruTag.setAlter_name(booruTag.getCn_name());
                // 写入创建人 QQ
                booruTag.setInfo(String.valueOf(user_id));
            } else {
                // 写入新的根词条
                // 写入创建人 QQ
                booruTag.setInfo(String.valueOf(user_id));
                if(!booruTagsService.saveAlter(booruTag))
                    throw new ReplyException("[CQ:at,qq=" + user_id + "]追加新的词条失败，请联系开发者 (>д<)");
            }

            // 遍历所有的别名并写入数据库
            for (; index < tags.length; index++) {
                booruTag.setCn_name(tags[index]);
                booruTag.setAlter_name(tags[1]);
                if(!booruTagsService.saveAlter(booruTag))
                    throw new ReplyException("[CQ:at,qq=" + user_id + "]添加“" + tags[index] + "”失败，请联系开发者 (´д`)");
            }
            reply.put("reply", "[CQ:at,qq=" + user_id + "]已成功追加所有标签 (´∀`)");
            return reply;
        }
    }

    /**
     * 提供 Booru 请求的一些帮助
     */
    @RequestMapping("louise/yande/help")
    public void booruHelp(@RequestBody InMessage inMessage) {
        Message.build(inMessage).at().text("所有符号请使用英文符号\n")
                .text("用例: !yande/day \n说明: 请求今日好图，day可为week,month\n\n")
                .text("用例: !yande hatsune_miku \n说明: 初音未来相关图片，可用空格分隔最大4个标签\n\n")
                .text("用例: !yande hatsune_miku | 2 10 \n说明: 初音未来相关图片第2页的10张，只有一个参数就只算页数\n\n")
                .text("用例: !yande/tags miku \n说明: 查询和miku有关的标签，翻页方法和上面一致\n\n")
                .text("用例: !yande/add 初音未来 ミク \n说明: 对初音未来的标签追加别名ミク，也可以不指定别名")
                .send();
    }

    /**
     * Konachan 同属 Booru 类型图站
     * 和 Yande 放在一起处理
     */
    @RequestMapping("louise/konachan/tags")
    public JSONObject konachanTags(@RequestBody InMessage inMessage) {
        // 处理命令前缀
        String[] msg;
        msg = inMessage.getMessage().split(" ");
        if (msg.length <= 1)
            throw new ReplyException("参数错误，请按如下格式尝试 !konachan/tags [参数]");
        return requestTags(msg, "https://konachan.com/tag.json?name=", inMessage);
    }


    /**
     * 根据 Tag 返回可能的 Tags 列表
     */
    @RequestMapping("louise/yande/tags")
    public JSONObject yandeTags(@RequestBody InMessage inMessage) {

        // 处理命令前缀
        String[] msg;
        msg = inMessage.getMessage().split(" ");
        if (msg.length <= 1)
            throw new ReplyException("参数错误，请按如下格式尝试 !yande/tags [参数]");
        return requestTags(msg, "https://yande.re/tag.json?name=", inMessage);
    }

    /**
     * 获取涩涩的流行 Konachan 壁纸
     */
    @RequestMapping("louise/konachan/{type}")
    public JSONObject konachanPic(@RequestBody InMessage inMessage, @PathVariable String type) {
        // TODO)) 考虑到 Yande 站的每日图片功能并不好做过滤，当群聊时转化成一般 Tag 请求
        if (inMessage.getGroup_id() > 0) {
            inMessage.setMessage("!yande *");
            requestBooru("https://konachan.com/post.json?tags=", "Konachan", inMessage);
            return null;
        }
        return requestPopular("https://konachan.com/post/popular_by_", "Konachan", type, inMessage);
    }

    /**
     *  获取 Yandere 每日图片
     */
    @RequestMapping("louise/yande/{type}")
    public JSONObject yandePic(@RequestBody InMessage inMessage, @PathVariable String type) {
        // TODO)) 考虑到 Yande 站的每日图片功能并不好做过滤，当群聊时转化成一般 Tag 请求
        if (inMessage.getGroup_id() > 0) {
            inMessage.setMessage("!yande *");
            requestBooru("https://yande.re/post.json?tags=", "Yande", inMessage);
            return null;
        }
        return requestPopular("https://yande.re/post/popular_by_", "Yande", type, inMessage);
    }

    @RequestMapping("louise/konachan")
    public JSONObject konachanSearch(@RequestBody InMessage inMessage) {
        requestBooru("https://konachan.com/post.json?tags=", "Konachan", inMessage);
        return null;
    }

    @RequestMapping("louise/yande")
    public JSONObject yandeSearch(@RequestBody InMessage inMessage) {
        requestBooru("https://yande.re/post.json?tags=", "Yande", inMessage);
        return null;
    }

    /**
     *
     * @param inMessage cqhttp send in message
     * @param resultJsonArray result as array
     * @param limit 如果是精选图集则只展示 15 张
     */
    private void sendYandeResult(InMessage inMessage, JSONArray resultJsonArray, Integer limit, Integer page, String fileOrigin, String[] tags_info, String final_tags) throws InterruptedException {
        Message message = Message.build(inMessage);
        boolean isGroup = message.getGroup_id() > 0;

        String page_nation = page + "页/" +  limit + "条";
        sendInfo(message, Arrays.toString(tags_info), page_nation);

        ArrayList<String> imagePathList = downloadFileImage(resultJsonArray, fileOrigin, page + "-" + limit + "-" + final_tags, limit, isGroup, message);
        if (imagePathList == null || imagePathList.size() == 0)
            return;
//        instantSend(message, imagePathList, Arrays.toString(tags_info), page_nation);
        sendImages(message, imagePathList);
        if (saveBooruImages(resultJsonArray))
            log.warn("图片数据写入数据库成功");
        else
            log.info("图片数据写入数据库失败");
    }

    private void instantSend(Message message, ArrayList<String> imageList, String tags_info, String page_nation) {
        sendInfo(message, tags_info, page_nation);
        sendImages(message, imageList);
    }

    private void sendInfo(Message message, String tagsInfo, String pageNation) {
        String announce = "支持中文搜索，请使用角色正确中文名\n如果想追加中文词条请使用!yande/help查看说明\n";
        message.node(Node.build().text(announce).text("你的请求结果出来了，你的参数是: " + tagsInfo + "\n分页: " + pageNation), 0);
        if (message.getGroup_id() >= 0)
            message.node(Node.build().text("已过滤过于离谱的图片，如需全部资料请私聊 (`ヮ´)"));
        message.send();
    }

    private void sendImages(Message message, ArrayList<String> imageList) {
        int imageCount = 0;
        ArrayList<Node> nodes = new ArrayList<>();
        Node imageNode = Node.build();

        for (String s : imageList) {
            if (imageCount >= 3) {
                nodes.add(imageNode);
                imageCount = 0;
                imageNode = Node.build();
            }
            imageCount += 1;
            imageNode.image(LouiseConfig.BOT_LOUISE_CACHE_IMAGE + s);
        }

        if (imageNode.getTransfers().size() != 0)
            nodes.add(imageNode);

        for (Node n : nodes) {
            message.node(n).send();
        }
    }

    private boolean isNSFW(String[] tagList) {
        for ( String tag : tagList ) {
            return switch (tag) {
                case "naked", "nipples", "sex", "anus", "breasts", "pussy", "naked_cape", "no_bra", "nopan", "bikini", "undressing", "pantsu", "monochrome", "bondage" ->
                        true;
                default -> false;
            };
        }
        return false;
    }

    private void requestBooru(String url, String target, InMessage inMessage) {
        Message msg = Message.build(inMessage);
        // 判断是否携带 Tags 参数
        if (inMessage.getMessage().length() < 7) {
            msg.reply().text("请至少携带一个 Tag 参数，像这样 !yande 参数1 参数2 页数 条数\n页数和条数可以不用指定").send();
            return;
        }

        // 处理命令前缀
        String message = inMessage.getMessage();
        message = message.substring(message.indexOf(' '));
        String[] tags;
        String[] tags_info;
        String[] pageNation = new String[2];
        pageNation[0] = "1";
        pageNation[1] = "10";

        // 格式化 Message
        tags = message.trim().split(" ");
        ArrayList<String> tag_array = new ArrayList<>(Arrays.asList(tags));
        tag_array.removeIf(s -> s.equals(""));

        // 遍历 tag_array 处理数字参数作为分页信息
        int pos = 0;
        Iterator<String> it = tag_array.iterator();
        while (it.hasNext()) {
            String tag = it.next();
            if (pos > 1)
                msg.reply().text("分页参数只有两个啦 页数 条数 (ﾟдﾟ)").fall("非法参数请求");
            try {
                if (Integer.parseInt(tag) < 0)
                    msg.reply().text("暂不支持负数分页 (*´д`)").fall("非法参数请求");
                pageNation[pos] = tag;
                pos++;
                it.remove();
            } catch (NumberFormatException e) {
                log.info("原始参数: " + tag);
            }
        }

        // 如果群聊加上过滤 tag
        if (msg.getGroup_id() != -1)
            tag_array.add(SAFE_KEYWORD);

        tags = tag_array.toArray(new String[0]);
        tags_info = tag_array.toArray(new String[0]);

        // 处理参数如果遇到中文参数则进行替换
        int index = 0;
        BooruTags booruTags = new BooruTags();
        while (index < tags.length) {
            if (tags[index].matches("[^\\x00-\\xff]+$")) {
                String nickname;
                booruTags.setCn_name(tags[index]);
                List<BooruTags> booru_list = booruTagsService.findByAlter(booruTags);

                if (!booru_list.isEmpty()) {

                    // 如果返回了多个结果 优先考虑创建者的 QQ 匹配
                    if (booru_list.size() > 1)
                        for (BooruTags bt: booru_list)
                            if (bt.getInfo().equals(String.valueOf(inMessage.getUser_id())))
                                booru_list.set(0, bt);

                    if (booru_list.get(0).getInfo() == null)
                        nickname = "";
                    else
                        nickname = userService.selectById(Long.parseLong(booru_list.get(0).getInfo())).getNickname();
                    tags[index] = booru_list.get(0).getOrigin_name();
                    tags_info[index] = booru_list.get(0).getCn_name();
                    if (nickname.equals(""))
                        tags_info[index] += "(默认)";
                    else
                        tags_info[index] += "(" + nickname + ")";
                }
                // 如果无法处理则替换为 girl
                else {
                    tags_info[index] = tags[index] + " 未支持 已替换";
                    tags[index] = "*";
                }
            }
            index++;
        }

        // pageNation 只准接收两个参数
        if (tags.length > 12) {
            msg.reply().text("标签最大只允许 12 个哦").fall("过多的参数");
            return;
        }

        // 修改最大条数
        if (Integer.parseInt(pageNation[1]) > 20)
            pageNation[1] = "20";

        // 处理线程安全问题
        String[] finalTags = tags;
        String[] final_tags_info = tags_info;
        // 尝试从缓存获取
        ArrayList<String> dragon = dragonflyUtils.get(target + " " + Arrays.toString(tags) + pageNation[0] + "页/" +  pageNation[1] + "条", ArrayList.class);
        if (dragon != null) {
            log.info("已找到 Dragonfly 缓存");
            String page_nation = pageNation[0] + "页/" +  pageNation[1] + "条";
            instantSend(Message.build(inMessage), dragon, Arrays.toString(tags_info), page_nation);
            return;
        }

        LouiseThreadPool.execute(() -> {
            // 构造消息请求体
            Message outMessage =Message.build(inMessage);
            outMessage.reply().text("开始检索 Yande 图库咯").send();
            StringBuilder tagsParam = new StringBuilder();
            // 构造 Tags 参数
            for(String tag : finalTags)
                tagsParam.append(tag).append("+");

            StringBuilder uri = new StringBuilder();
            uri.append(url).append(tagsParam.toString()).append("&limit=").append(pageNation[1]).append("&page=").append(pageNation[0]);

            log.info("请求地址: " + uri);
            // 使用代理请求 Yande
            String result = OkHttpUtils.builder().url(uri.toString()).get().async();
            JSONArray resultJsonArray = JSON.parseArray(result);

            assert resultJsonArray != null;
            if(resultJsonArray.isEmpty()) {
                outMessage.reply().text("没有找到你想要的结果呢，请检查参数是否正确，或者发送!yande/help获取帮助 |д`)").send();
                return;
            }
            try {
                sendYandeResult(inMessage, resultJsonArray, Integer.parseInt(pageNation[1]), Integer.parseInt(pageNation[0]), target, final_tags_info, Arrays.toString(finalTags));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return;
    }

    private JSONObject requestPopular(String uri, String target, String type, InMessage inMessage) {

        // 校验参数合法性
        if (!type.equals("day") && !type.equals("week") && !type.equals("month")) {
            JSONObject sendJson = new JSONObject();
            sendJson.put("reply", target + " 功能仅支持参数 day | week | month，请这样 !" + target.toLowerCase() + "/[参数] 请求哦");
            return sendJson;
        }


        uri += type + ".json";

        String finalUri = uri;
        // 尝试从缓存获取
        LouiseThreadPool.execute(() -> {
            // 构造消息请求体
            Message message = Message.build(inMessage);
            message.at().text("开始寻找今天的精选图片~").send();
            // 使用代理请求 Yande
            RestTemplate restTemplate = new RestTemplate();
            // 借助代理请求
            if (LouiseConfig.LOUISE_PROXY_PORT > 0)
                restTemplate.setRequestFactory(new HttpProxy().getFactory(target +" API"));

            String result = restTemplate.getForObject(finalUri + "?limit=" + LIMIT, String.class);
            log.info("请求 " + target + ": " + finalUri + "?limit=" + LIMIT);
            JSONArray resultJsonArray = JSON.parseArray(result);

            assert resultJsonArray != null;
            if(resultJsonArray.isEmpty()) {
                message.at().text("今天的 CG 还没有更新呢，请晚些再试吧 ~").send();
                return;
            }
            try {
                sendYandeResult(inMessage, resultJsonArray, LIMIT, 1, target, null, "/day");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        return null;
    }

    private JSONObject requestTags(String[] msg, String uri, InMessage inMessage) {
        String tag = msg[1];
        // 返回值
        JSONObject returnJson = new JSONObject();

        // TODO: 总记录条数太多 会引起 QQ 风控
        uri += tag + "&limit=" + 20;
        // 使用代理请求 Yande
        RestTemplate restTemplate = new RestTemplate();
        // 借助代理请求
        if (LouiseConfig.LOUISE_PROXY_PORT > 0)
            restTemplate.setRequestFactory(new HttpProxy().getFactory("Yande API"));

        String result = restTemplate.getForObject(uri, String.class);
        log.info("请求 Yande: " + uri);
        StringBuilder tagList = new StringBuilder(inMessage.getSender().getNickname() + ", 你是否在找?\n");
        JSONArray resultJsonArray = JSON.parseArray(result);

        assert resultJsonArray != null;

        if(resultJsonArray.isEmpty()) {
            returnJson.put("reply", "没有找到你想要的结果呢");
            return returnJson;
        }

        for ( Object object: resultJsonArray) {
            JSONObject tagObj = (JSONObject) object;
            String name = tagObj.getString("name");
            Integer count = tagObj.getInteger("count");
            Integer typeId = tagObj.getInteger("type");

            String type = switch (typeId) {
                case 0 -> "通常";
                case 1 -> "作者";
                case 3 -> "版权";
                case 4 -> "角色";
                default -> "";
            };
            tagList.append(name).append(" 类型: ").append(type).append(" 有 ").append(count).append(" 张 \r\n");
        }
        Message.build(inMessage).node(Node.build().text(tagList.toString())).send();
        return null;
    }

    private ArrayList<String> downloadSampleImage(JSONArray resultJsonArray, String booruApi, String booruParam, int limit, boolean isGroup, Message message) {
        String sampleKey = booruApi.toUpperCase() + ":SAMPLE:" + booruParam;
        ArrayList<String> filePathList = dragonflyUtils.get(sampleKey, ArrayList.class);
        if (filePathList != null)
            return filePathList;

        ArrayList<String> fileNameList = makeImageNameList(resultJsonArray, limit, isGroup);
        filePathList = makeImagePathList(fileNameList, "sample/" + booruApi + "/");
        ArrayList<String> fileUrlList = makeImageUrlList(resultJsonArray, "sample_url");

        ArrayList<String[]> uniformArrayList = makeUniformImageList(fileNameList, filePathList, fileUrlList, fileNameList.size());

        if(distributeTask(uniformArrayList, booruApi, DownloadType.SAMPLE, message)) {
            dragonflyUtils.setEx(sampleKey, filePathList, 3600);
            return filePathList;
        }

        return null;
    }

    private ArrayList<String> downloadFileImage(JSONArray resultJsonArray, String booruApi, String booruParam, int limit, boolean isGroup, Message message) {
        String fileKey = booruApi.toUpperCase() + ":FILE:" + booruParam;
        ArrayList<String> filePathList = dragonflyUtils.get(fileKey, ArrayList.class);
        if (filePathList != null)
            return filePathList;

        ArrayList<String> fileNameList = makeImageNameList(resultJsonArray, limit, isGroup);
        filePathList = makeImagePathList(fileNameList,  booruApi + "/");
        ArrayList<String> fileUrlList = makeImageUrlList(resultJsonArray, "file_url");

        ArrayList<String[]> uniformArrayList = makeUniformImageList(fileNameList, filePathList, fileUrlList, fileNameList.size());

        if(distributeTask(uniformArrayList, booruApi, DownloadType.FILE, message))
            dragonflyUtils.setEx(fileKey, filePathList, 3600);
        return null;
    }

    private ArrayList<String[]> makeUniformImageList(ArrayList<String> fileNameList, ArrayList<String> filePathList, ArrayList<String> fileUrlList, int limit) {
        ArrayList<String[]> uniformArrayList = new ArrayList<>();

        for (int index = 0; index < limit; index++) {
            String[] uniformArray = new String[3];
            uniformArray[0] = fileNameList.get(index);
            uniformArray[1] = filePathList.get(index);
            uniformArray[2] = fileUrlList.get(index);
            uniformArrayList.add(uniformArray);
        }

        return uniformArrayList;
    }

    private ArrayList<String> makeImageNameList(JSONArray resultJsonArray, int limit, boolean isGroup) {
        ArrayList<String> imageNameList = new ArrayList<>();
        for ( Object object: resultJsonArray) {
            if (limit == 0)
                break;
            JSONObject imgJsonObj = (JSONObject) object;
            String[] tagList = imgJsonObj.getString("tags").split(" ");
            // 如果是群聊跳过成人内容
            if (isGroup && isNSFW(tagList))
                continue;
            imageNameList.add(imgJsonObj.getString("md5") + "." + imgJsonObj.getString("file_ext"));
            limit--;
        }
        return imageNameList;
    }

    private ArrayList<String> makeImagePathList(ArrayList<String> imageNameList, String booruApi) {
        ArrayList<String> imagePathList = new ArrayList<>();
        for (String imageName : imageNameList)
            imagePathList.add(booruApi + imageName);
        return imagePathList;
    }

    private ArrayList<String> makeImageUrlList(JSONArray resultJsonArray, String imageKey) {
        ArrayList<String> imageUrlList = new ArrayList<>();
        for (Object o : resultJsonArray) {
            JSONObject imageJson = (JSONObject) o;
            imageUrlList.add(imageJson.getString(imageKey));
        }
        return imageUrlList;
    }

    private boolean distributeTask(ArrayList<String[]> imageList, String booruApi, DownloadType type, Message message) {
        // 下载任务的 List 集合
        ArrayList<DownloadPicTask> taskList = new ArrayList<>();
        if (Objects.requireNonNull(type) == DownloadType.SAMPLE) {
            booruApi = "sample/" + booruApi;
        }
        for (String[] image : imageList) {
            String filePath = LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION + booruApi + "/" + image[0];
//            if (fileControlApi.checkFileExist(filePath))
//                continue;
            taskList.add(new DownloadPicTask(0, image[2], image[0], booruApi, fileControlApi));
        }
        if (taskList.size() == 0)
            return true;
        try {
            return handleDownloadTask(taskList, message);
        } catch (InterruptedException e) {
            log.error("{}", e.getLocalizedMessage());
            return false;
        }
    }

    private boolean handleDownloadTask(ArrayList<DownloadPicTask> taskList, Message message) throws InterruptedException {
        List<MultiTaskService>[] taskListPerThread = TaskDistributor.distributeTasks(taskList, 4);
        List<WorkThread> workThreads = new ArrayList<>();
        for (int j = 0; j < taskListPerThread.length; j++) {
            WorkThread workThread = new WorkThread(taskListPerThread[j], j);
            workThreads.add(workThread);
            LouiseThreadPool.execute(() -> {
                workThread.run((tasks) -> {
                    ArrayList<String> imageList = new ArrayList<>();
                    Message callBack = new Message(message);
                    for (MultiTaskService mTask : tasks) {
                        DownloadPicTask task = (DownloadPicTask) mTask;
                        String filePath = task.getFileOrigin() + "/" + task.getFileName();
                        imageList.add(filePath);
                    }
                    sendImages(callBack, imageList);
                });
            });
        }
        return true;
    }

    private boolean saveBooruImages(JSONArray array) {
        int current = Integer.parseInt(Long.toString(Instant.now().getEpochSecond()));
        ArrayList<BooruImages> images = new ArrayList<>();
        for (Object element : array) {
            JSONObject image = (JSONObject) element;
            Integer parent_id = image.getInteger("parent_id");
            if (parent_id == null)
                parent_id = 0;

            images.add(new BooruImages(
                    image.getInteger("id"),
                    image.getString("tags"),
                    current,
                    image.getInteger("created_at"),
                    image.getInteger("updated_at"),
                    image.getInteger("creator_id"),
                    image.getString("author"),
                    image.getString("source"),
                    image.getString("md5"),
                    image.getString("file_ext"),
                    image.getString("file_url"),
                    image.getString("preview_url"),
                    image.getString("sample_url"),
                    image.getString("jpeg_url"),
                    image.getString("rating"),
                    image.getInteger("file_size"),
                    image.getInteger("sample_file_size"),
                    image.getInteger("jpeg_file_size"),
                    parent_id,
                    image.getInteger("width"),
                    image.getInteger("height")
            ));
        }
        return booruImagesService.saveOrUpdateBatch(images);
    }
}
