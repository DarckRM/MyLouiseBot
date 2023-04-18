package com.darcklh.louise.Model.Messages;

import com.darcklh.louise.Model.Sender;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author DarckLH
 * @date 2022/8/13 0:37
 * @Description 一种发送给 GO-CQHTTP 的消息类
 */
@Data
public class OutMessage {
    // 消息类型, 支持 private、group , 分别对应私聊、群组, 如不传入, 则根据传入的 *_id 参数判断
    private String message_type;
    // 接收消息对象的 QQ
    private long user_id;
    // 接收消息对象的群组 QQ
    private long group_id;
    // 消息体支持复杂类型
    private String message;
    // 合并转发时的消息结构体
    private ArrayList<Node> messages = new ArrayList<>();
    // 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
    private boolean auto_escape = false;
    // 用于存放发送消息者信息的字段
    private Sender sender;
    private String post_type;

    /**
     * 根据传入的 InMessage 构造 OutMessage
     * @param inMessage
     */
    public OutMessage(InMessage inMessage) {
        this.transferWithInMsg(inMessage);
    }

    public OutMessage() {

    }

    /**
     * 根据传入的 InMessage 自动转换成 OutMessage
     * @param inMessage
     */
    public void transferWithInMsg(InMessage inMessage) {
        this.setMessage_type(inMessage.getMessage_type());
        this.setGroup_id(inMessage.getGroup_id());
        this.setUser_id(inMessage.getUser_id());
        this.setSender(inMessage.getSender());
    }

}
