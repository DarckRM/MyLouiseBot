package com.darcklh.louise;

import com.darcklh.louise.Utils.LouiseThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyLouiseApplicationTests {
    @Test
    public void test() {
        new LouiseThreadPool(8, 16);
        for (int i = 0; i < 10; i++) {
            LouiseThreadPool.execute(() -> {
                log.info("wocao");
            });
        }

    }
}
