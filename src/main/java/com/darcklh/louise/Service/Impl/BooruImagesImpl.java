package com.darcklh.louise.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darcklh.louise.Mapper.BooruImagesDao;
import com.darcklh.louise.Model.Louise.BooruImages;
import com.darcklh.louise.Service.BooruImagesService;
import org.springframework.stereotype.Service;

@Service
public class BooruImagesImpl extends ServiceImpl<BooruImagesDao, BooruImages> implements BooruImagesService {
}
