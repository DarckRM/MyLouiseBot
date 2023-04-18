package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Saito.FeatureStatic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author DarckLH
 * @date 2022/8/14 0:28
 * @Description
 */
@Mapper
@Repository
public interface FeatureStaticDao extends BaseMapper<FeatureStatic> {

    @Select("SELECT * FROM t_feature_static")
    public List<FeatureStatic> findAll();

    @Select("SELECT * FROM t_feature_static WHERE user_id = #{user_id}")
    public List<FeatureStatic> findByUserId(String user_id);

    @Select("SELECT * FROM t_feature_static WHERE group_id = #{group_id")
    public List<FeatureStatic> findByGroupId(String group_id);

}
