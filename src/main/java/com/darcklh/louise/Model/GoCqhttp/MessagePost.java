package com.darcklh.louise.Model.GoCqhttp;

import com.darcklh.louise.Model.Sender;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/11/12 15:09
 * @Description Go-Cqhttp 消息上报的类型
 */
@Data
public class MessagePost implements AllPost {

    // 事件发生的时间戳
    long time = 0;
    // 收到事件的机器人 QQ 号
    long self_id = 0;
    // 上报类型 message: 消息; request: 请求; notice: 通知; meta_event: 元事件
    public PostType post_type = PostType.none;

    // 消息类型
    private String message_type;
    // 消息子类型, 如果是好友则是 friend, 如果是群临时会话则是 group, 如果是在群中自身发送则是 group_self
    private SubType sub_type;
    // 消息 ID
    private long message_id;
    // 发送者 QQ 号
    private Long user_id;
    // 发送群 QQ 号
    private Long group_id = -1L;
    // 消息内容
    private String message;
    // 原始消息内容
    private String raw_message;
    // 字体
    private int font;
    // 发送人信息
    private Sender sender;

    public enum SubType {
        friend,
        group,
        group_self,
        other,
        normal;
    }

}
