package com.darcklh.louise.Model.GoCqhttp;

import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/11/12 15:15
 * @Description 通知上报
 */
@Data
public class NoticePost implements AllPost {
    // 事件发生的时间戳
    long time = 0;
    // 收到事件的机器人 QQ 号
    long self_id = 0;
    // 上报类型 message: 消息; request: 请求; notice: 通知; meta_event: 元事件
    public PostType post_type = PostType.none;

    private NoticeType notice_type;

    // 事件子类型
    private SubType sub_type;

    // 群文件上传部分字段
    private long operator_id;
    private long group_id = -1;
    private long user_id;
    // 禁言事件字段
    private long duration;

    private enum NoticeType {
        group_upload,
        group_admin,
        group_ban,
        group_card,
        group_increase,
        group_decrease,
        friend_add,
        group_recall,
        friend_recall,
        client_status,
        notify,
        essence
    }

    private enum SubType {
        set,
        unset,
        leave,
        kick,
        kick_me,
        approve,
        invite,
        ban,
        lift_ban,
        poke,
        add,
        delete,
        honor
    }
}
