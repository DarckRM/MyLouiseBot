package com.darcklh.louise.WebSocket;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Filter.InvokeValidator;
import com.darcklh.louise.Model.GoCqhttp.AllPost;
import com.darcklh.louise.Model.GoCqhttp.MessagePost;
import com.darcklh.louise.Model.GoCqhttp.NoticePost;
import com.darcklh.louise.Model.GoCqhttp.RequestPost;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Model.Sender;
import com.darcklh.louise.Service.FeatureInfoService;
import com.darcklh.louise.Utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DarckLH
 * @date 2022/11/4 6:10
 * @Description
 */
@Component
@ServerEndpoint(value = "/onebot/v11/", decoders = {PostDecoder.class}, encoders = {PostEncoder.class})

@Slf4j
public class WsServer {

    static WsServer ws;
    @Autowired
    FeatureInfoService featureInfoService;
    @Autowired
    InvokeValidator validator;
    DragonflyUtils dragonflyUtils = DragonflyUtils.getInstance();

    @PostConstruct
    public void init() {
        ws = this;
    }

    // 和 CQHTTP Reverse WS 的连接状态
    private boolean isConnect = false;
    // 监听者计数器，当计数器为 0 时停止接收 WS 的消息
    private int listenerCounts = 0;
    // 被监听的 QQ 账号链表
    private final ArrayList<Long> accounts = new ArrayList<>();
    // 用于存放在监听状态下 WS 接收到的消息体
    private final ConcurrentHashMap<Long, Message> messageMap = new ConcurrentHashMap<>();
    // 存放唯一的和 CQHTTP 的会话
    private Session session;

    private final String FEATURE_ID_KEY = "MODEL:FEATURE:ID:";

    public void onOpen(Session session) {
        this.session = session;
        this.isConnect = true;
        log.info("成功和 onebot 客户端建立 WebSocket 连接");
    }

    @OnClose
    public void onClose() {
        log.info("go-cqhttp 断开了连接");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("go-cqhttp 错误 ,原因:" + error.getMessage());
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(AllPost post, Session session) {
        if (post.getPost_type() != AllPost.PostType.meta_event)
            log.info(post.log());

        // TODO 暂时先跳过所有心跳反应，后续可以实现 BOT 状态监听
        switch (post.getPost_type()) {
            case meta_event -> {
            }
            case message -> handleMessagePost((MessagePost) post);
            case notice -> handleNoticePost((NoticePost) post);
            case request -> handleRequestPost((RequestPost) post);
        }
    }

    // 处理响应式方法
    private void handleMessagePost(MessagePost post) {
        Message message = new Message(post);
        if (ws.listenerCounts != 0) {
            if (ws.accounts.contains(post.getUser_id())) {
                addMessage(message);
                return;
            }
        }
        spreadMessage(message);
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
                Sender sender = new Sender();
                sender.setNickname("Louise");
                sender.setUser_id(Long.parseLong(LouiseConfig.BOT_ACCOUNT));
                message.setSender(sender);
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

    private void spreadMessage(Message message) {
        // 向所有监听模式功能发送消息
        for (Map.Entry<Integer, PluginInfo> entry : PluginManager.pluginInfos.entrySet()) {
            HashMap<String, Method> messageMap = entry.getValue().getMessagesMap();
            if (messageMap.size() == 0)
                continue;

            for (Map.Entry<String, Method> keyMethod : messageMap.entrySet()) {
                // TODO)) 以后使用消息段解析
                if (!message.getRaw_message().matches(keyMethod.getKey()))
                    continue;
                FeatureInfo featureInfo = dragonflyUtils.get(FEATURE_ID_KEY + entry.getValue().getFeature_id(), FeatureInfo.class);
                if (!ws.validator.valid(message, featureInfo))
                    return;
                // 更新调用统计数据
                log.info("用户 {} 请求 {} at {}", message.getSender().getNickname() + "(" + message.getUser_id() + ")", entry.getValue().getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
                ws.featureInfoService.addCount(entry.getValue().getFeature_id(), message.getGroup_id(), message.getUser_id());
                LouiseThreadPool.execute(() -> {
                    try {
                        keyMethod.getValue().invoke(entry.getValue().getPluginService(), message);
                    } catch (Exception e) {
                        log.error("处理 ws 消息异常: {}", e.getLocalizedMessage());
                    }
                });
            }
        }
    }

    private void startWatch(Long userId) {
        // 进入监听模式
        accounts.add(userId);
        listenerCounts++;
    }

    private void stopWatch(Long userId) {
        // 监听计数器减少，移除多余消息
        listenerCounts--;
        ws.messageMap.remove(userId);
        accounts.remove(userId);
    }

    /**
     * 指定超时时间和用户ID尝试获取用户发送的消息
     *
     * @param callBack GoCallBack
     * @param userId Long
     * @param exceedTime Long
     * @return Message
     */
    public static Message getMessage(GoCallBack callBack, Long userId, Long exceedTime) {
        return ws.syncGetMessage(callBack, userId, exceedTime);
    }

    private synchronized Message syncGetMessage(GoCallBack callBack, Long userId, Long exceedTime) {
        // 进入监听模式
        startWatch(userId);
        int interval = 0;
        Message message;
        try {
            log.info("正在监听来自 " + Arrays.toString(accounts.toArray()) + " 的消息");
            while (interval < exceedTime) {
                message = messageMap.get(userId);
                if (message != null) {
                    // 监听计数器减少，移除多余消息
                    stopWatch(userId);
                    callBack.call(message);
                    return message;
                }
                wait(1000);
                interval += 1000;
            }
            stopWatch(userId);
            callBack.call(null);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    public static void addMessage(Message message) {
        ws.syncAddMessage(message);
    }

    private synchronized void syncAddMessage(Message message) {
        messageMap.put(message.getUser_id(), message);
        notifyAll();
    }

    public interface GoCallBack {
        void call(Message message);
    }
}
