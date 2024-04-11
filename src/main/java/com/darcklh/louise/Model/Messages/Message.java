package com.darcklh.louise.Model.Messages;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.GoCqhttp.MessagePost;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.Sender;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
@Data
public class Message {
    // 事件发生的时间戳
    private long time;
    // 收到事件的机器人 QQ 号
    private Long self_id;
    // 上报类型 message: 消息; request: 请求; notice: 通知; meta_event: 元事件
    private String post_type;
    // 消息类型
    private String message_type;
    // 消息子类型, 如果是好友则是 friend, 如果是群临时会话则是 group, 如果是在群中自身发送则是 group_self
    private String sub_type;
    // 消息 ID
    private Long message_id;
    // 发送者 QQ 号
    private Long user_id;
    // 发送群 QQ 号
    private Long group_id = -1L;
    // 数组类型消息
    private ArrayList<JSONObject> message = new ArrayList<>();
    // 合并转发时的消息结构体
    private ArrayList<Node> nodes = new ArrayList<>();
    // 原始消息内容
    private String raw_message;
    // 字体
    private int font;
    // 发送人信息
    private Sender sender;
    // 临时会话来源
    private String temp_source;

    public Message(MessagePost post) {
        this.setTime(post.getTime());
        this.setSelf_id(post.getSelf_id());
        this.setPost_type(post.getPost_type().name());
        this.setSub_type(post.getSub_type().name());
        this.setMessage_id(post.getMessage_id());
        this.setMessage_type(post.getMessage_type());
        this.setUser_id(post.getUser_id());
        this.setGroup_id(post.getGroup_id());
        // TODO)) 将采取 MessageSegment 解析
//        this.setMessage(post.getMessage());
        this.setRaw_message(post.getRaw_message());
        this.setFont(post.getFont());
        this.setSender(post.getSender());
    }

    public Message(InMessage inMessage) {
        this.setMessage_type(inMessage.getMessage_type());
        this.setGroup_id(inMessage.getGroup_id());
        this.setUser_id(inMessage.getUser_id());
        this.setSender(inMessage.getSender());
        this.setMessage_id(inMessage.getMessage_id());
        this.setPost_type(inMessage.getPost_type());
        this.setSub_type(inMessage.getSub_type());
        this.setRaw_message(inMessage.getRaw_message());
    }

    /**
     * 从 Message 构建一个新的 Message 消息内容清空
     *
     * @param message Message
     */
    public Message(Message message) {
        this.setMessage_type(message.getMessage_type());
        this.setGroup_id(message.getGroup_id());
        this.setUser_id(message.getUser_id());
        this.setSender(message.getSender());
        this.setMessage_id(message.getMessage_id());
        this.setPost_type(message.getPost_type());
        this.setSub_type(message.getSub_type());
        this.setRaw_message("");
    }

    public Message() {

    }

    public static Message build() {
        return new Message();
    }

    public static Message build(InMessage inMessage) {
        return new Message(inMessage);
    }

    public Message clear() {
        this.message.clear();
        this.nodes.clear();
        return this;
    }

    public Message at(Long user_id) {
        JSONObject obj = new JSONObject();
        obj.put("type", "at");
        JSONObject data = new JSONObject();
        data.put("qq", user_id);
        obj.put("data", data);
        this.message.add(obj);
        return this;
    }

    public Message at() {
        JSONObject obj = new JSONObject();
        obj.put("type", "at");
        JSONObject data = new JSONObject();
        data.put("qq", this.user_id);
        obj.put("data", data);
        this.message.add(obj);
        return this;
    }

    public Message text(String text) {
        JSONObject obj = new JSONObject();
        obj.put("type", "text");
        JSONObject data = new JSONObject();
        data.put("text", text);
        obj.put("data", data);
        this.message.add(obj);
        return this;
    }

    public Message text(String text, int index) {
        JSONObject obj = new JSONObject();
        obj.put("type", "text");
        JSONObject data = new JSONObject();
        data.put("text", text);
        obj.put("data", data);
        this.message.add(index, obj);
        return this;
    }

    public Message image(String image) {
        JSONObject obj = new JSONObject();
        obj.put("type", "image");
        JSONObject data = new JSONObject();
        data.put("file", image);
        obj.put("data", data);
        this.message.add(obj);
        return this;
    }

    public Message image(String image, int index) {
        JSONObject obj = new JSONObject();
        obj.put("type", "image");
        JSONObject data = new JSONObject();
        data.put("file", image);
        obj.put("data", data);
        this.message.add(index, obj);
        return this;
    }

    public Message reply() {
        if (this.message_id != null) {
            JSONObject obj = new JSONObject();
            obj.put("type", "reply");
            JSONObject data = new JSONObject();
            data.put("id", this.message_id);
            obj.put("data", data);
            this.message.add(obj);
        }
        return this;
    }

    public Message reply(Integer message_id) {
        JSONObject obj = new JSONObject();
        obj.put("type", "reply");
        JSONObject data = new JSONObject();
        data.put("id", message_id);
        obj.put("data", data);
        this.message.add(obj);
        return this;
    }

    public Message node(Node node) {
        this.nodes.add(node);
        return this;
    }

    public Message node(Node node, int index) {
        this.nodes.add(index, node);
        return this;
    }

    public void send() {
        R r = R.getR();
        if (this.group_id == -1 && !this.nodes.isEmpty()) {
            Message message = Message.build();
            message.setUser_id(this.user_id);
            message.setGroup_id(this.group_id);
            message.setMessage_type("privacy");
            message.setSender(this.sender);
            for (Node node : nodes) {
                parseNode(message, node);
                message.send();
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            long groupInfo = this.getGroup_id();
            String userInfo = this.getSender().getNickname() + "(" + this.getUser_id() + ")";
            if (groupInfo != -1)
                log.info("发送到 群聊 {}: {}", groupInfo, makeLogInfo(this.getNodes().size() == 0 ? this.getMessage().toString() : this.getNodes().toString()));
            else
                log.info("发送到 私聊 {}: {}", userInfo, makeLogInfo(this.getMessage().toString()));
            nodesToMessage();
            r.send(this);
        }
        this.clear();
    }

    private void nodesToMessage() {
        for (Node node : this.nodes) {
            this.message.add((JSONObject) JSONObject.toJSON(node));
        }
        this.nodes.clear();
    }

    private String makeLogInfo(String logInfo) {
        if (logInfo.length() >= 255)
            logInfo = logInfo.substring(0, 255) + "...内容已省略";
        return logInfo;
    }

    private void parseNode(Message message, Node node) {
        for (Node.Transfer transfer : node.transfers) {
            switch (transfer.nodeType) {
                case text -> message.text(transfer.value);
                case image -> message.image(transfer.value);
            }
        }
    }

    public void send(MessageCallBack func) {
        R r = R.getR();
        if (this.group_id == -1 && this.nodes.size() != 0) {
            Message message = Message.build();
            message.setUser_id(this.user_id);
            message.setGroup_id(this.group_id);
            message.setMessage_type("privacy");
            message.setSender(this.sender);
            for (Node node : nodes) {
                parseNode(message, node);
                message.send(func);
            }
        } else {
            func.call(r.send(this));
        }
        this.clear();
    }

    public void fall() {
        R r = new R();
        r.finish(this);
        this.clear();
    }

    public void fall(String text) {
        R r = new R();
        r.finish(this, text);
        this.clear();
    }

    public interface MessageCallBack {
        void call(JSONObject result);
    }

}
