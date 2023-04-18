package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.VO.GroupRole;
import com.darcklh.louise.Service.GroupService;
import com.darcklh.louise.Utils.isEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/28 19:43
 * @Description
 */
@RestController
@RequestMapping("saito/group/")
public class GroupController {

    @Autowired
    GroupService groupService;

    @RequestMapping("findBy")
    public Result<GroupRole> findBy() {
        Result<GroupRole> result = new Result<>();
        List<GroupRole> groups = groupService.findGroupRoleBy();
        if (isEmpty.isEmpty(groups)) {
            result.setCode(202);
            return result;
        }
        result.setCode(200);
        result.setMsg("请求成功");
        result.setDatas(groups);
        return result;
    }

    @RequestMapping("add")
    public String add(@RequestBody Group group) {
        String reply = groupService.add(group);
        return "";
    }

    public String edit(FeatureInfo featureInfo) {
        return "";
    }

    public Integer del(Integer feature_id) {
        return 1;
    }

    @RequestMapping("switchStatus")
    public Result switchStatus(@RequestBody JSONObject jsonObject) {
        Result result = new Result();
        result.setMsg(groupService.switchStatus(jsonObject.getLong("group_id")));
        if (result.getMsg().equals("变更状态失败")) {
            result.setCode(201);
        } else {
            result.setCode(200);
        }
        return result;
    }

}
