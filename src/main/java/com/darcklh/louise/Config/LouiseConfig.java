package com.darcklh.louise.Config;

import com.darcklh.louise.Model.Saito.SysConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * 配置信息类 通过读取数据库实现动态配置
 */
@Component
@Data
@Slf4j
public class LouiseConfig {

    public static String SERVER_PORT;

    //Louise相关配置
    public static String LOUISE_PROXY;
    public static int LOUISE_PROXY_PORT;
    public static String LOUISE_WELCOME_SENTENCE;
    public static String LOUISE_HELP_PAGE;
    public static String LOUISE_ADMIN_NUMBER;
    public static String LOUISE_CACHE_IMAGE_LOCATION;
    public static String LOUISE_CACHE_LOCATION;
    public static String LOUISE_ERROR_UNKNOWN_COMMAND;
    public static String LOUISE_ERROR_THIRD_API_REQUEST_FAILED;
    public static String LOUISE_ERROR_BANNED_USER;
    public static String LOUISE_ERROR_UNKNOWN_USER;
    public static String LOUISE_ERROR_UPLOAD_IMAGE_FAILED;

    //CQ-HTTP相关配置
    public static String BOT_LOUISE_CACHE_IMAGE;
    public static String BOT_BASE_URL;
    public static String BOT_HTTP_POST_KEY;
    public static String BOT_CACHE_LOCATION;
    public static String BOT_ACCOUNT;

    //API相关配置
    public static String PIXIV_REVERSE_PROXY;
    public static String PIXIV_PROXY_URL;
    public static String SOURCENAO_URL;
    public static String SOURCENAO_API_KEY;
    public static String SOURCENAO_ERROR_REQUEST_FAILED;
    public static String AI_PAINT_URL;

    /**
     * 从数据库中更新配置
     */
    public static void refreshConfig(List<SysConfig> sysConfigs) {
        //获取系统配置项
        HashMap<String, String> configs = new HashMap<>();

        for ( SysConfig sysConfig : sysConfigs ) {
            configs.put(sysConfig.getConfig_name(), sysConfig.getConfig_value());
        }

        //从数据库中取得配置(默认读取一次配置文件)
        LOUISE_PROXY = configs.get("LOUISE.HTTP_PROXY.URL");
        LOUISE_PROXY_PORT = Integer.parseInt(configs.get("LOUISE.HTTP_PROXY.PORT"));
        LOUISE_WELCOME_SENTENCE = configs.get("LOUISE.WELCOME_SENTENCE");
        LOUISE_HELP_PAGE = configs.get("LOUISE.HELP_PAGE");
        LOUISE_ADMIN_NUMBER = configs.get("LOUISE.ADMIN_NUMBER");
        LOUISE_CACHE_LOCATION = configs.get("LOUISE.CACHE.LOCATION");
        LOUISE_CACHE_IMAGE_LOCATION = configs.get("LOUISE.CACHE.IMAGE_LOCATION");

        BOT_LOUISE_CACHE_IMAGE = configs.get("BOT.LOUISE_CACHE_IMAGE");
        BOT_BASE_URL = configs.get("BOT.BASE_URL");
        BOT_HTTP_POST_KEY = configs.get("BOT.HTTP_POST_KEY");
        BOT_CACHE_LOCATION = configs.get("BOT.CACHE_LOCATION");
        BOT_ACCOUNT = configs.get("BOT.ACCOUNT");

        SOURCENAO_URL = configs.get("API.SOURCENAO.URL");
        SOURCENAO_API_KEY = configs.get("API.SOURCENAO.API_KEY");
        PIXIV_PROXY_URL = configs.get("API.PIXIV.PROXY_URL");
        AI_PAINT_URL = configs.get("API.AI_PAINT_URL");
    }


}
