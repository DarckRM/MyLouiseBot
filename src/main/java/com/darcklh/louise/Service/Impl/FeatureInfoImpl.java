package com.darcklh.louise.Service.Impl;

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

    @Autowired
    DragonflyUtils dragonflyUtils;

    private final String featureMinKeyRoleId = "model:feature_min:role_id:";
    private final String featureCountKey = "op:feature_id_count:";
    private final String featureStaticKey = "op:feature_static:";

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

        String reply = "功能<" + featureInfo.getFeature_name() +">修改失败了！";
        if(featureInfoDao.updateById(featureInfo) > 0)
            reply = "功能<" + featureInfo.getFeature_name() +">修改成功！";
        return reply;
    }

    @Override
    public String add(FeatureInfo featureInfo) {

        featureInfo.setIs_enabled(-1);
        String reply = "功能<" + featureInfo.getFeature_name() +">添加失败了！";
        if (featureInfoDao.insert(featureInfo) > 0)
            reply = "功能<" + featureInfo.getFeature_name() +">添加成功！";
        return reply;
    }

    public Integer isEnabled(Integer feature_id) {
        return featureInfoDao.isEnabled(feature_id);
    }

    public String switchStatus(Integer feature_id, String feature_name) {
        String reply = "变更状态失败";
        if (featureInfoDao.switchStatus(feature_id) == 1) {
            reply = isEnabled(feature_id) == 1 ? "功能<"+feature_name+">已启用" : "功能<"+feature_name+">已禁用";
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
            array = dragonflyUtils.get(featureMinKeyRoleId + roleId, JSONArray.class);
            // 如果缓存中没有值则写入
            if (array == null) {
                array = new JSONArray();
                mins = featureInfoDao.findWithRoleId(roleId);
                array.addAll(mins);
                dragonflyUtils.set(featureMinKeyRoleId + roleId, array);
            } else {
                for ( Object min : array)
                    mins.add(JSONObject.parseObject(min.toString(), FeatureInfoMin.class));
                log.debug("用户命中缓存");
            }
        } else {
            array = new JSONArray();
            mins = featureInfoDao.findWithRoleId(roleId);
            array.addAll(mins);
            dragonflyUtils.set(featureMinKeyRoleId + roleId, array);
        }
        return mins;
    }

    @Override
    public List<FeatureInfoMin> findAllMins() {
        return featureInfoDao.findAllMins();
    }

    @Override
    public void addCount(Integer feature_id, long group_id, long user_id) {
        // 缓存操作记数器
        count++;
        String stringCount = dragonflyUtils.get(featureCountKey + feature_id);

        // 从缓存中获取调用功能的计数 featureId:count 并更新缓存
        int featureCount = 1;
        if (stringCount != null)
            featureCount = Integer.parseInt(stringCount) + 1;
        dragonflyUtils.set(featureCountKey + feature_id, featureCount);

        // 向缓存中写入某功能被调用的一条记录
        Timestamp now = new Timestamp(new Date().getTime());
        dragonflyUtils.set(featureStaticKey + count + ":invoke_time", now.toString());
        dragonflyUtils.set(featureStaticKey + count + ":user_id", user_id);
        dragonflyUtils.set(featureStaticKey + count + ":feature_id", feature_id);
        dragonflyUtils.set(featureStaticKey + count + ":group_id", group_id);

        // 达到缓存阈值后将缓存中的记录持久化到数据库
        if (count >= 15) {
            // 新开线程处理 可能会有线程安全问题
            log.info("开始 FeatureStatic 缓存数据持久化");
            HashMap<Integer, Integer> featureCountMap = new HashMap<>();
            FeatureStatic featureStatic;
            for (int cacheCount = 1; cacheCount <= count; cacheCount++) {
                // 从缓存中取出功能调用记录
                featureStatic = new FeatureStatic();
                int featureId = Integer.parseInt(dragonflyUtils.get(featureStaticKey + cacheCount + ":feature_id"));
                Timestamp cacheNow = Timestamp.valueOf(dragonflyUtils.get(featureStaticKey + cacheCount + ":invoke_time"));

                featureStatic.setInvoke_time(cacheNow);
                featureStatic.setFeature_id(featureId);
                featureStatic.setUser_id(Long.parseLong(dragonflyUtils.get(featureStaticKey + cacheCount + ":user_id")));
                featureStatic.setGroup_id(Long.parseLong(dragonflyUtils.get(featureStaticKey + cacheCount + ":group_id")));
                featureStaticDao.insert(featureStatic);

                // 从缓存中取出功能调用计数
                if (featureCountMap.get(featureId) != null)
                    featureCountMap.put(featureId, Integer.parseInt(dragonflyUtils.get(featureCountKey + featureId)));
            }

            for (Map.Entry<Integer, Integer> entry : featureCountMap.entrySet()) {
                featureInfoDao.addCount(entry.getKey(), entry.getValue());
            }
            // 清空缓存
            dragonflyUtils.remove(featureCountKey);
            dragonflyUtils.remove(featureStaticKey);
            count = 0;
        }
    }
}
