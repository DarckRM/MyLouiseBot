package com.darcklh.louise.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.darcklh.louise.Model.Louise.BooruTags;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2023/3/23 5:55
 * @Description
 */
@Service
public interface BooruTagsService extends IService<BooruTags> {
    public List<BooruTags> findBy(BooruTags booruTags);
    public List<BooruTags> findByAlter(BooruTags booruTags);
    public boolean saveAlter(BooruTags booruTags);
}
