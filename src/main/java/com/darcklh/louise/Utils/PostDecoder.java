package com.darcklh.louise.Utils;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.GoCqhttp.*;
import com.darcklh.louise.Model.Messages.InMessage;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

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
        switch (post_type) {
            case "meta_event": return JSONObject.parseObject(msg, MetaEventPost.class);
            case "message": return JSONObject.parseObject(msg, MessagePost.class);
            case "notice": return JSONObject.parseObject(msg, NoticePost.class);
            case "request": return JSONObject.parseObject(msg, RequestPost.class);
        }
        return null;
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

}
