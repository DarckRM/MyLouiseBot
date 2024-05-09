package com.darcklh.louise.CronTask;

import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Plugin.YandePlugin;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyImage {
    @Resource
    YandePlugin yandePlugin;
    private final String name = "每日 Yande 精选图集";
    private final Logger log = LoggerFactory.getLogger(DailyImage.class);
    // 动态任务的目标
//    private final long[] targets = {792873704, 798823950, 377418622, 947082838, 675699747, 701837853, 726751510};
    private final long[] targets = {392405198};

//    @Scheduled(cron = "0 10 11,18 * * ?")
//    @Scheduled(cron = "10,20,30 * * * * ?")
    public void run() {
        log.info("定时任务 {} 执行", name);
        try {
            yandePlugin.dailyImage(targets);
        } catch (Exception e) {
            log.error("定时任务 {} 异常: {}", name, e.getLocalizedMessage());
        }
        log.info("定时任务 {} 结束", name);
    }

}
