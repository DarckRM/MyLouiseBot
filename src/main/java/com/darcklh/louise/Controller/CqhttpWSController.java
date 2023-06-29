package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.GoCqhttp.AllPost;
import com.darcklh.louise.Model.GoCqhttp.MessagePost;
import com.darcklh.louise.Model.GoCqhttp.NoticePost;
import com.darcklh.louise.Model.GoCqhttp.RequestPost;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Service.FeatureInfoService;
import com.darcklh.louise.Service.GroupService;
import com.darcklh.louise.Service.Impl.FeatureInfoImpl;
import com.darcklh.louise.Service.UserService;
import com.darcklh.louise.Utils.*;
import com.mysql.cj.protocol.x.Notice;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarckLH
 * @date 2022/11/4 6:10
 * @Description
 */
@ServerEndpoint(value="/go-cqhttp", decoders = { PostDecoder.class }, encoders = { PostEncoder.class })
@Slf4j
@Component
public class CqhttpWSController {

    @Autowired
    FeatureInfoService featureInfoService;

    static CqhttpWSController cqhttpWSController;

    @Autowired
    GroupService groupService;
    @Autowired
    UserService userService;

    DragonflyUtils dragonflyUtils = DragonflyUtils.getInstance();

    @PostConstruct
    public void init() {
        cqhttpWSController = this;
    }

    // 和 CQHTTP Reverse WS 的连接状态
    public boolean isConnect = false;

    // 监听者计数器，当计数器为 0 时停止接收 WS 的消息
    public static int listenerCounts = 0;

    // 被监听的 QQ 账号链表
    public static ArrayList<Long> accounts = new ArrayList<>();

    // 用于存放在监听状态下 WS 接收到的消息体
    public static ConcurrentHashMap<Long, InMessage> messageMap = new ConcurrentHashMap<>();

    // 存放唯一的和 CQHTTP 的会话
    private Session session;
    // 控制用户请求时间间隔
    Map<Long, Map<Integer , Long>> userReqLog = new HashMap<>();
    private final String featureIdKey = "model:feature:id:";

    public void onOpen(Session session) {
        this.session = session;
        this.isConnect = true;
        log.info("成功和 go-cqhttp 建立 WebSocket 连接");
    }

    @OnClose
    public void onClose() {
        log.info("go-cqhttp 断开了连接");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("go-cqhttp 错误 ,原因:"+error.getMessage());
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(AllPost post, Session session) {
        log.debug("收到 go-cqhttp 报文:" + post.toString());
        // TODO 暂时先跳过所有心跳反应，后续可以实现 BOT 状态监听

        switch (post.getPost_type()) {
            case meta_event: return;
            case message: handleMessagePost((MessagePost) post); return;
            case notice: handleNoticePost((NoticePost) post); return;
            case request: handleRequestPost((RequestPost) post);
        }
    }

    // 处理响应式方法
    public void handleMessagePost(MessagePost post) {
        InMessage inMessage = new InMessage(post);
        // 向所有监听模式功能发送消息

        // 测试
        for (Map.Entry<Integer, PluginInfo> entry : PluginManager.pluginInfos.entrySet()) {
            HashMap<String, Method> map = entry.getValue().getMessagesMap();
            if ( map.size() != 0) {
                for (Map.Entry<String, Method> keyMethod : map.entrySet()) {
                    if ( inMessage.getMessage().matches(keyMethod.getKey()) ) {
                        if(!valid(inMessage, entry.getValue()))
                            return;
                        // 更新调用统计数据
                        cqhttpWSController.featureInfoService.addCount(entry.getValue().getFeature_id(), inMessage.getGroup_id(), inMessage.getUser_id());
                        LouiseThreadPool.execute(() -> {
                            try {
                                keyMethod.getValue().invoke(entry.getValue().getPluginService(), inMessage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }
        }

        Pattern pattern;
        // TODO 可以移除了
        for ( Map.Entry<Integer, PluginInfo> entry: PluginManager.pluginInfos.entrySet()) {
            if (entry.getValue().getType() != 0) {
                pattern = Pattern.compile(entry.getValue().getCmd());
                if (pattern.matcher(inMessage.getMessage()).find()) {
                    log.info("用户 {} 请求 {} at {}", inMessage.getSender().getNickname() + "(" + inMessage.getUser_id() + ")", entry.getValue().getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
                    // 更新调用统计数据
                    cqhttpWSController.featureInfoService.addCount(entry.getValue().getFeature_id(), inMessage.getGroup_id(), inMessage.getUser_id());
                    LouiseThreadPool.execute(() -> entry.getValue().getPluginService().service(inMessage));
                }
            }
        }

        // 如果当前不处于特殊监听状态则不添加消息到 messageMap 中
        if (listenerCounts == 0)
            return;

        // 排除所有不是 message 类型且不属于监听对象的消息上报
        if (!accounts.contains(post.getUser_id()))
            return;

        // 向 messageMap 中写入消息体
        messageMap.put(post.getUser_id(), inMessage);
    }

    private void handleNoticePost(NoticePost post) {

    }

    private void handleRequestPost(RequestPost post) {
        // 判断 request_type
        JSONObject jsonObject = new JSONObject();
        R r = new R();
        switch (post.getRequest_type()) {
            case friend -> {
                // 允许添加好友并且回复一些基础语句
                jsonObject.put("flag", post.getFlag());
                jsonObject.put("approve", true);
                r.requestAPI("set_friend_add_request", jsonObject);
                // 添加好友后补充发送消息
                Message message = Message.build();
                message.setUser_id(post.getUser_id());
                message.text("露易丝已经成功添加好友，有什么问题的话可以发送!help哦").send();
            }
            case group -> {
                if (post.getSub_type() == RequestPost.SubType.add)
                    return;
                // 允许添加群组并且回复一些基础语句
                jsonObject.put("flag", post.getFlag());
                jsonObject.put("sub_type", "invite");
                jsonObject.put("approve", true);
                r.requestAPI("set_group_add_request", jsonObject);
                jsonObject.put("group_id", post.getGroup_id());
                jsonObject.put("message", "各位好，这里是露易丝bot，请管理员发送!group_join注册群聊哦，获取其它帮助请发送!help");
                r.requestAPI("send_msg", jsonObject);
            }
        }
    }

    public static void startWatch(Long userId) {
        // 进入监听模式
        accounts.add(userId);
        listenerCounts++;
    }

    public static void stopWatch(Long userId) {
        // 监听计数器减少，移除多余消息
        listenerCounts--;
        messageMap.remove(userId);
        accounts.remove(userId);
    }

    /**
     * 指定超时时间和用户ID尝试获取用户发送的消息
     * @param callBack
     * @param userId
     * @param exceedTime
     * @return InMessage inMessage
     */
    public static InMessage getMessage(GoCallBack callBack, Long userId, Long exceedTime) {
        // 进入监听模式
        startWatch(userId);
        int interval = 0;
        InMessage inMessage;
        while (interval < exceedTime) {
            if (interval % 5000 == 0)
                log.info("正在监听来自 " + Arrays.toString(accounts.toArray()) + " 的消息");
            inMessage = messageMap.get(userId);
            if (inMessage != null) {
                // 监听计数器减少，移除多余消息
                stopWatch(userId);
                callBack.call(inMessage);
                return inMessage;
            }
            interval += 1000;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 监听计数器减少，移除多余消息
        stopWatch(userId);
        callBack.call(null);
        return null;
    }

    public interface GoCallBack {
        void call(InMessage inMessage);
    }

    private boolean valid(InMessage inMessage, PluginInfo pluginInfo) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String userInfo = inMessage.getSender().getNickname() + "(" + inMessage.getUser_id() + ")";
        // 权限校验
        boolean tag = false;

        FeatureInfo featureInfo = dragonflyUtils.get(featureIdKey + pluginInfo.getFeature_id(), FeatureInfo.class);
        long userId = inMessage.getUser_id();
        long groupId = inMessage.getGroup_id();
        log.info("用户 {} 请求 {} at {}", inMessage.getSender().getNickname() + "(" + inMessage.getUser_id() + ")", pluginInfo.getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));

        // 管理员和 Bot 的命令将不受限制
        if (userId == Long.parseLong(LouiseConfig.BOT_ACCOUNT)) {
            featureInfoService.addCount(pluginInfo.getFeature_id(), groupId, userId);
            return true;
        }

        // 更新 Interval 控制
        long now = new Date().getTime();
        int featureId = pluginInfo.getFeature_id();

        Map<Integer, Long> reqLogs = userReqLog.get(inMessage.getUser_id());
        if (reqLogs != null) {
            Long lastReq = reqLogs.get(featureId);
            if(null != lastReq) {
                long interval = now - lastReq;
                long reqLimit = featureInfo.getInvoke_limit() * 1000L;
                if (interval < reqLimit)
                {
                    Message.build(inMessage).reply().at().text("此功能还有 " + (reqLimit - interval) / 1000 + " 秒冷却").fall();
                    return false;
                }
                else
                    reqLogs.put(featureId, now);
            } else
                reqLogs.put(featureId, now);
        } else {
            Map<Integer, Long> userMap = new HashMap<>();
            userMap.put(featureId, now);
            userReqLog.put(inMessage.getUser_id(), userMap);
        }
        howMuchCost("| 请求限制校验耗时 {} 毫秒", stopWatch);
        // 放行不需要鉴权的命令
        if (featureInfo.getIs_auth() == 0) {
            // 更新调用统计数据
            featureInfoService.addCount(featureInfo.getFeature_id(), groupId, userId);
            return true;
        }
        User user = cqhttpWSController.userService.selectById(userId);
        // 判断用户是否存在并启用
        if (user == null) {
            log.info("未登记的用户 {}", userInfo);
            Message.build(inMessage).reply().at().text("请在群内发送 !join 以启用你的使用权限").fall();
            return false;
        } else if (user.getIsEnabled() != 1) {
            log.info("未启用的用户 {}", userInfo);
            Message.build(inMessage).reply().at().text("你的权限已被暂时禁用").fall();
            return false;
        }

        // 判断群聊还是私聊
        if (inMessage.getGroup_id() != -1) {
            Group group = cqhttpWSController.groupService.selectById(groupId);
            if (group != null) {
                if (group.getIs_enabled() != 1) {
                    log.info("未启用的群组: {}", groupId);
                    throw new ReplyException("主人不准露易丝在这个群里说话哦");
                }
            } else {
                log.info("未注册的群组: {}", groupId);
                throw new ReplyException("群聊还没有在露易丝中注册哦");
            }

            List<FeatureInfoMin> featureInfoMins = cqhttpWSController.featureInfoService.findWithRoleId(group.getRole_id());
            log.debug("| 群聊允许的功能列表: {}", formatList(featureInfoMins));
            for ( FeatureInfoMin featureInfoMin: featureInfoMins) {
                if (featureInfoMin.getFeature_id().equals(featureInfo.getFeature_id())) {
                    tag = true;
                    break;
                }
            }
            if (!tag)
                throw new ReplyException("这个群聊的权限不准用这个功能哦");
        }

        tag = false;

        List<FeatureInfoMin> featureInfoMins = cqhttpWSController.featureInfoService.findWithRoleId(user.getRole_id());
        log.debug("| 用户允许的功能列表: {}", formatList(featureInfoMins));
        for ( FeatureInfoMin featureInfoMin: featureInfoMins) {
            if (featureInfoMin.getFeature_id().equals(featureInfo.getFeature_id())) {
                tag = true;
                break;
            }
        }
        if (!tag)
            throw new ReplyException("你的权限还不准用这个功能哦");

        //合法性校验通过 扣除CREDIT
        int credit = cqhttpWSController.userService.minusCredit(userId, featureInfo.getCredit_cost());
        if (credit < 0) {
            throw new ReplyException("你的CREDIT余额不足哦");
        }
        howMuchCost("| 请求鉴权耗时 {} 毫秒", stopWatch);
        return true;
    }

    private void howMuchCost(String prompt, StopWatch stopWatch) {
        stopWatch.stop();
        log.info(prompt, stopWatch.getTotalTimeMillis());
        stopWatch.start();
    }
    private String formatList(List<FeatureInfoMin> list) {
        StringBuilder result = new StringBuilder();
        for ( FeatureInfoMin min : list) {
            result.append(min.getFeature_id()).append(":").append(min.getFeature_name()).append("; ");
        }
        result.append("]");
        return result.toString();
    }
}
