package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Model.VO.GroupRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PluginInfoDao extends BaseMapper<PluginInfo> {
    @Select("SELECT * FROM t_plugin_info WHERE cmd LIKE #{cmd}")
    public PluginInfo findByCmd(String cmd);
}
