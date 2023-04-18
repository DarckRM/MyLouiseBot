package com.darcklh.louise.Service;

import com.darcklh.louise.Model.Saito.PluginInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2022/9/14 1:28
 * @Description
 */
public interface PluginInfoService extends BaseService<PluginInfo>{
    public PluginInfo findByCmd(String cmd);
    public List<PluginInfo> loadPlugins();
    public boolean reloadPlugin(Integer pluginId);
    public String unloadPlugins();
    public boolean unloadPlugin(String path);
}
