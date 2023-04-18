package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Louise.BooruTags;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author DarckLH
 * @date 2023/3/23 5:54
 * @Description
 */
@Mapper
@Repository
public interface BooruTagsDao extends BaseMapper<BooruTags> {
}
