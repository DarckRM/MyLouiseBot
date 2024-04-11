package com.darcklh.louise.Model;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Utils.OkHttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;


/**
 * 和Cqhttp通信的实体
 */
@Data
@Slf4j
public class R {

    //发送报文的基础信息
    private String nickname;
    private String senderType;
    private long number;

    private static R r = null;

    /**
     * 单例方法
     * @return R
     */
    public static R getR() {
        if (r == null)
            r = new R();
        return r;
    }

    private boolean isConnected() {
        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress("127.0.0.1", 3000);
        try {
            socket.setSoTimeout(1000);
            socket.connect(address, 1000);
            socket.close();
        } catch (IOException e){
            log.warn("无法与BOT建立连接: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 使用 JsonObject 作为参数请求协议端接口
     * @param api String
     * @param param JSONObject
     * @return JSONObject
     */
    public JSONObject requestAPI(String api, JSONObject param) {
        if (!isConnected())
            return null;
        //开始请求
        log.info("┌ 请求协议端接口: {}", api);
        String responseString = OkHttpUtils.builder().url(LouiseConfig.BOT_BASE_URL + api)
                .addBody(param.toJSONString())
                .post(true)
                .async();
        log.info("└ 接口 {} 返回消息: {}", api, responseString);
        return JSONObject.parseObject(responseString);
    }

    public JSONObject requestAPI(String api, Message message) {
        if (!isConnected())
            return null;

        JSONObject obj = (JSONObject) JSONObject.toJSON(message);
        // TODO 临时处理 group_id 为 -1 的情况
        if (message.getGroup_id() == -1) {
            obj.remove("group_id");
        }
        // 开始请求
        log.debug("┌ 请求 cqhttp 接口: " + api);
        String responseString = OkHttpUtils.builder().url(LouiseConfig.BOT_BASE_URL + api)
                .addBody(obj.toJSONString())
                .post(true)
                .async();
        JSONObject resp = getResponse(responseString);
        if (resp == null) {
            return null;
        }
        log.info("└ 接口 {} 返回消息: {}", api, resp);
        return resp;
    }

    public JSONObject send(Message message) {
        if (!isConnected())
            throw new InnerException(message.getUser_id(), "无法连接 BOT， 请确认 Go-Cqhttp 正在运行", "");
        return this.requestAPI("send_msg", message);
    }

    public void finish(Message message, String text) {
        if (!isConnected())
            throw new InnerException(message.getUser_id(), "无法连接 BOT， 请确认 Go-Cqhttp 正在运行", "");
        if (!message.getNodes().isEmpty())
            this.requestAPI("send_group_forward_msg", message);
        this.requestAPI("send_msg", message);
        throw new InnerException(message.getUser_id(), text, "");
    }
    public void finish(Message message) {
        if (!isConnected())
            throw new InnerException(message.getUser_id(), "无法连接 BOT， 请确认 Go-Cqhttp 正在运行", "");
        if (!message.getNodes().isEmpty())
            this.requestAPI("send_group_forward_msg", message);
        this.requestAPI("send_msg", message);
        throw new InnerException(message.getUser_id(), "主动退出", "");
    }

    /**
     * 校验请求的结果
     * @param response JSONObject
     */
    private JSONObject getResponse(String response) {
        try{
            JSONObject resp = JSONObject.parseObject(response);
            if (resp != null) {
                if (!resp.getString("status").equals("ok")) {
                    log.warn("协议端请求失败: {}", resp);
                } else {
                    log.debug("协议端请求成功: {}", resp);
                }
                return resp;
            }
            return null;
        } catch (Exception e) {
            log.error("协议端请求异常: {}", e.getMessage());
            return null;
        }
    }
}
