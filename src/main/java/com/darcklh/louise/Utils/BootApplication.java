package com.darcklh.louise.Utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Controller.CqhttpWSController;
import com.darcklh.louise.Controller.PluginInfoController;
import com.darcklh.louise.Controller.SaitoController;
import com.darcklh.louise.Mapper.SysConfigDao;
import com.darcklh.louise.Mapper.CronTaskDao;
import com.darcklh.louise.Model.Louise.Role;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Messages.Node;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.CronTask;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Model.Saito.SysConfig;
import com.darcklh.louise.Model.Sender;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Service.CronTaskService;
import com.darcklh.louise.Service.FeatureInfoService;
import com.darcklh.louise.Service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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
    RoleService roleService;

    @Autowired
    CronTaskDao cronTaskDao;

    @Autowired
    CronTaskService cronTaskService;

    @Autowired
    PluginInfoController pluginInfoController;

    @Autowired
    FeatureInfoService featureInfoService;

    @Autowired
    DragonflyUtils dragonflyUtils;

    public static Date bootDate;

    @PostConstruct
    public void run() {

        // 获取系统启动时间
        bootDate = new Date();// 获取当前时间

        // 初始化线程池
        new LouiseThreadPool(16, 32);

        // 尝试从缓存中获取配置
        log.info("<--加载 MyLouise 配置信息-->");
        List<SysConfig> list = JSONObject.parseArray(dragonflyUtils.get("sys-config"), SysConfig.class);
        if (list != null) {
            log.info("已从缓存中获取 " + list.size() + " 条系统配置");
            LouiseConfig.refreshConfig(list);
        }
        else {
            list = sysConfigDao.selectList(null);
            dragonflyUtils.set("sys-config", JSONObject.toJSONString(list));
            LouiseConfig.refreshConfig(list);
        }

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

        //将配置写入 DragonFly 缓存
        log.info("<--加载 MyLouise 功能信息-->");
        String featureKey = "model:feature:cmd:";
        String featureIdKey = "model:feature:id:";
        List<FeatureInfo> featureInfos = featureInfoService.findBy();
        for (FeatureInfo info : featureInfos) {
            String cmd = info.getFeature_cmd().split(" ")[0];
            if (cmd.contains("/"))
                cmd = cmd.substring(0, cmd.indexOf("/") + 1) + "{%";
            else
                cmd += " %";
            dragonflyUtils.set(featureKey + cmd, info);
            dragonflyUtils.set(featureIdKey + info.getFeature_id(), info);
        }

        log.info("<--加载 MyLouise 权限信息-->");
        String featureMinKey = "model:feature_min:role_id:";
        List<Role> roles = roleService.findBy();
        if (!roles.isEmpty()) {
            JSONArray array = new JSONArray();
            for ( Role role : roles) {
                List<FeatureInfoMin> mins = featureInfoService.findWithRoleId(role.getRole_id());
                array.addAll(mins);
                dragonflyUtils.set(featureMinKey + role.getRole_id(), array);
                array.clear();
            }
        } else {
            log.info("<-!未加载到 MyLouise 权限信息!->");
        }

        try {
            Message msg = Message.build();
            msg.setGroup_id(-1L);
            Sender sender = new Sender();
            sender.setNickname("ADMIN");
            msg.setSender(sender);
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
