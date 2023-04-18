package com.darcklh.louise.Service;

import com.darcklh.louise.Model.Saito.SysConfig;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/20 23:01
 * @Description
 */
@Service
public interface SysConfigService {

    public List<SysConfig> findSysConfigByType(String type);
    public List<SysConfig> findAllSysConfig();
    public Integer edit(List<SysConfig> sysConfigs);

}
