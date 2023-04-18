package com.darcklh.louise;

import com.darcklh.louise.Config.LouiseConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.darcklh.louise.Mapper")
@EnableScheduling
public class MyLouiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyLouiseApplication.class, args);
    }

}
