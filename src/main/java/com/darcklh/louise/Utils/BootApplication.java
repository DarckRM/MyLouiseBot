package com.darcklh.louise.Utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Controller.CqhttpWSController;
import com.darcklh.louise.Controller.PluginInfoController;
import com.darcklh.louise.Controller.SaitoController;
import com.darcklh.louise.Mapper.SysConfigDao;
import com.darcklh.louise.Mapper.CronTaskDao;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Messages.Node;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.CronTask;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Service.CronTaskService;
import com.darcklh.louise.Service.FeatureInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class BootApplication {

    @Autowired
    PluginManager pluginManager;

    @Autowired
    SysConfigDao sysConfigDao;

    @Autowired
    SaitoController saitoController;

    @Autowired
    CronTaskDao cronTaskDao;

    @Autowired
    CronTaskService cronTaskService;

    @Autowired
    PluginInfoController pluginInfoController;

    @Autowired
    FeatureInfoService featureInfoService;

    public static Date bootDate;

    @PostConstruct
    public void run() {

        // 获取系统启动时间
        bootDate = new Date();// 获取当前时间

        // 尝试从缓存中获取配置
        LouiseConfig.refreshConfig(sysConfigDao.selectList(null));

        //将配置写入 DragonFly 缓存

        log.info("<--加载 MyLouise 插件-->");
        Result<PluginInfo> result = pluginInfoController.loadPlugins();

        // 加载定时任务
        log.info("<--加载 MyLouise 定时任务-->");
        QueryWrapper<CronTask> wrapper = new QueryWrapper<>();
        wrapper.ne("is_enabled", 0);
        List<CronTask> cronTasks = cronTaskDao.selectList(wrapper);
        if (cronTasks.size() > 0) {
            for (CronTask cronTask : cronTasks) {
                cronTaskService.add(cronTask);
                log.info("加载定时任务 <-- " + cronTask.getTask_name() + "---" + cronTask.getInfo() + " -->");
            }
        }

//        log.info("<--加载 MyLouise 系统缓存-->");
//        List<FeatureInfo> featureInfos = featureInfoService.findBy();
//        dragonflyUtils.setEx("feature-info", JSONObject.toJSONString(featureInfos), 3600);

        try {
            Message msg = Message.build();
            msg.setUser_id(Long.parseLong(LouiseConfig.LOUISE_ADMIN_NUMBER));
            msg.text("启动时间 " + bootDate + " Louise 系统已启动\n")
                    .text(result.getMsg())
                    .text("\n成功加载了 " + cronTasks.size() + " 个计划任务")
            .send();
        } catch (Exception e) {
            log.info("未能建立与 Cqhttp 的连接 - Louise 系统已启动");
        }

    }

}
