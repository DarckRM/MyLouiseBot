package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.GoCqhttp.AllPost;
import com.darcklh.louise.Model.GoCqhttp.MessagePost;
import com.darcklh.louise.Model.GoCqhttp.NoticePost;
import com.darcklh.louise.Model.GoCqhttp.RequestPost;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Service.FeatureInfoService;
import com.darcklh.louise.Service.Impl.FeatureInfoImpl;
import com.darcklh.louise.Utils.PluginManager;
import com.darcklh.louise.Utils.PostDecoder;
import com.darcklh.louise.Utils.PostEncoder;
import com.darcklh.louise.Utils.UniqueGenerator;
import com.mysql.cj.protocol.x.Notice;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
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

    public void handleMessagePost(MessagePost post) {
        InMessage inMessage = new InMessage(post);
        // 向所有监听模式功能发送消息
        Pattern pattern;
        // TODO 使用正则表达式判断
        for ( Map.Entry<Integer, PluginInfo> entry: PluginManager.pluginInfos.entrySet()) {
            if (entry.getValue().getType() != 0) {
                pattern = Pattern.compile(entry.getValue().getCmd());
                if (pattern.matcher(inMessage.getMessage()).find()) {
                    // 更新调用统计数据
                    cqhttpWSController.featureInfoService.addCount(entry.getValue().getFeature_id(), inMessage.getGroup_id(), inMessage.getUser_id());
                    new Thread(() -> {
                        entry.getValue().getPluginService().service(inMessage);
                    }, entry.getValue().getName()).start();
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
            case friend: {
                // 允许添加好友并且回复一些基础语句
                jsonObject.put("flag", post.getFlag());
                jsonObject.put("approve", true);
                r.requestAPI("set_friend_add_request", jsonObject);
                // 添加好友后补充发送消息
                jsonObject.put("user_id", post.getUser_id());
                jsonObject.put("message", "露易丝已经成功添加好友，有什么问题的话可以在私聊里发送!help哦");
                r.requestAPI("send_msg", jsonObject);
            }
            case group: {
                // TODO 暂不处理加群请求
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
            default:
        }
    }

    public static void startWatch(Long user_id) {
        // 进入监听模式
        accounts.add(user_id);
        listenerCounts++;
    }

    public static void stopWatch(Long user_id) {
        // 监听计数器减少，移除多余消息
        listenerCounts--;
        messageMap.remove(user_id);
        accounts.remove(user_id);
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

}
