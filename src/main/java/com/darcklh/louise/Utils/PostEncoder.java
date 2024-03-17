package com.darcklh.louise.Utils;

import com.alibaba.fastjson.JSON;
import com.darcklh.louise.Model.GoCqhttp.AllPost;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

/**
 * @author DarckLH
 * @date 2022/11/4 6:30
 * @Description
 */
public class PostEncoder implements Encoder.Text<AllPost> {
    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public String encode(AllPost post) throws EncodeException {
        return JSON.toJSONString(post);
    }
}
