package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Saito.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author DarckLH
 * @date 2021/9/13 23:21
 * @Description
 */
@Mapper
@Repository
public interface SysUserDao extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    public SysUser findUserByNickname(String username);

    public SysUser SelectOne(Wrapper<SysUser> sysUserWrapper);

}
