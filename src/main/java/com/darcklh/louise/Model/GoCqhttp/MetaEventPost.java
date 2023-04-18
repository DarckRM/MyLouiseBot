package com.darcklh.louise.Model.GoCqhttp;

import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/11/12 15:16
 * @Description 元事件上报
 */
@Data
public class MetaEventPost implements AllPost {
    private String meta_event_type;
    // 事件发生的时间戳
    long time = 0;
    // 收到事件的机器人 QQ 号
    long self_id = 0;
    // 上报类型 message: 消息; request: 请求; notice: 通知; meta_event: 元事件
    public PostType post_type = PostType.none;
}
