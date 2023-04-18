package com.darcklh.louise.Service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Mapper.CronTaskDao;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.Saito.CronTask;
import com.darcklh.louise.Model.Sender;
import com.darcklh.louise.Service.CronTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

/**
 * @author DarckLH
 * @date 2022/10/17 15:25
 * @Description 定时任务具体实现
 */
@Slf4j
@Service
public class CronTaskImpl extends ServiceImpl<CronTaskDao, CronTask> implements CronTaskService {
    /**
     * 以下两个都是线程安全的集合类。
     */
    public Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    public List<String> taskList = new CopyOnWriteArrayList<String>();

    @Autowired
    R r;

    private final ThreadPoolTaskScheduler syncScheduler;

    public CronTaskImpl(ThreadPoolTaskScheduler syncScheduler) {
        this.syncScheduler = syncScheduler;
    }

    /**
     * 查看已开启但还未执行的动态任务
     * @return
     */
    public List<String> getTaskList() {
        return taskList;
    }


    /**
     * 添加一个动态任务
     *
     * @param cronTask
     * @return
     */
    public boolean add(CronTask cronTask) {
        // 此处的逻辑是 ，如果当前已经有这个名字的任务存在，先删除之前的，再添加现在的。（即重复就覆盖）
        if (null != taskMap.get(cronTask.getTask_name())) {
            stop(cronTask.getTask_name());
        }

        // schedule :调度给定的Runnable ，在指定的执行时间调用它。
        //一旦调度程序关闭或返回的ScheduledFuture被取消，执行将结束。
        //参数：
        //任务 – 触发器触发时执行的 Runnable
        //startTime – 任务所需的执行时间（如果这是过去，则任务将立即执行，即尽快执行）
        ScheduledFuture<?> schedule = syncScheduler.schedule(getRunnable(cronTask), new CronTrigger(cronTask.getCron()));
        taskMap.put(cronTask.getTask_name(), schedule);
        taskList.add(cronTask.getTask_name());
        return true;
    }


    /**
     * 运行任务
     *
     * @param cronTask
     * @return
     */
    public Runnable getRunnable(CronTask cronTask) {
        return () -> {

            log.info("---执行动态任务 - " + cronTask.getTask_name() +  "---");
            try {
                log.info("系统时间 - " + LocalDateTime.now());
                // 转化参数构造 InMessage 对象
                InMessage in = new InMessage();
                in.setSelf_id(Long.parseLong(LouiseConfig.BOT_ACCOUNT));
                // 判断任务类型
                switch (cronTask.getType()) {
                    // 0 系统任务
                    case 0: break;
                    // 1 发送消息
                    case 1: {
                        in.setPost_type("message");
                        // r.sendMessage(in);
                    }; break;
                    // 2 执行功能
                    case 2: {
                        in.setPost_type("message");
                        // 构造请求体
                        RestTemplate rest = new RestTemplate();

                        // 获取执行任务 QQ 对象数组
                        String[] targets = cronTask.getTarget().split(" ");

                        for (String target: targets) {
                            String[] params = target.split("-");
                            // target 中第一个字符代表群发或私发
                            Sender sender = new Sender();
                            sender.setUser_id(Long.parseLong(cronTask.getNumber()));
                            in.setSender(sender);
                            in.setMessage(cronTask.getParameter());
                            if (params[0].equals("g")) {
                                in.setMessage_type("group");
                                in.setUser_id(Long.parseLong(cronTask.getNumber()));
                                in.setGroup_id(Long.parseLong(params[1]));
                            }
                            else {
                                in.setMessage_type("private");
                                in.setUser_id(Long.parseLong(params[1]));
                                in.setGroup_id((long)-1);
                            }
                            HttpEntity request = new HttpEntity<>(in, null);
                            JSONObject result = rest.postForObject("http://localhost:8099/louise/" + cronTask.getUrl(), request, JSONObject.class);
                            Thread.sleep(3000);
                        }

                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * 停止任务
     *
     * @param name
     * @return
     */
    public boolean stop(String name) {
        if (null == taskMap.get(name)) {
            return false;
        }
        ScheduledFuture<?> scheduledFuture = taskMap.get(name);
        scheduledFuture.cancel(true);
        taskMap.remove(name);
        taskList.remove(name);
        return true;
    }
}
