package com.darcklh.louise.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darcklh.louise.Mapper.CronTaskDao;
import com.darcklh.louise.Model.Saito.CronTask;
import com.darcklh.louise.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2022/10/21 9:14
 * @Description
 */
@Service
public class TaskImpl implements TaskService {

    @Autowired
    private CronTaskDao cronTaskDao;

    @Override
    public List<CronTask> findBy() {
        QueryWrapper<CronTask> wrapper = new QueryWrapper<>();
        return cronTaskDao.selectList(wrapper);
    }

    @Override
    public String delBy(Integer id) {
        return null;
    }

    @Override
    public String editBy(CronTask object) {
        return null;
    }

    @Override
    public String add(CronTask object) {
        return null;
    }
}
