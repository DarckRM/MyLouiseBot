package com.darcklh.louise.Service.Impl;

import com.darcklh.louise.Mapper.FeatureInfoDao;
import com.darcklh.louise.Mapper.FeatureStaticDao;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.FeatureStatic;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Service.FeatureInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        FeatureInfo featureInfo = featureInfoDao.findWithFeatureCmd(feature_cmd);
        if (featureInfo == null)
            throw new ReplyException("[CQ:at,qq=" + user_id + "]未知的命令，请使用!help [页数]命令获取相关帮助");
        return featureInfo;
    }

    @Override
    public List<FeatureInfoMin> findWithRoleId(Integer role_id) {
        return featureInfoDao.findWithRoleId(role_id);
    }

    @Override
    public void addCount(Integer feature_id, long group_id, long user_id) {

        FeatureStatic featureStatic = new FeatureStatic();
        Timestamp now = new Timestamp(new Date().getTime());

        featureStatic.setInvoke_time(now);
        featureStatic.setFeature_id(feature_id);
        featureStatic.setUser_id(user_id);
        featureStatic.setGroup_id(group_id);

        featureStaticDao.insert(featureStatic);
        featureInfoDao.addCount(feature_id);
    }
}
