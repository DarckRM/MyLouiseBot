package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Saito.CronTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author DarckLH
 * @date 2022/10/21 9:15
 * @Description
 */
@Mapper
public interface CronTaskDao extends BaseMapper<CronTask> {

    @Update("UPDATE t_cron_task SET is_enabled = -is_enabled WHERE task_id = #{id}")
    public int switchStatus(int id);
}
