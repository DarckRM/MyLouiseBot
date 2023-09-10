package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONArray;
import com.darcklh.louise.Model.Louise.Role;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Model.VO.RoleFeatureId;
import com.darcklh.louise.Service.FeatureInfoService;
import com.darcklh.louise.Service.RoleService;
import com.darcklh.louise.Utils.DragonflyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/29 0:24
 * @Description
 */
@RestController
@RequestMapping("saito/role/")
public class RoleController {

    @Autowired
    RoleService roleService;

    @Autowired
    FeatureInfoService featureInfoService;

    @Autowired
    DragonflyUtils dragonflyUtils;
    private final String FEATURE_MIN_KEY_ROLE_ID = "model:feature_min:role_id:";

    @RequestMapping("findBy")
    public Result<Role> findBy() {
        Result<Role> result = new Result<>();
        List<Role> roles = roleService.findBy();
        if (roles.size() == 0) {
            result.setCode(202);
            return result;
        }
        result.setCode(200);
        result.setMsg("请求成功");
        result.setDatas(roles);
        return result;
    }

    @RequestMapping("edit")
    public Result edit(@RequestBody RoleFeatureId roleFeatureId, Integer type) {

        Result<String> result = new Result<>();

        // 判断是否修改功能权限列表
        if (type != null) {
            // 清除原本的 role-feature 信息
            int delCounts = roleService.delRoleFeature(roleFeatureId.getRole_id());
            Integer roleId = roleFeatureId.getRole_id();
            if (delCounts >= 0) {
                roleService.addRoleFeatureList(roleId, roleFeatureId);
                List<FeatureInfoMin> mins = featureInfoService.findWithRoleId(roleId);
                JSONArray array = new JSONArray();
                array.addAll(mins);
                dragonflyUtils.set(FEATURE_MIN_KEY_ROLE_ID + roleId, array);
                result.setMsg(roleService.edit(roleFeatureId));
                result.setCode(200);
            } else {
                result.setMsg("更新失败");
                result.setCode(502);
            }
        } else {
            result.setMsg(roleService.edit(roleFeatureId));
            result.setCode(200);
        }
        return result;
    }

    @RequestMapping("switchStatus")
    public Result<String> test(@RequestBody Role role) {
        Result<String> result = new Result<>();
        System.out.println(role);
        result.setMsg(roleService.switchStatus(role.getRole_id(), role.getRole_name()));
        result.setCode(200);

        return result;
    }

    //TODO 实体类逻辑有点混乱 可以改进
    @RequestMapping("save")
    public Result<Role> save(@RequestBody RoleFeatureId roleFeatureId){

        Result<Role> result = new Result<Role>();

        Role role = new Role();
        FeatureInfoMin infoMin = new FeatureInfoMin();
        role.setFeatureInfoList(new ArrayList<>());
        role.setInfo(roleFeatureId.getInfo());
        role.setRole_name(roleFeatureId.getRole_name());
        for (Integer feature_id: roleFeatureId.getFeatureInfoList()) {
            infoMin.setFeature_id(feature_id);
            role.getFeatureInfoList().add(infoMin);
        }

        result.setMsg(roleService.add(role));
        result.setCode(200);

        return result;
    }

}
