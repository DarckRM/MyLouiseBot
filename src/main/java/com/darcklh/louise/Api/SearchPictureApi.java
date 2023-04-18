package com.darcklh.louise.Api;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Controller.CqhttpWSController;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Messages.OutMessage;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Service.SearchPictureService;
import com.darcklh.louise.Utils.UniqueGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 识别发送的图片的Api
 */
@RestController
@Slf4j
public class SearchPictureApi{

    @Autowired
    SearchPictureService searchPictureService;

    @Autowired
    R r;

    /**
     * 根据图片以及参数调用识图接口
     * @param inMessage
     * @return
     */
    @RequestMapping("louise/find")
    private JSONObject findPicture(@RequestBody InMessage inMessage) {

        Message msg = new Message(inMessage);

        new Thread(() -> {
            long userId = inMessage.getUser_id();
            msg.at(userId).text("请在 15秒 内发送你要搜索的图片吧").send();
            // 尝试从 WS 获取参数
            CqhttpWSController.getMessage((value) -> {
                if (value == null) {
                    msg.at(userId).text("你已经很久没有理 Louise 了, 下次再搜索吧").fall();
                } else {
                    inMessage.setMessage(inMessage.getMessage() + value.getMessage());
                    msg.setMessage_id(value.getMessage_id());
                }
            }, userId, 15000L);

            //解析上传的信息 拿到图片URL还有一些相关参数
            String url = inMessage.getMessage();
            url = url.substring(url.indexOf("url=")+4, url.length()-1);

            log.info("上传图片的地址:"+ url);

            String finalUrl = url;
            msg.reply().text("开始检索图片").send();
            new Thread(() -> {searchPictureCenter(inMessage, finalUrl); }).start();

        }).start();
        return null;
    }


    /**
     * 搜图入口
     * @param inMessage
     * @return
     */
    private void searchPictureCenter(InMessage inMessage, String url){
        // TODO 线程名过长
        new Thread(() -> searchPictureService.findWithSourceNAO(inMessage, url), UniqueGenerator.uniqueThreadName("", "NAO")).start();
        // new Thread(() -> searchPictureService.findWithAscii2d(inMessage, url), UniqueGenerator.uniqueThreadName("", "A2d")).start();

    }

}
