package com.darcklh.louise.Model.Messages;

import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/8/13 0:44
 * @Description
 */
@Data
public class Messages {

    // 字符串消息内容
    private String message;
    // 自定义转发消息, 具体看 CQ code
    private Node[] messages = new Node[1];

    public Messages() {

    }

    // 合并消息转发构造函数
    public Messages(String message, Long self_id) {
        // TODO 现在只支持合并转发一条消息
        this.messages[0] = new Node(message, self_id);
    }

    // 普通消息构造函数
    public Messages(String message) {
        this.message = message;
    }

}

