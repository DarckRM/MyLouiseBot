package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Louise.Role;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author DarckLH
 * @date 2021/9/29 0:27
 * @Description
 */
@Mapper
@Repository
public interface RoleDao extends BaseMapper<Role> {

    @Insert("INSERT INTO t_role_feature ( role_id, feature_id ) VALUES ( #{role_id}, #{feature_id})")
    public Integer insertRoleFeature(Integer role_id, Integer feature_id);

    @Delete("DELETE FROM t_role_feature WHERE role_id = #{role_id}")
    public Integer delRoleFeatureByRoleId(Integer role_id);

    @Insert("INSERT INTO t_role ( role_name, info, is_enabled ) VALUES ( #{role_name}, #{info}, -1 )")
    public Role insertRole(Role role);

    @Select("SELECT COUNT(role_id) FROM t_role WHERE role_id = #{role_id}")
    public Integer isExist(Integer role_id);

    @Select("SELECT is_enabled FROM t_role WHERE role_id = #{role_id}")
    public Integer isEnabled(Integer role_id);

    @Update("UPDATE t_role SET is_enabled = -is_enabled WHERE role_id = #{role_id}")
    public Integer switchStatus(Integer role_id);

}
