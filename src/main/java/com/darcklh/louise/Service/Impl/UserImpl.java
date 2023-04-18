package com.darcklh.louise.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darcklh.louise.Mapper.RoleDao;
import com.darcklh.louise.Mapper.UserDao;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.VO.UserRole;
import com.darcklh.louise.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.darcklh.louise.Utils.isEmpty.isEmpty;

/**
 * @author DarckLH
 * @date 2021/8/7 19:14
 * @Description 用户信息相关接口
 */
@Slf4j
@Service
public class UserImpl extends ServiceImpl<UserDao, User> implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    RoleDao roleDao;

    public JSONObject joinLouise(long user_id, long group_id) {

        log.info("进入注册流程");
        log.info("用户来自群: " + group_id + " QQ号: " + user_id);
        JSONObject jsonObject = new JSONObject();
        //判断用户是否注册
        long[] users_id = findAllUserID();
        for (long id : users_id)
            if (id == user_id) {
                log.warn("用户 " + user_id + " 已注册");
                throw new ReplyException("你已经注册过了哦");
            }

        //构造Rest请求模板
        RestTemplate restTemplate = new RestTemplate();
        //请求go-cqhhtp的参数和请求头
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //构造请求体
        JSONObject userInfo = new JSONObject();
        userInfo.put("user_id", user_id);
        userInfo.put("group_id", group_id);

        //请求bot获取用户信息
        JSONObject result = JSON.parseObject(restTemplate.postForObject("http://localhost:5700/get_group_member_info", userInfo, String.class));
        log.info(result.toString());

        User user = new User();
        result = result.getJSONObject("data");

        user.setGroup_id(group_id);
        user.setUser_id(user_id);
        user.setNickname(result.getString("nickname"));
        user.setAvatar("https://q1.qlogo.cn/g?b=qq&nk=" + user_id + "&s=640");
        user.setRole_id(2);
        user.setCredit(10000);
        user.setCredit_buff(0);
        user.setCount_setu(0);
        user.setCount_upload(0);
        user.setIsEnabled(1);
        //TODO 没搞懂腾讯返回的时间格式 日后再搞
        //user.setJoin_time(result.getTimestamp("join_time"));

        if (userDao.insert(user) == 0) {
            jsonObject.put("reply", "注册失败了，遗憾！请稍后再试吧");
            log.warn("用户 " + user.getUser_id() + "(" + user.getNickname() + ") 注册失败!");
        }
        else
            jsonObject.put("reply","注册成功了！请输入!help获得进一步帮助");
            log.info("用户 " + user.getUser_id() + "(" + user.getNickname() + ") 注册成功!");
        return jsonObject;
    }

    @Override
    public User selectById(long user_id) {
        User user = userDao.selectById(user_id);
        if (isEmpty(user)) {
            log.warn("用户 " + user_id + " 不存在");
            return null;
        }
        return user;
    }

    /**
     * 根据用户qq查询相关用户信息
     * @param user_id String
     * @return
     */
    @Override
    public JSONObject myInfo(long user_id) {
        return null;
    }

    /**
     * 更新用户某类数据
     * @param user_id String 用户qq
     * @param option String 某个字段
     */
    public void updateCount(long user_id, int option) {

        switch (option) {
            case 1: userDao.updateCountSetu(user_id); return;
            case 2: userDao.updateCountUpload(user_id); return;
        }

    }

    public String banUser(long user_id) {
        String reply = "变更状态失败";
        if (userDao.banUser(user_id) == 1) {
            reply = isUserAvailable(user_id) == 1 ? "用户"+user_id+"已解封" : "用户"+user_id+"已封禁";
        }
        return reply;
    }

    public int isUserAvailable(long user_id) {
        //判断用户是否已注册
        if (userDao.isUserExist(user_id) == 0)
            return 0;
        //判断用户是否启用
        if (userDao.isUserEnabled(user_id) <= 0)
            return -1;
        return 1;
    }

    @Override
    public int minusCredit(long user_id, int credit) {

        User user = userDao.selectById(user_id);
        int balance = user.getCredit() - credit;
        if (balance < 0)
            return balance;
        userDao.minusCredit(credit, user_id);
        log.info("用户 " + user_id + " CREDIT余额还有 " + balance);
        return balance;
    }

    @Override
    public List<UserRole> findAll() {
        return userDao.findBy();
    }

    public long[] findAllUserID() {
        return userDao.findAllUserID();
    }
}
