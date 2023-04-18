package com.darcklh.louise.Model;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Messages.OutMessage;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/8/13 22:16
 * @Description
 */
@Data
public class ReplyException extends RuntimeException {

    /**
     * 指定抛出给QQ端的异常消息 outMessage
     */
    private OutMessage outMessage;

    /**
     * 快速返回的异常消息
     */
    private JSONObject reply = new JSONObject();

    /**
     * 异常类型
     * 0: 快速返回
     * 1: 根据指定对象返回
     */
    private int type;

    /**
     *
     * @param reply
     */
    public ReplyException(String reply) {
        this.type = 0;
        this.reply.put("reply", reply);
    }

    /**
     *
     * @param outMessage
     */
    public ReplyException(OutMessage outMessage) {
        this.type = 1;
        this.outMessage = outMessage;
    }

}
