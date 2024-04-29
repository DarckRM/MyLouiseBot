package com.darcklh.louise.Utils;
import com.darcklh.louise.Service.MultiTaskService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class WorkThread {

    // 本线程待执行的任务列表，你也可以指为任务索引的起始值
    private List<MultiTaskService> taskList = null;
    private List<MultiTaskService>[] taskListArray = null;
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
    public WorkThread(List<MultiTaskService> taskList, int threadId) {
        this.taskList = taskList;
        this.threadId = threadId;
        this.restTask = taskList.size();
    }

    /**
     * 不传入任务 ID 则内部进行处理，并允许同步执行
     * @param taskListArray List<MultiTaskService>
     */
    public WorkThread(List<MultiTaskService>[] taskListArray) {
        this.taskListArray = taskListArray;
        this.threadId = 0;
        for(List<MultiTaskService> item: taskListArray)
            this.restTask += item.size();
    }

    /**
     * 一个同步方法，用于执行完所有分配的任务后执行回调函数
     * @param call Call
     * @return boolean
     */
    public boolean run_sync(CallSync call) {
        try {
            AtomicInteger rest = new AtomicInteger();
            rest.set(this.restTask);

            for (List<MultiTaskService> taskList : taskListArray) {
                LouiseThreadPool.execute(() -> {
                    for(MultiTaskService service : taskList) {
                        try {
                            service.execute();
                            rest.decrementAndGet();
                        } catch (NoSuchAlgorithmException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
            int waiting = 0;
            while (rest.get() > 0) {
                if (waiting >= 120000)
                    return false;
                waiting += 1000;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            this.restTask = 0;
            call.call(this.taskListArray);
        }
        catch (Exception e) {
            log.error("执行任务异常: {}\n{}", e.getClass(), e.getMessage());
            return false;
        }
        return true;
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

    public interface CallSync {
        public void call(List<MultiTaskService>[] taskArray);
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
