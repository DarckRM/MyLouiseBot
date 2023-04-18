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
public class WorkThread extends Thread {

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
    public void run() {
        try {
            for (MultiTaskService taskService : taskList) {
                taskService.setThreadId(this.threadId);
                taskService.setTotal(this.restTask);
                if (taskService.execute())
                    callBackFunc();
            }
        } catch (IOException e) {
            throw new InnerException(500, "文件读写错误", e.getMessage());
        } catch ( NoSuchAlgorithmException e) {
            throw new InnerException(501, "未知的错误", e.getClass() + "-" + e.getMessage());
        }
    }

    public void callBackFunc() {
        this.restTask--;
        if (this.restTask == 0) {
            log.info("任务列表 " + this.threadId + " 已完成");
        }
    }
}
