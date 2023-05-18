package com.darcklh.louise;

import com.darcklh.louise.Api.FileControlApi;
import com.darcklh.louise.Utils.DragonflyUtils;
import com.darcklh.louise.Utils.LouiseThreadPool;
import com.darcklh.louise.Utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Base64;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyLouiseApplicationTests {

//    @Autowired
//    DragonflyUtils dragonflyUtils;
//    @Test
//    public void test() {
//        log.info(dragonflyUtils.scan("model:feature:").toString());
//    }
}
