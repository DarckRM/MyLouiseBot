package com.darcklh.louise.Utils;

import com.darcklh.louise.Model.InnerException;
import com.darcklh.louise.Service.MultiTaskService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Data
public class WorkThread {

    // 本线程待执行的任务列表，你也可以指为任务索引的起始值
    private List<MultiTaskService> taskList = null;
    private int threadId;
    private int restTask;

    /**
     * 构造工作线程，为其指派任务列表，及命名线程 ID
     *
     * @param taskList
     *            欲执行的任务列表
     * @param threadId
     *            线程 ID
     */
    public WorkThread(List taskList, int threadId) {
        this.taskList = taskList;
        this.threadId = threadId;
        this.restTask = taskList.size();
    }

    /**
     * 执行被指派的所有任务
     */
    public void run(Call call) {
        try {
            for (MultiTaskService taskService : taskList) {
                taskService.setThreadId(this.threadId);
                taskService.setTotal(this.restTask);
                if (taskService.execute()) {
                    callBackFunc(call);
                }
            }
        }
        catch (Exception e) {
            log.error("执行任务异常: {}\n{}", e.getClass(), e.getMessage());
            callBackFunc(call);
        }
    }

    public void run() {
        try {
            for (MultiTaskService taskService : taskList) {
                taskService.setThreadId(this.threadId);
                taskService.setTotal(this.restTask);
                if (taskService.execute()) {
                    callBackFunc();
                }
            }
        }
        catch (Exception e) {
            log.error("执行任务异常: {}\n{}", e.getClass(), e.getMessage());
            callBackFunc();
        }
    }

    public interface Call {
        public void call(List<MultiTaskService> tasks);
    }

    public void callBackFunc(Call call) {
        this.restTask--;
        if (this.restTask == 0) {
            log.info("任务列表 " + this.threadId + " 已完成");
            call.call(taskList);
        }
    }

    public void callBackFunc() {
        this.restTask--;
        if (this.restTask == 0) {
            log.info("任务列表 " + this.threadId + " 已完成");
        }
    }
}
