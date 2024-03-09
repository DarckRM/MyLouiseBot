package com.darcklh.louise.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.VO.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService extends IService<User> {

    public List<UserRole> findAll();
    public JSONObject joinLouise(long user_id, long group_id);
    public JSONObject myInfo(long user_id);
    public void updateCount(long user_id, int option);
    public String banUser(long user_id);
    public int isUserAvailable(long user_id);
    public User selectById(long user_id);
    public int minusCredit(long user_id, int credit);

}
