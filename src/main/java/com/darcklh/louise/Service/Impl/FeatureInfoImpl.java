package com.darcklh.louise.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Mapper.FeatureInfoDao;
import com.darcklh.louise.Mapper.FeatureStaticDao;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.FeatureStatic;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Service.FeatureInfoService;
import com.darcklh.louise.Utils.DragonflyUtils;
import com.darcklh.louise.Utils.LouiseThreadPool;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author DarckLH
 * @date 2021/9/28 15:43
 * @Description
 */
@Service
@Slf4j
public class FeatureInfoImpl implements FeatureInfoService {

    @Autowired
    FeatureInfoDao featureInfoDao;

    @Autowired
    FeatureStaticDao featureStaticDao;

    DragonflyUtils dragon = DragonflyUtils.getInstance();

    private final String FEATURE_MIN_KEY_ROLE_ID = "model:feature_min:role_id:";
    private final String FEATURE_COUNT_KEY = "op:feature_id_count:";
    private final String FEATURE_STATIC_KEY = "op:feature_static:";

    private int count = 0;

    private boolean isUpdate = true;

    public boolean isUpdate() {
        return this.isUpdate;
    }

    public void update(boolean status) {
        this.isUpdate = status;
    }

    @Override
    public List<FeatureInfo> findBy() {
        return featureInfoDao.selectList(null);
    }

    @Override
    public String delBy(Integer feature_id) {
        return null;
    }

    @Override
    public String editBy(FeatureInfo featureInfo) {

        String reply = "功能<" + featureInfo.getFeature_name() + ">修改失败了！";
        if (featureInfoDao.updateById(featureInfo) > 0)
            reply = "功能<" + featureInfo.getFeature_name() + ">修改成功！";
        return reply;
    }

    @Override
    public String add(FeatureInfo featureInfo) {

        featureInfo.setIs_enabled(-1);
        String reply = "功能<" + featureInfo.getFeature_name() + ">添加失败了！";
        if (featureInfoDao.insert(featureInfo) > 0)
            reply = "功能<" + featureInfo.getFeature_name() + ">添加成功！";
        return reply;
    }

    public Integer isEnabled(Integer feature_id) {
        return featureInfoDao.isEnabled(feature_id);
    }

    public String switchStatus(Integer feature_id, String feature_name) {
        String reply = "变更状态失败";
        if (featureInfoDao.switchStatus(feature_id) == 1) {
            reply = isEnabled(feature_id) == 1 ? "功能<" + feature_name + ">已启用" : "功能<" + feature_name + ">已禁用";
        }
        return reply;
    }

    @Override
    public FeatureInfo findWithFeatureURL(String feature_url) {
        return featureInfoDao.findWithFeatureURL(feature_url);
    }

    @Override
    public FeatureInfo findWithFeatureCmd(String feature_cmd, long user_id) {
        return featureInfoDao.findWithFeatureCmd(feature_cmd);
    }

    @Override
    public List<FeatureInfoMin> findWithRoleId(Integer roleId) {
        // 如果数据未更新则从缓存中取值
        JSONArray array;
        List<FeatureInfoMin> mins = new ArrayList<>();
        // 缓存的值是最新的
        if (isUpdate()) {
            array = dragon.get(FEATURE_MIN_KEY_ROLE_ID + roleId, JSONArray.class);
            // 如果缓存中没有值则写入
            if (array == null) {
                array = new JSONArray();
                mins = featureInfoDao.findWithRoleId(roleId);
                array.addAll(mins);
                dragon.set(FEATURE_MIN_KEY_ROLE_ID + roleId, array);
            } else {
                for (Object min : array)
                    mins.add(JSONObject.parseObject(min.toString(), FeatureInfoMin.class));
                log.debug("用户命中缓存");
            }
        } else {
            array = new JSONArray();
            mins = featureInfoDao.findWithRoleId(roleId);
            array.addAll(mins);
            dragon.set(FEATURE_MIN_KEY_ROLE_ID + roleId, array);
        }
        return mins;
    }

    @Override
    public List<FeatureInfoMin> findAllMins() {
        return featureInfoDao.findAllMins();
    }

    @Override
    public void addCount(Integer feature_id, long group_id, long user_id) {
        // 全局命令调用计数器
        int count = dragon.length(FEATURE_STATIC_KEY);
        // 缓存操作记数器
        String stringCount = dragon.get(FEATURE_COUNT_KEY + feature_id);

        // 从缓存中获取调用功能的计数 featureId:count 并更新缓存
        int featureCount = 1;
        if (stringCount != null)
            featureCount = Integer.parseInt(stringCount) + 1;
        dragon.set(FEATURE_COUNT_KEY + feature_id, featureCount);

        // 向缓存中追加某功能被调用的一条记录
        Timestamp now = new Timestamp(new Date().getTime());
        JSONObject feature_json = new JSONObject();
        feature_json.put("invoke_time", now.toString());
        feature_json.put("user_id", user_id);
        feature_json.put("feature_id", feature_id);
        feature_json.put("group_id", group_id);
        dragon.lpush(FEATURE_STATIC_KEY, feature_json);
        // 达到缓存阈值后将缓存中的记录持久化到数据库
        if (count >= 15) {
            // 新开线程处理 可能会有线程安全问题
            log.info("开始 FeatureStatic 缓存数据log");
            HashMap<Integer, Integer> featureCountMap = new HashMap<>();
            FeatureStatic featureStatic;
            Function<List<String>, List<JSONObject>> h = values -> {
                List<JSONObject> return_list = new ArrayList<>();
                values.forEach(value -> return_list.add(JSONObject.parseObject(value)));
                return return_list;
            };
            for (JSONObject one : dragon.list(FEATURE_STATIC_KEY, h)) {
                 // 从缓存中取出功能调用记录
                int featureId = one.getInteger("feature_id");
                Timestamp cacheNow = Timestamp.valueOf(one.getString("invoke_time"));
                featureStatic = new FeatureStatic(one.getInteger("user_id"), one.getInteger("group_id"), one.getInteger("feature_id"), cacheNow);
                log.info("写入" + featureStatic.getFeature_id() + "中");
                featureStaticDao.insert(featureStatic);

                // 从缓存中取出功能调用计数
                if (featureCountMap.get(featureId) != null)
                    featureCountMap.put(featureId, Integer.parseInt(dragon.get(FEATURE_COUNT_KEY + featureId)));
            }
            for (Map.Entry<Integer, Integer> entry : featureCountMap.entrySet()) {
                featureInfoDao.addCount(entry.getKey(), entry.getValue());
            }
            // 清空缓存
            dragon.remove(FEATURE_COUNT_KEY);
            dragon.remove(FEATURE_STATIC_KEY);
        }
    }
}