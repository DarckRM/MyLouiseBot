package com.darcklh.louise.Service.Impl;

import com.darcklh.louise.Mapper.SysUserDao;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.SysUser;
import com.darcklh.louise.Service.SysUserService;
import com.darcklh.louise.Utils.isEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/13 23:24
 * @Description
 */
@Service
public class SysUserImpl implements SysUserService {

    @Autowired
    SysUserDao sysUserDao;

    public List<SysUser> findAll() {
        return sysUserDao.selectList(null);
    }

    public Result<SysUser> Login(SysUser sysUser) {

        Result<SysUser> result = new Result<SysUser>();
        SysUser findUser = findUserByNickname(sysUser.getUsername());

        if (isEmpty.isEmpty(findUser)) {
            result.setCode(201);
            result.setMsg("用户名或密码错误");
            return result;
        }
        //判断是否启用
        if (findUser.getIsEnabled() == 0) {
            result.setCode(403);
            result.setMsg("账户已禁用");
            return result;
        }

        //验证密码
        if (findUser.getPassword().equals(sysUser.getPassword())) {
            result.setCode(200);
            // 修改密码为 Token 有效期
            findUser.setPassword(Long.toString(new Date().getTime()));
            result.setData(findUser);
            result.setMsg("登录成功");
            return result;
        } else {
            result.setCode(202);
            result.setMsg("用户名或密码错误");
            return result;
        }
    }

    public SysUser findUserByNickname(String username) {
        return sysUserDao.findUserByNickname(username);
    }

    public List<SysUser> SelectOne(SysUser sysUser) {
        return null;
    }

}
