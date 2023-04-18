package com.darcklh.louise.Controller;

import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.CronTask;
import com.darcklh.louise.Service.CronTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author DarckLH
 * @date 2022/10/17 15:24
 * @Description 控制定时任务
 */

@RestController
@RequestMapping("/saito/cron-task")
public class CronTaskController {

    @Autowired
    CronTaskService cronTaskService;

    /**
     * 返回所有的定时任务列表
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
     * @param cronTask
     * @return
     */
    @RequestMapping("/dynamic")
    public String startDynamicTask(@RequestBody CronTask cronTask){
        // 将这个添加到动态定时任务中去
        cronTaskService.add(cronTask);
        return "动态任务:"+ cronTask.getTask_name()+" 已开启";
    }

    /**
     *  根据名称 停止一个动态任务
     * @param name
     * @return
     */
    @PostMapping("/{name}")
    public String stopDynamicTask(@PathVariable("name") String name){
        // 将这个添加到动态定时任务中去
        if(!cronTaskService.stop(name)){
            return "停止失败,任务已在进行中.";
        }
        return "任务已停止";
    }
}
