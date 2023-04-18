package com.darcklh.louise.Service;

import com.darcklh.louise.Model.Louise.Role;
import com.darcklh.louise.Model.VO.RoleFeatureId;

/**
 * @author DarckLH
 * @date 2021/9/29 0:25
 * @Description
 */
public interface RoleService extends BaseService<Role>{
    public String switchStatus(Integer role_id, String role_name);
    public Integer delRoleFeature(Integer role_id);
    public String addRoleFeature(Integer role_id, Integer feature_id);
    public String edit(RoleFeatureId roleFeatureId);
    public Role selectById(Integer role_id);

}
