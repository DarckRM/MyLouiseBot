package com.darcklh.louise.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author DarckLH
 * @date 2022/10/17 15:39
 * @Description
 */
@Configuration
public class ThreadPoolTaskExecutorConfig {
    @Bean
    public ThreadPoolTaskScheduler syncScheduler() {
        ThreadPoolTaskScheduler syncScheduler = new ThreadPoolTaskScheduler();
        syncScheduler.setPoolSize(5);
        // 这里给线程设置名字，主要是为了在项目能够更快速的定位错误。
        syncScheduler.setThreadGroupName("syncTg");
        syncScheduler.setThreadNamePrefix("syncThread-");
        syncScheduler.initialize();
        return syncScheduler;
    }
}
