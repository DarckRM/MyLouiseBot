package com.darcklh.louise.Model.GoCqhttp;

import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/11/12 15:15
 * @Description 请求上报
 */
@Data
public class RequestPost implements AllPost {

    // 事件发生的时间戳
    long time = 0;
    // 收到事件的机器人 QQ 号
    long self_id = 0;
    // 上报类型 message: 消息; request: 请求; notice: 通知; meta_event: 元事件
    public PostType post_type = PostType.none;
    // 请求类型
    private RequestType request_type;
    private SubType sub_type;

    private long user_id;
    private long group_id = -1;

    // 验证信息
    private String comment;
    private String flag;

    public enum RequestType {
        friend,
        group
    }

    public enum SubType {
        add,
        invite
    }
}
