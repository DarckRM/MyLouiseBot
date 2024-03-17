package com.darcklh.louise.Model;

import lombok.Data;

import jakarta.websocket.Session;

/**
 * @author DarckLH
 * @date 2021/9/24 21:40
 * @Description 管理WebSocket连接
 */
@Data
public class WebSocketClient {

    private Session session;
    private String uri;

}
