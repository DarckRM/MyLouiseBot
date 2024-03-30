package com.darcklh.louise.Model.Messages;

import com.darcklh.louise.Model.GoCqhttp.MessagePost;
import com.darcklh.louise.Model.Sender;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/8/8 20:57
 * @Description 包含 go-cqhttp 上报的消息体
 */
@Data
@Deprecated(since = "该类大部分方法可以由 Message 类覆盖，不再需要")
public class InMessage {

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
    // 消息内容
    private String message;
    // 原始消息内容
    private String raw_message;
    // 字体
    private long font;
    // 发送人信息
    private Sender sender;
    // 临时会话来源
    private String temp_source;

    public InMessage() {

    }

    public InMessage(MessagePost post) {
        this.setTime(post.getTime());
        this.setSelf_id(post.getSelf_id());
        this.setPost_type(post.getPost_type().name());
        this.setSub_type(post.getSub_type().name());
        this.setMessage_id(post.getMessage_id());
        this.setMessage_type(post.getMessage_type());
        this.setUser_id(post.getUser_id());
        this.setGroup_id(post.getGroup_id());
        this.setMessage(post.getMessage());
        this.setRaw_message(post.getRaw_message());
        this.setFont(post.getFont());
        this.setSender(post.getSender());
    }

    public InMessage(Message message) {
        this.setTime(message.getTime());
        this.setSelf_id(message.getSelf_id());
        this.setPost_type(message.getPost_type());
        this.setSub_type(message.getSub_type());
        this.setMessage_id(message.getMessage_id());
        this.setMessage_type(message.getMessage_type());
        this.setUser_id(message.getUser_id());
        this.setGroup_id(message.getGroup_id());
        this.setMessage(message.getMessage().toString());
        this.setRaw_message(message.getRaw_message());
        this.setFont(message.getFont());
        this.setSender(message.getSender());
    }

}
