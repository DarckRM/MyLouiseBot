package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.VO.GroupRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/28 19:45
 * @Description
 */
@Mapper
@Repository
public interface GroupDao extends BaseMapper<Group> {

    @Select("SELECT *, role_name, info FROM t_group LEFT JOIN t_role ON t_role.role_id = t_group.role_id")
    public List<GroupRole> findBy();

    @Select("SELECT COUNT(group_id) FROM t_group WHERE group_id = #{group_id}")
    public Integer isGroupExist(long group_id);

    @Select("SELECT is_enabled FROM t_group WHERE group_id = #{group_id}")
    public Integer isGroupEnabled(long group_id);

    @Update("UPDATE t_group SET is_enabled = -is_enabled WHERE group_id = #{group_id}")
    public Integer switchStatus(long group_id);
}
