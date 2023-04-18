package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Saito.CronTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author DarckLH
 * @date 2022/10/21 9:15
 * @Description
 */
@Mapper
public interface CronTaskDao extends BaseMapper<CronTask> {
}
