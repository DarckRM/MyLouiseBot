package com.darcklh.louise.Api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Utils.HttpProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 发送随机好图的Api
 */
@Service
public class SendPictureApi {

    Logger logger = LoggerFactory.getLogger(SendPictureApi.class);

    public JSONObject sendPicture(long user_id, String nickname, String senderType, InMessage inMessage) {

        logger.info("进入发图流程, 发起用户为:"+nickname+" QQ:" + user_id);

        //构造Rest请求模板
        RestTemplate restTemplate = new RestTemplate();

        // 借助代理请求
        if (LouiseConfig.LOUISE_PROXY_PORT > 0)
            restTemplate.setRequestFactory(new HttpProxy().getFactory("Lolicon API 请求"));

        JSONObject jsonObject = new JSONObject();

        //构造请求LoliApi V2的请求体
        HttpHeaders loli = new HttpHeaders();
        loli.setContentType(MediaType.APPLICATION_JSON);
        JSONObject requestLoli = generateRequestBody(inMessage.getRaw_message().substring(5), "tag");
        requestLoli.put("size","regular");

        //TODO 请求第三方API的状态码判断
        HttpEntity<String> littlLoli = new HttpEntity<>(requestLoli.toString(), loli);
        String result = restTemplate.postForObject("https://api.lolicon.app/setu/v2", littlLoli, String.class);

        JSONObject goodLoli = JSONObject.parseObject(result);
        logger.info(goodLoli.getJSONArray("data").toString());

        if(goodLoli.getJSONArray("data").toArray().length == 0) {
            jsonObject.put("reply", "非常遗憾，没能找到结果");
            return jsonObject;
        }
        logger.debug(goodLoli.toString());
        //格式化结果
        String title = "";
        String author = "";
        String pid = "";
        String url = "";
        String tags = "";
        JSONArray loliArray = goodLoli.getJSONArray("data");

        for (int i = 0; i < loliArray.size(); i++) {
            title = loliArray.getJSONObject(i).getString("title");
            author = loliArray.getJSONObject(i).getString("author");
            pid = loliArray.getJSONObject(i).getString("pid");
            url = loliArray.getJSONObject(i).getJSONObject("urls").getString("regular");
            // url = louiseConfig.getPIXIV_REVERSE_PROXY() + url.substring(20);
            tags = loliArray.getJSONObject(i).getJSONArray("tags").toString();
        }
        logger.info("图片地址: " + url);
        jsonObject.put(senderType, user_id);
        jsonObject.put("reply",
                nickname+"，你要的图片已经送达，请注意身体健康哦"+
                "\n标题:"+title+
                "\n作者:"+author+
                "\npid:"+pid+
                "\n[CQ:image,file="+url+"]"+
                "\n标签:"+tags);
        return jsonObject;
    }

    /**
     * 请求参数构造器
     * @param testString
     * @param commandType
     */
    JSONObject generateRequestBody(String testString, String commandType) {

        JSONObject loliBody = new JSONObject();
        loliBody.put(commandType,"");
        JSONArray loliParams = new JSONArray();
        for (String params : testString.split(" ")) {
            JSONArray loliParam = new JSONArray();
            for (String param : params.split(",")) {
                loliParam.add(param);
            }
            loliParams.add(loliParam);
        }
        loliBody.put("tag", loliParams);
        return loliBody;
    }
}
