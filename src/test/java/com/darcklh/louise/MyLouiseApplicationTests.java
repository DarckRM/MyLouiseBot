package com.darcklh.louise;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darcklh.louise.Api.FileControlApi;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Controller.CqhttpWSController;
import com.darcklh.louise.Mapper.BooruImagesDao;
import com.darcklh.louise.Mapper.FeatureStaticDao;
import com.darcklh.louise.Model.Annotation.LouisePlugin;
import com.darcklh.louise.Model.Annotation.OnCommand;
import com.darcklh.louise.Model.Annotation.OnMessage;
import com.darcklh.louise.Model.Louise.BooruImages;
import com.darcklh.louise.Model.Louise.BooruTags;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Saito.FeatureStatic;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Service.PluginService;
import com.darcklh.louise.Utils.DragonflyUtils;
import com.darcklh.louise.Utils.EncryptUtils;
import com.darcklh.louise.Utils.LouiseThreadPool;
import com.darcklh.louise.Utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyLouiseApplicationTests {

    private final HashMap<String, Method> commandMap = new HashMap<>();
    private final HashMap<String, Method> messageMap = new HashMap<>();

    @Autowired
    FeatureStaticDao featureStaticDao;

    @Autowired
    BooruImagesDao booruImagesDao;


    void testBooruImages() {
        QueryWrapper<BooruImages> query = new QueryWrapper<>();
        List<BooruImages> result = booruImagesDao.selectList(query);
        System.out.println(result);
    }


    void testGet() {
        String[] a = {"https://gchat.qpic.cn/gchatpic_new/3558790540/3882405198-2366533900-D71428D3D8E8397FD81DCAEC3582BE9D/0?term=255&amp;is_origin=0,file_size=127777", "https://gchat.qpic.cn/offpic_new/412543224//412543224-1138232288-87E55408B107C714457ED28B293DD106/0?term=255&amp;is_origin=1,file_size=154345"};
        for (String url : a) {
            new Thread(() -> {
                String res = OkHttpUtils.builder().url(LouiseConfig.SOURCENAO_URL)
                        .addParam("url", url)
                        .addParam("api_key", LouiseConfig.SOURCENAO_API_KEY)
                        .addParam("db", "999")
                        .addParam("output_type", "2")
                        .addParam("numres", "5")
                        .get()
                        .async();

                log.info(res);
            }).start();
        }
        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    void testRedisCache() {
        DragonflyUtils du = DragonflyUtils.getInstance();
        FeatureStatic min = new FeatureStatic(123456L, 123456L, 1, new Timestamp(123456213));
        min.setInvoke_id(0);
        Function<List<String>, List<JSONObject>> h = values -> {
            List<JSONObject> return_list = new ArrayList<>();
            values.forEach(value -> return_list.add(JSONObject.parseObject(value)));
            return return_list;
        };
        for (JSONObject one : du.list("aka", h)) {
            System.out.println(one.toString());
            featureStaticDao.insert(min);
        }
    }

    void testThreadPool() {
        while (true) {
            try {
                Thread.sleep(100);
                LouiseThreadPool.execute(() -> {
                    System.out.println("我是" + Thread.currentThread().getName() + " 號線程");
                    Thread.currentThread().interrupt();
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void testNewPlugin() {
        TestPlugin testPlugin = new TestPlugin();
        Class<? extends PluginService> plugin = testPlugin.getClass();

        // 对注解的插件进行反射处理 {命令注入} {方法实例化}
        if (plugin.isAnnotationPresent(LouisePlugin.class)) {
            // 前缀
            String prefix = plugin.getAnnotation(LouisePlugin.class).prefix();

            for (Method m : plugin.getDeclaredMethods()) {
                // 命令式方法
                if (m.isAnnotationPresent(OnCommand.class)) {
                    // 获取该方法的MyAnnotation注解实例
                    OnCommand annotation = m.getAnnotation(OnCommand.class);
                    for (String command : annotation.commands()) {
                        // 校验命令
                        if (command.length() > 12) {
                            log.info(plugin.getName() + "." + m.getName() + ":" + command + " 命令过长 已略过");
                            continue;
                        }

                        if (command.length() == 0) {
                            log.info(plugin.getName() + "." + m.getName() + ":" + command + " 命令非法 已略过");
                            continue;
                        }
                        commandMap.put(prefix + " " + command, m);
                    }
                }
                // 响应式方法
                if (m.isAnnotationPresent(OnMessage.class)) {
                    // 获取该方法的MyAnnotation注解实例
                    OnMessage annotation = m.getAnnotation(OnMessage.class);
                    for (String message : annotation.messages()) {
                        // 校验命令
                        if (message.length() > 64) {
                            log.info(plugin.getName() + "." + m.getName() + ":" + message + " 表达式过长 已略过");
                            continue;
                        }

                        if (message.length() == 0) {
                            log.info(plugin.getName() + "." + m.getName() + ":" + message + " 命令非法 已略过");
                            continue;
                        }
                        messageMap.put(message, m);
                    }
                }
            }
        }


        for (Map.Entry<String, Method> entry : commandMap.entrySet()) {
            try {
                entry.getValue().invoke(testPlugin, new Message());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void test() {
        final String body = "{\"need_wiki\":\"true\",\"role_id\":\"101009856\",\"server\":\"cn_gf01\"}";
        final String cookie = "account_mid_v2=0uypvsxxgn_mhy;cookie_token_v2=v2_309bytflwIijPrM9zXJ1V5f4_rKvuK0cp1BacssxuQ02nPk9lycbiSWccDuv9fTjmOHvSAlMUkg8LgTJcHCHyy-ZNtNxEp7wLC02cvRPYHUBJnPyoGKN;ltoken_v2=v2_y5OzvxeCIfTwYsy4V_lhau4ekC1zE4ICQl-6o8rHU6i_Jq8Zom0=;ltmid_v2=0uypvsxxgn_mhy;ltuid=76422415";
        final String url = "https://api-takumi-record.mihoyo.com/game_record/app/genshin/api/character";
        String result = OkHttpUtils.builder()
                .url(url)
                .addBody(body)
                .addHeader("Cookie", cookie)
                .addHeader("DS", getDS(body, ""))
                .addHeader("Referer", "https://webstatic.mihoyo.com")
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12; Yz-02774) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.73 Mobile Safari/537.36 miHoYoBBS/2.37.1")
                .addHeader("x-rpc-app_version", "2.37.1")
                .addHeader("x-rpc-client_type", "5")
                .post(true)
                .async();
        log.info(result);
    }

    public void testSR() {
        final String query = "role_id=100588143&server=prod_gf_cn";
        final String body = "{\"role_id\":\"100588143\",\"server\":\"prod_gf_cn\"}";
        final String cookie = "account_mid_v2=0uypvsxxgn_mhy;cookie_token_v2=v2_309bytflwIijPrM9zXJ1V5f4_rKvuK0cp1BacssxuQ02nPk9lycbiSWccDuv9fTjmOHvSAlMUkg8LgTJcHCHyy-ZNtNxEp7wLC02cvRPYHUBJnPyoGKN;ltoken_v2=v2_y5OzvxeCIfTwYsy4V_lhau4ekC1zE4ICQl-6o8rHU6i_Jq8Zom0=;ltmid_v2=0uypvsxxgn_mhy;ltuid=76422415";
        final String url = "https://api-takumi-record.mihoyo.com/game_record/app/hkrpg/api/index";
        String result = OkHttpUtils.builder()
                .url(url)
                .addParam("role_id", "100588143")
                .addParam("server", "prod_gf_cn")
                .addHeader("Cookie", cookie)
                .addHeader("DS", getDS("", query))
                .addHeader("Referer", "https://app.mihoyo.com")
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) miHoYoBBS/2.50.1")
                .addHeader("x-rpc-app_version", "2.40.1")
                .addHeader("x-rpc-client_type", "5")
                .get()
                .async();
        log.info(result);
    }

    private String getDS(String b, String q) {
        final String n = "xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs";
        final long t = new Date().getTime() / 1000;
        final int r = (int) Math.floor(Math.random() * 900000 + 100000);
        String c = EncryptUtils.MD5("salt=" + n + "&t=" + t + "&r=" + r + "&b=" + b + "&q=" + q);
        log.info(c);
        return t + "," + r + "," + c;
    }

    public void checkDS() {
        String b = "";
        final String q = "role_id=100588143&server=prod_gf_cn";
        final String n = "xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs";
        final long t = 1684852255;
        final int r = 585267;
        String c = EncryptUtils.MD5("salt=" + n + "&t=" + t + "&r=" + r + "&b=" + b + "&q=" + q);
        log.info(c);
        log.info(t + "," + r + "," + c);
    }
}
