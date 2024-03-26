package com.darcklh.louise.Utils;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.GoCqhttp.*;

import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

/**
 * @author DarckLH
 * @date 2022/11/4 6:29
 * @Description
 */
public class PostDecoder implements Decoder.Text<AllPost> {
    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }


    @Override
    public AllPost decode(String msg) throws DecodeException {
        JSONObject json = JSONObject.parseObject(msg);
        String post_type = json.getString("post_type");
        return switch (post_type) {
            case "meta_event" -> JSONObject.parseObject(msg, MetaEventPost.class);
            case "message" -> JSONObject.parseObject(msg, MessagePost.class);
            case "notice" -> JSONObject.parseObject(msg, NoticePost.class);
            case "request" -> JSONObject.parseObject(msg, RequestPost.class);
            default -> null;
        };
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

}
