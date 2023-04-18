package com.darcklh.louise.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Api.FileControlApi;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Messages.*;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Service.SearchPictureService;
import com.darcklh.louise.Utils.HttpProxy;
import com.darcklh.louise.Utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

/**
 * @author DarckLH
 * @date 2022/8/13 0:05
 * @Description
 */
@Service
@Slf4j
public class SearchPictureImpl implements SearchPictureService {

    @Autowired
    FileControlApi fileControlApi;

    @Autowired
    R r;

    /**
     * 通过SourceNAO开放API搜图
     * @param inMessage 昵称
     * @param url
     * @return
     */
    @Override
    public void findWithSourceNAO(InMessage inMessage, String url) {
        // 构造返回体
        OutMessage outMessage = new OutMessage(inMessage);
        Message message = new Message(inMessage);
        String res;
        JSONObject sauceNAO;

        try {
            log.info("开始请求 sauceNAO 图片上传地址: " + url);
//            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 7890));
            // sauceNAO = JSON.parseObject(restTemplate.getForObject(LouiseConfig.SOURCENAO_URL + "?url={url}&db={db}&api_key={api_key}&output_type={output_type}&numres={numres}", String.class, map));
            res = OkHttpUtils.builder().url(LouiseConfig.SOURCENAO_URL)
                    .addParam("url", url)
                    .addParam("api_key", LouiseConfig.SOURCENAO_API_KEY)
                    .addParam("db", "999")
                    .addParam("output_type", "2")
                    .addParam("numres", "5")
                    .get()
                    .async();
        } catch (Exception e) {
            e.printStackTrace();
            message.text("请求 SauceNAO 出现异常").fall();
            return;
        }
        sauceNAO = JSONObject.parseObject(res);
        // 判断结果 Header
        int status = sauceNAO.getJSONObject("header").getInteger("status");

        if (status < 0)
            message.at(message.getUser_id()).text("sauceNAO 遇到了一些问题，搜索失败了").fall();

        JSONArray results = sauceNAO.getJSONArray("results");
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        Integer indexId = -9;
        String similarity = "";
        String maxSimilarity = "none";
        Integer bestIndexId = -1;
        String index_name = "";
        // 低可能性和不支持来源结果集
        List<JSONObject> bestMatches = new ArrayList<>();

        for (Object object : results) {
            // 获取单个结果的信息
            JSONObject result = (JSONObject) object;
            header = result.getJSONObject("header");
            data = result.getJSONObject("data");
            indexId = header.getInteger("index_id");
            similarity = header.getString("similarity");
            index_name = header.getString("index_name");
            if (maxSimilarity.equals("none"))
                maxSimilarity = similarity;
            // 跳过无法处理的来源
            switch (header.getInteger("index_id")) {
                case 5:
                case 4:
                case 12:
                case 9:
                case 25: {
                    //  最佳结果
                    if (bestMatches.size() == 0) {
                        bestMatches.add(result);
                        bestIndexId = indexId;
                    }
                }; break;
                default: {
                    message.node(Node.build().text("其他结果\n" + index_name + "\n相似度 " + similarity)
                            .text("\n" + data.toJSONString() + "\n")
                            .image(header.getString("thumbnail")));
                }
            }
        }

        // 最大相似度低于 70 直接返回或者没有支持的来源
        if (Float.parseFloat(maxSimilarity) < 70.0 || bestMatches.size() == 0) {
            JSONObject bestHeader = bestMatches.get(0).getJSONObject("header");
            JSONObject bestData = bestMatches.get(0).getJSONObject("data");

            message.node(Node.build().text("可能性过低的结果\n" + bestHeader.getString("index_name")
                    + "\n相似度: " + bestHeader.getString("similarity")
                    + "\n具体信息:\n" + bestData.toJSONString() + "\n")
                    .image(bestHeader.getString("thumbnail")), 0).send();
            // outMessage.getMessages().add(new Node(bestInfo, inMessage.getSelf_id()));
//            if (badList.size() != 0)
//                for (String one : badList)
//                    outMessage.getMessages().add(new Node(one, inMessage.getSelf_id()));
            return;
        }
        // 格式化结果
        log.info("最佳结果: " + bestMatches.get(0).toString());
        data = bestMatches.get(0).getJSONObject("data");
        header.put("invoker", "NAO");

        //判断结果来源以及是否可以处理 如twitter之流来源很难获取图片 会补充URL以供查看
        switch (bestIndexId) {
            //来自Pixiv
            case 5:
                handleFromPixiv(similarity, data, header, message).send();
                break;
            //TODO 暂时禁用推特来源 未解决图片缓存路径问题
            case 4:
                handleFromTwitter(similarity, data, header, message).send();
                break;
            case 12:
                handleFromYande(similarity, data, message).send();
                break;
            case 9:
            case 25:
                handleFromGelbooru(similarity, data, message).send();
                break;
        }
    }

    @Override
    public void findWithAscii2d(InMessage inMessage, String url) {
        log.info("进入Ascii2d识别流程");

        JSONObject resultData = new JSONObject();
        OutMessage outMessage = new OutMessage(inMessage);
        Message message = Message.build(inMessage);
        String nickname = outMessage.getSender().getNickname();
        RestTemplate restTemplate = new RestTemplate();
        //由于Ascii2d返回的是HTML文档 借助Jsoup进行解析
        try {

            /**
             * TODO 现在的做法是先把搜索的图片作为参数去请求一次Ascii2d
             *      Ascii2d这样才能得到图片的MD5编码
             *      获取到的Document中的色度检索和特征检索URL
             *      再以获取到的URL做请求Ascii2d
             */
            log.info("正在向Ascii2d上传图片 URL https://ascii2d.net/search/uri/?utf8=✓&uri=" + url);

            fileControlApi.downloadPicture_RestTemplate(url, "temp.jpg", "temp");
            String uri = "https://ascii2d.net/search/uri";
            String utf8 = "✓";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("utf8", utf8);
            params.add("uri", url);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.USER_AGENT, "PostmanRuntime/7.26.8");
            headers.add("Connection","keep-alive");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

            CloseableHttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
            factory.setHttpClient(httpClient);
            restTemplate.setRequestFactory(factory);

            // 借助代理请求
            if (LouiseConfig.LOUISE_PROXY_PORT > 0)
                restTemplate.setRequestFactory(new HttpProxy().getFactory("Ascii2d 上传"));

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
            // 搜索完成后删除缓存图片
            File file = new File(LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION + "temp/temp.jpg");
            file.delete();

//            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
//
//            String colorSearchUrl = response.getBody().substring(35,100);
//            String bovwSearchUrl = "https://ascii2d.net/search/bovw/" + colorSearchUrl.substring(colorSearchUrl.length() - 32);
//
//            //解析Document查询出来的第一个数据
//            log.info("上传完成 图片的检索URL为 " + bovwSearchUrl);
//            log.info("开始进行特征检索");
//            params.clear();
//            HttpEntity<MultiValueMap<String, String>> request2 = new HttpEntity<>(params, headers);
//            ResponseEntity<String> result = restTemplate.exchange(bovwSearchUrl, HttpMethod.GET, request2, String.class);
            Document document = Jsoup.parse(Objects.requireNonNull(response.getBody()));

            Element element = document.getElementsByClass("info-box").get(1).select(".detail-box h6").get(0);
            Element img = document.getElementsByClass("image-box").get(1).getElementsByAttribute("loading").get(0);

            //判断来源 Twitter跳过
            String source = element.getElementsByTag("small").text();
            String title = element.child(1).text();
            String thumbnail = "https://ascii2d.net"+img.attr("src");
            ArrayList<String> origin = new ArrayList<>();
            origin.add(element.child(1).attr("href"));
            String member_name = element.child(2).text();
            String author_page = element.child(2).attr("href");

            if (source.equals("twitter")) {
                outMessage.setMessage(nickname + "，这是Ascii2d的结果" +
                        "\n标题: " + title +
                        "\n作者: " + member_name +
                        "\n来源推特: " + origin +
                        "\n[CQ:image,file="+thumbnail+"]");
                r.sendMessage(outMessage);
                return;
            }

            String pixiv_id = origin.get(0).substring(31);
            resultData.put("pixiv_id", pixiv_id);
            resultData.put("title", title);
            resultData.put("member_name", member_name);
            resultData.put("ext_urls", origin);
            resultData.put("index_name", 0);
            resultData.put("thumbnail", thumbnail);
            resultData.put("invoker", "A2d");
            handleFromPixiv("来自Ascii2d", resultData, resultData, message).send();
        } catch (Exception e) {
            log.info("请求失败： "+e.getMessage());
            outMessage.setMessage("请求Ascii2d失败了！");
            r.sendMessage(outMessage);
        }
    }


    /**
     * 处理来自Pixiv的图
     * @param similarity String
     * @param resultData JSONObject
     * @param resultHeader JSONObject
     * @return JSONObject
     */
    private Message handleFromPixiv(String similarity, JSONObject resultData, JSONObject resultHeader, Message message) {
        Node node = Node.build();
        String nickname = message.getSender().getNickname();
        String invoker = resultHeader.getString("invoker");
        String pixiv_id = resultData.getString("pixiv_id");
        String title = resultData.getString("title");
        String thumbnail = resultHeader.getString("thumbnail");
        String member_name = resultData.getString("member_name");
        String ext_urls = resultData.getJSONArray("ext_urls").toArray()[0].toString();
        String url = LouiseConfig.BOT_LOUISE_CACHE_IMAGE + "pixiv/" + pixiv_id + ".jpg";

        //牺牲速度获得更好的图片显示 后台预解析图片信息
        try {
            Document document = Jsoup.connect(LouiseConfig.PIXIV_PROXY_URL + pixiv_id + ".jpg").ignoreHttpErrors(true).post();
            //试着确认是否多图结果
            String images_number = document.body().getElementsByTag("p").first().text();
            images_number = images_number.substring(images_number.indexOf(" ") + 1, images_number.lastIndexOf(" "));
            int count = Integer.parseInt(images_number);
            log.info("总共有 " + count + " 张图片");

            //如果是从Ascii2d调用的handleFromPixiv()那么跳过获取图片次序的逻辑
            //确认是多图结果 从JSON中获取匹配结果图片的次序
            int index;
            if (invoker.equals("NAO")) {
                String image_index = resultHeader.getString("index_name");
                image_index = image_index.substring(image_index.indexOf("_p") + 2);
                String indexString = "";
                for (char cc : image_index.toCharArray()) {
                    if (Character.isDigit(cc)) {
                        indexString += cc;
                    } else break;
                }
                index = Integer.parseInt(indexString) + 1;
                log.info("精确匹配结果为第 " + index + " 张");
            } else {
                index = 1;
                log.info("无法判断精确位置，默认为第 " + index + " 张");
            }

            int start = index - 2;
            int end = index + 2;
            int number = 0;

            while(number < 5) {

                if (start < 1 || end > count) {
                    if (start < 1)
                        start++;
                    else end--;
                    number++;
                    continue;
                }
                number++;
            }

            if (count <= 5) {
                start = 1;
                end = count;
            }
            //大于1张图的情况
            for (int i = start; i <= end; i++) {
                //下载图片到本地
                fileControlApi.downloadPicture_RestTemplate(LouiseConfig.PIXIV_PROXY_URL + pixiv_id + "-" + i + ".jpg", pixiv_id + "-" + i + ".jpg", "pixiv");
                node.image(LouiseConfig.BOT_LOUISE_CACHE_IMAGE + "pixiv/" + pixiv_id + "-" + i + ".jpg").text("\n");
            }
            message.node(node.text(nickname + "，查询出来咯，有" + count + "张结果" + "，精确结果在第" + index + "张" +
                    "\n来源Pixiv" +
                    "\n标题:" + title +
                    "\n作者:" + member_name +
                    "\n相似度:" + similarity +
                    "\n可能的图片地址:" + ext_urls + "\n")
                    .image(thumbnail), 0);
            return message;
        } catch (Exception e) {
            log.debug(e.getLocalizedMessage());
            fileControlApi.downloadPicture_RestTemplate(LouiseConfig.PIXIV_PROXY_URL + pixiv_id + ".jpg", pixiv_id + ".jpg", "pixiv");
            message.node(node.text(nickname + "，查询出来咯" +
                    "\n来源Pixiv" +
                    "\n标题:" + title +
                    "\n作者:" + member_name +
                    "\n相似度:" + similarity +
                    "\n可能的图片地址:" + ext_urls + "\n")
                    .image(thumbnail)
                    .image(url), 0);
            return message;
        }
    }
    /**
     * 处理来自Twitter的图
     * @param message Message
     * @param similarity String
     * @param sourceNaoData JSONObject
     * @param sourceNaoHeader JSONObject
     * @return JSONObject
     */
    private Message handleFromTwitter(String similarity, JSONObject sourceNaoData, JSONObject sourceNaoHeader, Message message) {
        String sourceNaoArray = sourceNaoData.getJSONArray("ext_urls").toString();
        String twitter_user_id = sourceNaoData.getString("twitter_user_id");
        String twitter_user_handle = sourceNaoData.getString("twitter_user_handle");
        String index_name = sourceNaoHeader.getString("index_name");

        String imageUrl;
        imageUrl = index_name.substring(index_name.indexOf(" - ")+3, index_name.length()-4);
        String imageUrlEndfix = index_name.substring(index_name.indexOf(".")+1, index_name.indexOf(".")+4);
        String imageUrlPrefix = "https://pbs.twimg.com/media/";
        String finalUrl = imageUrlPrefix + imageUrl + "?format=" + imageUrlEndfix + "&name=large";
        //TODO 暂时无法下载Twitter的图片
        fileControlApi.downloadPicture_RestTemplate(finalUrl, imageUrl, "Twiiter");

        return message.text(message.getSender().getNickname()+"，查询出来咯"+
                "\n来源Twitter" +
                "\n推文用户:" + twitter_user_handle+
                "\n用户ID:" + twitter_user_id+
                "\n相似度:" + similarity+
                "\n图片可能无法正常显示，说明缺乏网络环境，请点击链接访问"+
                "\n推文地址:" + sourceNaoArray+
                "\n图片地址:" + finalUrl + "\n")
                .image(finalUrl);
    }
    /**
     * 处理来自Danbooru的图
     * @param similarity String
     * @param sourceNaoData JSONObject
     * @param message Message
     * @return JSONObject
     */
    private Message handleFromGelbooru(String similarity, JSONObject sourceNaoData, Message message) {

        log.info("处理Gelbooru来源");
        Node node = Node.build();
        String sourceNaoArray = sourceNaoData.getJSONArray("ext_urls").toString();
        String characters = sourceNaoData.getString("characters");
        String creator = sourceNaoData.getString("creator");
        String gelbooru_id = sourceNaoData.getString("gelbooru_id");

        String uri = "https://gelbooru.com/index.php?page=dapi&s=post&q=index&json=1&id=" + gelbooru_id;
        JSONObject imgJsonObj = requestBooru(uri, "GelBooru API", message).getJSONArray("post").getJSONObject(0);

        String jpegUrl = imgJsonObj.getString("file_url");
        String fileName = "image_" + imgJsonObj.getString("image");
        String tags = imgJsonObj.getString("tags");
        fileControlApi.downloadPicture_RestTemplate(jpegUrl, fileName, "Gelbooru");

        message.node(node.text(message.getSender().getNickname() + "，查询出来咯"+
                        "\n来源 Gelbooru" +
                        "\n角色:" + characters +
                        "\n作者:" + creator +
                        "\n标签:" + tags +
                        "\n相似度:" + similarity +
                        "\n可能的图片地址:" + sourceNaoArray + "\n")
                .image(LouiseConfig.BOT_LOUISE_CACHE_IMAGE + "Gelbooru/" + fileName)
                .text("\n信息来自 Gelbooru，结果可能不准确，请通过上面的链接访问"), 0);
        return message;
    }

    private Message handleFromYande(String similarity, JSONObject sourceNaoData, Message message) {
        log.info("处理Yande来源");
        Node node = Node.build();
        String sourceNaoArray = sourceNaoData.getJSONArray("ext_urls").toString();
        String post_id = sourceNaoData.getString("yandere_id");
        String characters = sourceNaoData.getString("characters");
        String creator = sourceNaoData.getString("creator");

        String uri = "https://yande.re/post.json" + "?tags=id:" + post_id;
        JSONObject imgJsonObj = requestBooru(uri, "Yande API", message);

        String jpegUrl = imgJsonObj.getString("sample_url");
        String tags = imgJsonObj.getString("tags");
        String fileName = imgJsonObj.getString("md5") + "." + imgJsonObj.getString("file_ext");
        fileControlApi.downloadPicture_RestTemplate(jpegUrl, fileName, "Yande");

        message.node(node.text(message.getSender().getNickname() + "，查询出来咯"+
                        "\n来源Yande.re" +
                        "\n角色:" + characters +
                        "\n作者:" + creator +
                        "\n标签:" + tags +
                        "\n相似度:" + similarity +
                        "\n可能的图片地址:" + sourceNaoArray + "\n")
                .image(LouiseConfig.BOT_LOUISE_CACHE_IMAGE + "Yande/" + fileName)
                .text("\n信息来自Yande.re，结果可能不准确，请通过上面的链接访问"), 0);
        return message;
    }

    private JSONObject requestBooru(String uri, String booru_type, Message message) {
        // 构造请求图站的请求体
        log.info("请求地址: " + uri);
        // 使用代理请求 Yande
        RestTemplate restTemplate = new RestTemplate();
        // 借助代理请求
        if (LouiseConfig.LOUISE_PROXY_PORT > 0)
            restTemplate.setRequestFactory(new HttpProxy().getFactory(booru_type));

        JSONObject returnJson;
        JSONArray returnArray;

        String result = restTemplate.getForObject(uri, String.class);
        if (booru_type == "GelBooru API") {
            returnJson = JSON.parseObject(result);
            return returnJson;
        } else {
            returnArray = JSON.parseArray(result);
        }

        if (returnArray == null) {
            message.text("遗憾, 图片可能已经被删除了😢").send();
            return null;
        }
        return (JSONObject)returnArray.get(0);
    }

}
