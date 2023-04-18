package com.darcklh.louise.Service;

import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.VO.FeatureInfoMin;

import java.util.List;


/**
 * @author DarckLH
 * @date 2021/9/28 15:40
 * @Description
 */
public interface FeatureInfoService extends BaseService<FeatureInfo>{
    public String switchStatus(Integer feature_id, String feature_name);
    public FeatureInfo findWithFeatureURL(String feature_url);
    public FeatureInfo findWithFeatureCmd(String feature_cmd, long user_id);
    public List<FeatureInfoMin> findWithRoleId(Integer role_id);
    public void addCount(Integer feature_id, long group_id, long user_id);
}
