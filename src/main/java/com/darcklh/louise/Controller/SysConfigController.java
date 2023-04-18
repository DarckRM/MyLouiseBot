package com.darcklh.louise.Controller;

import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.LoggerQueue;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.SysConfig;
import com.darcklh.louise.Service.SysConfigService;
import com.darcklh.louise.Service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author DarckLH
 * @date 2021/9/20 22:36
 * @Description
 */
@RestController
@RequestMapping("saito/sys-config")
public class SysConfigController {

    Logger logger = LoggerFactory.getLogger(SysConfigController.class);

    @Autowired
    SysConfigService sysConfigService;

    @RequestMapping("/{type}")
    public Result<SysConfig> findSysConfigByType(@PathVariable String type) {

        Result<SysConfig> result = new Result<>();
        result.setDatas(sysConfigService.findSysConfigByType(type));

        return result;
    }

    @RequestMapping("findBy")
    public Result<SysConfig> findSysConfigBy() {

        Result<SysConfig> result = new Result<>();
        result.setDatas(sysConfigService.findAllSysConfig());
        return result;
    }

    @RequestMapping("edit")
    public Result editSysConfig(@RequestBody List<SysConfig> sysConfigList) {
        List<SysConfig> originalSysConfigs = sysConfigService.findAllSysConfig();
        List<SysConfig> presentSysConfigs = new ArrayList<>();

        Result result = new Result();
        result.setMsg("未变更配置");
        int i = 0;
        int index = 0;
        for (SysConfig sysConfig : sysConfigList) {
            if (!originalSysConfigs.get(index).getConfig_value().equals(sysConfig.getConfig_value())) {
                i++;
                presentSysConfigs.add(sysConfig);
            }
            index++;
        }
        if (i != 0)
            result.setMsg("修改了"+i+"条配置");
        sysConfigService.edit(presentSysConfigs);
        //更新配置
        LouiseConfig.refreshConfig(sysConfigList);
        logger.info("已刷新系统配置");
        return result;
    }

}
