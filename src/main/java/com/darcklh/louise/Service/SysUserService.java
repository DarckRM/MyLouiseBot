package com.darcklh.louise.Service;

import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.SysUser;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/13 23:25
 * @Description
 */
public interface SysUserService {

    public Result<SysUser> Login(SysUser sysUser);
    public SysUser findUserByNickname(String nickname);
    public List<SysUser> findAll();
    public List<SysUser> SelectOne(SysUser sysUser);

}
