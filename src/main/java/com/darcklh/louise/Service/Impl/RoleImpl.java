package com.darcklh.louise.Service.Impl;

import com.darcklh.louise.Mapper.FeatureInfoDao;
import com.darcklh.louise.Mapper.RoleDao;
import com.darcklh.louise.Model.Louise.Role;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Model.VO.RoleFeatureId;
import com.darcklh.louise.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/29 0:25
 * @Description
 */
@Service
public class RoleImpl implements RoleService{

    @Autowired
    RoleDao roleDao;

    @Autowired
    FeatureInfoDao featureInfoDao;

    @Override
    public List<Role> findBy() {

        List<Role> roles = roleDao.selectList(null);
        for (Role role: roles) {
            role.setFeatureInfoList(featureInfoDao.findWithRoleId(role.getRole_id()));
        }
        return roles;
    }

    @Override
    public Role selectById(Integer role_id) {
        return roleDao.selectById(role_id);
    }

    @Override
    public String delBy(Integer role_id) {
        return null;
    }

    public Integer delRoleFeature(Integer role_id) {
        return roleDao.delRoleFeatureByRoleId(role_id);
    }

    @Override
    public String editBy(Role role) {
        String reply = "修改失败";
        if (roleDao.updateById(role) > 0) {
            reply = "修改成功";
        }
        return reply;
    }

    public String edit(RoleFeatureId roleFeatureId) {
        Role role = new Role();
        role.setRole_id(roleFeatureId.getRole_id());
        role.setRole_name(roleFeatureId.getRole_name());
        role.setInfo(roleFeatureId.getInfo());
        String reply = "修改失败";
        if (roleDao.updateById(role) > 0) {
            reply = "修改成功";
        }
        return reply;
    }

    @Override
    public String add(Role role) {
        Integer role_id = 0;
        role.setIs_enabled(-1);
        roleDao.insert(role);
        role_id = role.getRole_id();
        for (FeatureInfoMin featureInfoMin: role.getFeatureInfoList()) {
            roleDao.insertRoleFeature(role_id, featureInfoMin.getFeature_id());
        }
        return null;
    }

    public String addRoleFeature(Integer role_id, Integer feature_id) {
        roleDao.insertRoleFeature(role_id, feature_id);
        return "";
    }

    public String switchStatus(Integer role_id, String role_name) {
        String reply = "变更状态失败";
        if (roleDao.switchStatus(role_id) == 1) {
            reply = isEnabled(role_id) ? "角色<"+role_name+">已启用" : "角色<"+role_name+">已暂时禁用";
        }
        return reply;
    }

    public boolean isUserExist(Integer role_id) {
        //判断用户是否已注册
        if (roleDao.isExist(role_id) == 0)
            return false;
        return true;
    }

    public boolean isEnabled(Integer role_id) {
        //判断是否启用
        if (roleDao.isEnabled(role_id) <= 0)
            return false;
        return true;
    }
}
