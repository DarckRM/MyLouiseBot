package com.darcklh.louise.Model;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Messages.InMessage;
import lombok.Data;

import java.io.IOException;

/**
 * @author DarckLH
 * @date 2022/4/18 21:37
 * @Description 返回系统内部的异常
 */
@Data
public class InnerException extends RuntimeException {

    /**
     * 异常信息
     */
    private String errorMsg;

    /**
     * 错误码
     */
    private String innerCode;

    /**
     * 返回给客户端的消息
     */
    private JSONObject jsonObject;

    /**
     * 原始异常信息
     */
    private String originErrorMessage;

    /**
     *
     * @param innerCode
     * @param errorMsg
     * @param originErrorMessage
     */
    public InnerException(long innerCode, String errorMsg, String originErrorMessage) {
        super(errorMsg);
        this.innerCode = "SRT" + innerCode;
        this.errorMsg = "RuntimeException: "+errorMsg;
        this.originErrorMessage = "OriginErrorMsg: " + originErrorMessage;

    }

    public InnerException(long innerCode, String errorMsg, JSONObject reply, String originErrorMessage) {
        super(errorMsg);
        this.innerCode = "SRT" + innerCode;
        this.errorMsg = "RuntimeException: "+errorMsg;
        this.originErrorMessage = "OriginErrorMsg: " + originErrorMessage;
        this.jsonObject = reply;
    }

}
