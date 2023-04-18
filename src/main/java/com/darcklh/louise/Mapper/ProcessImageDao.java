package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Louise.ProcessImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author DarckLH
 * @date 2022/5/21 18:37
 * @Description
 */
@Mapper
@Repository
public interface ProcessImageDao extends BaseMapper<ProcessImage> {

    @Select("SELECT * FROM t_process_image")
    public List<ProcessImage> findAll();

    @Select("TRUNCATE TABLE t_process_image;")
    public Integer removeAllData();

}
