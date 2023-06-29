package com.darcklh.louise;

import com.darcklh.louise.Api.FileControlApi;
import com.darcklh.louise.Model.Annotation.LouisePlugin;
import com.darcklh.louise.Model.Annotation.OnCommand;
import com.darcklh.louise.Model.Annotation.OnMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Saito.PluginInfo;
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
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyLouiseApplicationTests {

    private final HashMap<String, Method> commandMap = new HashMap<>();
    private final HashMap<String, Method> messageMap = new HashMap<>();

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
                        if ( command.length() > 12 ) {
                            log.info(plugin.getName() + "." + m.getName() + ":" + command + " 命令过长 已略过");
                            continue;
                        }

                        if ( command.length() == 0) {
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
                        if ( message.length() > 64 ) {
                            log.info(plugin.getName() + "." + m.getName() + ":" + message + " 表达式过长 已略过");
                            continue;
                        }

                        if ( message.length() == 0) {
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
