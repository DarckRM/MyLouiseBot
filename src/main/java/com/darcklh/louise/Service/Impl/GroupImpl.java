package com.darcklh.louise.Service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Mapper.GroupDao;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.Messages.OutMessage;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.VO.GroupRole;
import com.darcklh.louise.Service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/28 19:48
 * @Description
 */
@Service
public class GroupImpl implements GroupService {

    @Autowired
    GroupDao groupDao;

    @Autowired
    R r;

    public List<GroupRole> findGroupRoleBy() {
        return groupDao.findBy();
    }

    public List<Group> findBy() {
        return groupDao.selectList(null);
    }

    public String delBy(Integer id){return null; };

    public String delByGroup_ID(long group_id) {
        return null;
    }

    public Group selectById(long group_id) {
        return groupDao.selectById(group_id);
    }

    public String editBy(Group group) {
        return null;
    }

    public String add(Group group) {

        String reply = "新增群组失败了";
        // 判断数据库中是否存在群组
        if(groupDao.isGroupExist(group.getGroup_id()) > 0) {
            reply = "群 " + group.getGroup_id() + " 已经注册过了哦";
            return reply;
        }

        group = getGroupFromCqhttp(group);

        if (group == null) {
            reply = "获取群信息失败，注册失败";
            return reply;
        }

        if (groupDao.insert(group) == 1) {
            reply = "新增群组成功";
        }
        return reply;
    }

    public String update(Group group) {
        String reply = "更新群组 " + group.getGroup_id() + " 失败";
        group = getGroupFromCqhttp(group);

        if (group == null) {
            reply = "获取群信息失败，更新失败";
            return reply;
        }

        if (groupDao.updateById(group) == 1) {
            reply = "更新群组 " + group.getGroup_id() + " 成功";
        }
        return reply;
    }

    private Group getGroupFromCqhttp(Group group) {
        // 向Bot请求群聊数据
        JSONObject param = new JSONObject();
        param.put("group_id", group.getGroup_id());
        // 禁用缓存
        param.put("no_cache", "true");
        // TODO 还未解决参数问题
        JSONObject group_info = r.requestAPI("get_group_info", param);
        JSONArray member_array = r.requestAPI("get_group_member_list", param).getJSONArray("data");

        // 获取群主以及管理
        String owner = "";
        StringBuilder admins_builder = new StringBuilder();
        for (Object o: member_array) {
            JSONObject member = (JSONObject) o;
            String user_id = member.getString("user_id");
            String role = member.getString("role");
            if (role.equals("owner")) {
                owner = user_id;
            }
            admins_builder.append(user_id).append(',');
        }
        String admins = admins_builder.toString();

        if(group_info.getJSONObject("data") == null)
            return null;
        JSONObject returnGroup = group_info.getJSONObject("data");

        group.setGroup_name(returnGroup.getString("group_name"));
        group.setMember_count(returnGroup.getString("member_count"));
        group.setGroup_memo(returnGroup.getString("group_memo"));
        group.setGroup_owner(owner);
        group.setGroup_admins(admins_builder.substring(0, admins.length() - 1));
        group.setRole_id(1);
        group.setIs_enabled(1);
        group.setAvatar("http://p.qlogo.cn/gh/" + group.getGroup_id() +"/" + group.getGroup_id() + "/0");

        return group;
    }

    @Override
    public String getGroupAdmin(long group_id) {
        if (isGroupExist(group_id))
            return groupDao.selectById(group_id).getGroup_admins();
        else
            return null;
    }

    @Override
    public String switchStatus(long group_id) {
        String reply = "变更状态失败";
        if (groupDao.switchStatus(group_id) == 1) {
            reply = isGroupEnabled(group_id) ? "群组"+group_id+"已启用" : "群组"+group_id+"已禁用";
        }
        return reply;
    }

    @Override
    public boolean isGroupExist(long group_id) {
        //判断用户是否已注册
        if (groupDao.isGroupExist(group_id) == 0)
            return false;
        return true;
    }

    @Override
    public boolean isGroupEnabled(long group_id) {
        //判断用户是否启用
        if (groupDao.isGroupEnabled(group_id) <= 0)
            return false;
        return true;
    }
}
