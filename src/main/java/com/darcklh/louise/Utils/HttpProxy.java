package com.darcklh.louise.Utils;

import com.darcklh.louise.Config.LouiseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author DarckLH
 * @date 2022/8/7 22:15
 * @Description
 */
@Controller
@Slf4j
public class HttpProxy {

    /**
     * 构建请求代理
     * @return
     */
    public SimpleClientHttpRequestFactory getFactory(String logText) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        //单位为ms
        factory.setReadTimeout(10 * 1000);
        //单位为ms
        factory.setConnectTimeout(30 * 1000);
        // 代理的url网址或ip, port端口
        InetSocketAddress address = new InetSocketAddress(LouiseConfig.LOUISE_PROXY, LouiseConfig.LOUISE_PROXY_PORT);
        log.info("代理请求: " + LouiseConfig.LOUISE_PROXY + ":" + LouiseConfig.LOUISE_PROXY_PORT  + " "+ logText);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        factory.setProxy(proxy);
        return factory;
    }

}
