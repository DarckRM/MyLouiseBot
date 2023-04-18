package com.darcklh.louise.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.darcklh.louise.Model.Saito.CronTask;
import org.springframework.stereotype.Service;

/**
 * @author DarckLH
 * @date 2022/10/17 15:40
 * @Description
 */

@Service
public interface CronTaskService extends IService<CronTask> {
    public boolean add(CronTask cronTask);
    public Runnable getRunnable(CronTask cronTask);
    public boolean stop(String name);
}
