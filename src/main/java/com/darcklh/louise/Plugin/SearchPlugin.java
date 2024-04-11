package com.darcklh.louise.Plugin;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Annotation.LouisePlugin;
import com.darcklh.louise.Model.Annotation.OnMessage;
import com.darcklh.louise.Model.Enum.Environment;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Service.PluginService;
import com.darcklh.louise.Service.SearchPictureService;
import com.darcklh.louise.WebSocket.WsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@LouisePlugin(prefix = "!", name = "search")
public class SearchPlugin implements PluginService {
    private final Logger log = LoggerFactory.getLogger(SearchPlugin.class);
    @Autowired
    SearchPictureService searchPictureService;
    @Override
    public String pluginName() {
        return null;
    }

    @Override
    public JSONObject service(Message message) {
        return null;
    }

    @Override
    public JSONObject service() {
        return null;
    }

    @Override
    public boolean init(Environment env) {
        return false;
    }

    /**
     * 根据图片以及参数调用识图接口
     * @param message Message
     */
    @OnMessage(messages = "find")
    public JSONObject findPicture(Message message) {
        long userId = message.getUser_id();
        Message reply = Message.build();
        reply.setUser_id(userId);
        reply.setSender(message.getSender());
        reply.setMessage_type("private");
        reply.text("请在 15秒 内发送你要搜索的图片吧").send();
        // 尝试从 WS 获取参数
        WsServer.getMessage((value) -> {
            if (value == null) {
                message.clear().at(userId).text("你已经很久没有理 Louise 了, 下次再搜索吧").fall("等待检索图片超时");
            } else {
                message.setRaw_message(message.getRaw_message() + value.getRaw_message());
                message.setMessage_id(value.getMessage_id());
            }
        }, userId, 15000L);

        //解析上传的信息 拿到图片URL还有一些相关参数
        String url = message.getRaw_message();
        url = url.substring(url.indexOf("url=")+4, url.length()-1);

        log.info("上传图片的地址:"+ url);

        String finalUrl = url;
        message.clear().reply().text("开始检索图片").send();

        searchPictureCenter(message, finalUrl);

        return null;
    }


    /**
     * 搜图入口
     * @param message Message
     */
    private void searchPictureCenter(Message message, String url){
        // TODO 线程名过长
        searchPictureService.findWithSourceNAO(message, url);
    }

    @Override
    public boolean reload() {
        return false;
    }
}
