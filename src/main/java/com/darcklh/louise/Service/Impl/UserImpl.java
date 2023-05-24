package com.darcklh.louise.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darcklh.louise.Mapper.UserDao;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.VO.UserRole;
import com.darcklh.louise.Service.UserService;
import com.darcklh.louise.Utils.DragonflyUtils;
import com.darcklh.louise.Utils.LouiseThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DarckLH
 * @date 2021/8/7 19:14
 * @Description 用户信息相关接口
 */
@Slf4j
@Service
public class UserImpl extends ServiceImpl<UserDao, User> implements UserService {

    @Autowired
    DragonflyUtils dragonflyUtils;
    @Autowired
    UserDao userDao;
    private boolean isUpdate = true;

    private final String userKey = "model:user:id:";

    private final String creditLogKey = "op:credit:id:";

    private int creditEditCount = 0;

    public boolean isUpdate() {
        return this.isUpdate;
    }

    public void update(boolean status) {
        this.isUpdate = status;
    }

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
        // 如果数据未更新则从缓存中取值
        User user;
        // 缓存的值是最新的
        if (isUpdate()) {
            user = dragonflyUtils.get(userKey + user_id, User.class);
            // 如果缓存中没有值则写入
            if (user == null) {
                user = userDao.selectById(user_id);
                dragonflyUtils.set(userKey + user_id, user);
            } else {
                log.debug("用户命中缓存");
            }
        } else {
            user = userDao.selectById(user_id);
            dragonflyUtils.set(userKey + user_id, user);
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
    public int minusCredit(long userId, int credit) {
        // credit 的修改记录进行缓存 达到一定阈值写入数据库
        creditEditCount++;
        String stringCredit = dragonflyUtils.get(creditLogKey + userId);
        int balance = 0;
        if (stringCredit != null) {
            balance = Integer.parseInt(stringCredit);
            balance -= credit;
            dragonflyUtils.set(creditLogKey + userId, String.valueOf(balance));
        } else {
            User user = userDao.selectById(userId);
            balance = user.getCredit() - credit;
            if (balance < 0)
                return balance;
            userDao.minusCredit(credit, userId);
            dragonflyUtils.set(creditLogKey + userId, balance);
            log.info("用户 " + userId + " CREDIT 余额还有 " + balance);
        }

        if (creditEditCount == 30) {
            LouiseThreadPool.execute(() -> {
                log.info("开始 Credit 缓存数据持久化");
                List<String> ids = dragonflyUtils.scan(creditLogKey);
                List<User> users = userDao.selectBatchIds(ids);
                for (User user : users) {
                    userDao.updateCredit(user.getUser_id(), Integer.parseInt(dragonflyUtils.get(creditLogKey + user.getUser_id())));
                }
                dragonflyUtils.remove(creditLogKey);
                creditEditCount = 0;
            });
        }
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
