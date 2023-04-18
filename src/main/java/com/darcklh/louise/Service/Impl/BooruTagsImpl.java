package com.darcklh.louise.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darcklh.louise.Mapper.BooruTagsDao;
import com.darcklh.louise.Model.Louise.BooruTags;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Service.BooruTagsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2023/3/23 5:57
 * @Description
 */
@Service
@Slf4j
public class BooruTagsImpl extends ServiceImpl<BooruTagsDao, BooruTags> implements BooruTagsService {

    @Autowired
    private BooruTagsDao booruTagsDao;

    @Override
    public List<BooruTags> findBy(BooruTags booruTags) {
        QueryWrapper<BooruTags> wrapper = new QueryWrapper<>(booruTags);
        wrapper.isNull("alter_name");
        return booruTagsDao.selectList(wrapper);
    }

    @Override
    public List<BooruTags> findByAlter(BooruTags booruTags) {
        QueryWrapper<BooruTags> wrapper = new QueryWrapper<>(booruTags);
        return booruTagsDao.selectList(wrapper);
    }

    @Override
    public boolean saveAlter(BooruTags booruTags) {
        // 校验是否存在其它词条与之对应
        QueryWrapper<BooruTags> wrapper = new QueryWrapper<>(booruTags);
        wrapper.eq("cn_name", booruTags.getCn_name());
        wrapper.eq("alter_name", booruTags.getAlter_name());
        if(booruTagsDao.selectList(wrapper).size() != 0) {
            log.info("已存在 " + booruTags.getOrigin_name() + " -> " + booruTags.getCn_name() + " -> " + booruTags.getAlter_name() + " 的记录");
            throw new ReplyException("已存在 " + booruTags.getOrigin_name() + " -> " + booruTags.getCn_name() + " -> " + booruTags.getAlter_name() + " 的记录 (´д`)");
        }
        return booruTagsDao.insert(booruTags) == 1;
    }
}
