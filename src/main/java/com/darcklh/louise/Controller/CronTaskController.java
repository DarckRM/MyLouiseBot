package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.CronTask;
import com.darcklh.louise.Service.CronTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author DarckLH
 * @date 2022/10/17 15:24
 * @Description 控制定时任务
 */

@Slf4j
@RestController
@RequestMapping("/saito/cron-task")
public class CronTaskController {

    @Autowired
    CronTaskService cronTaskService;

    /**
     * 返回所有的定时任务列表
     *
     * @return
     */
    @GetMapping("/findAll")
    public Result<CronTask> findAll() {
        List<CronTask> cronTaskList = cronTaskService.list();
        if (cronTaskList.size() != 0) {
            Result<CronTask> result = new Result<>();
            result.setCode(200);
            result.setMsg("请求成功");
            result.setDatas(cronTaskList);
            return result;
        } else {
            Result<CronTask> result = new Result<>();
            result.setCode(201);
            result.setMsg("请求失败");
            return result;
        }
    }

    /**
     * 开启一个动态任务
     *
     * @param cronTask
     * @return
     */
    @RequestMapping("/dynamic")
    public String addCronTask(@RequestBody CronTask cronTask) {
        // 将这个添加到动态定时任务中去
        cronTaskService.add(cronTask);
        return "动态任务:" + cronTask.getTask_name() + " 已开启";
    }

    /**
     * 根据名称 停止一个动态任务
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCronTask(@PathVariable("id") Integer id) {
        log.info("准备禁用 " + id + " 定时任务");
        // 将这个添加到动态定时任务中去
        Result<String> result = new Result<>();
        if (!cronTaskService.stop(id)) {
            result.setCode(200);
            result.setMsg("停止失败, 任务已在进行中");
            return result;
        }
        result.setCode(200);
        result.setMsg("任务已停止");
        return result;
    }

    @GetMapping("reload/{id}")
    public Result<String> reloadCronTask(@PathVariable("id") Integer id) {
        log.info("准备重载 " + id + " 定时任务");
        Result<String> result = new Result<>();
        try {
            cronTaskService.stop(id);
            CronTask cronTask = cronTaskService.getById(id);
            cronTaskService.add(cronTask);
            result.setCode(200);
            result.setMsg("重载 " + id + " 定时任务成功");
        } catch (Exception e) {
            log.error("重载 " + id + " 定时任务时异常: " + e.getMessage() + "\n");
            e.printStackTrace();
            result.setMsg("重载 " + id + " 定时任务时异常: " + e.getMessage());
            result.setCode(500);
        }
        return result;
    }
}
