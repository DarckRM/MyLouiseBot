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

    /**
     * group_upload 群文件上传
     * group_admin 群管理员变更
     * group_decrease 群成员减少
     * group_increase 群成员增加
     * group_ban 群成员禁言
     * friend_add 好友添加
     * group_recall 群消息撤回
     * friend_recall 好友消息撤回
     * group_card 群名片变更
     * offline_file 离线文件上传
     * client_status 客户端状态变更
     * essence 精华消息
     * notify 系统通知
     */
    public enum NoticeType {
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
        essence,
        none
    }

    public enum SubType {
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
