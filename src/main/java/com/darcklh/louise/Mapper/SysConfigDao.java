package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Saito.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/20 17:12
 * @Description
 */
@Mapper
@Repository
public interface SysConfigDao extends BaseMapper<SysConfig> {

    @Select("SELECT * FROM t_sys_config WHERE type = #{type}")
    public List<SysConfig> findSysConfigByType(Integer type);

}
