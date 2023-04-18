package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/28 15:43
 * @Description
 */
@Mapper
@Repository
public interface FeatureInfoDao extends BaseMapper<FeatureInfo> {

    @Select("SELECT t2.feature_id, t2.feature_name FROM t_role_feature t1 LEFT JOIN t_feature_info t2 ON t2.feature_id = t1.feature_id WHERE role_id = #{role_id}")
    public List<FeatureInfoMin> findWithRoleId(Integer role_id);

    @Select("SELECT * FROM t_feature_info WHERE feature_url = #{feature_url}")
    public FeatureInfo findWithFeatureURL(String feature_url);

    @Select("SELECT * FROM t_feature_info WHERE feature_cmd LIKE #{feature_cmd}")
    public FeatureInfo findWithFeatureCmd(String feature_cmd);

    @Update("UPDATE t_feature_info SET is_enabled = -is_enabled WHERE feature_id = #{feature_id}")
    public Integer switchStatus(Integer feature_id);

    @Select("SELECT is_enabled FROM t_feature_info WHERE feature_id = #{feature_id}")
    public Integer isEnabled(Integer feature_id);

    @Select("UPDATE t_feature_info SET count = count + 1 WHERE feature_id = #{feature_id}")
    public Integer addCount(Integer feature_id);
}
