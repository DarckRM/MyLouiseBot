package com.darcklh.louise.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darcklh.louise.Mapper.PluginInfoDao;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Service.PluginInfoService;
import com.darcklh.louise.Service.PluginService;
import com.darcklh.louise.Utils.PluginManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2022/9/14 1:29
 * @Description
 */
@Slf4j
@Service
public class PluginInfoImpl implements PluginInfoService {

    @Autowired
    PluginInfoDao pluginInfoDao;

    @Autowired
    PluginManager pluginManager;

    @Override
    public List<PluginInfo> findBy() {
        return pluginInfoDao.selectList(null);
    }

    @Override
    public String delBy(Integer id) {
        return null;
    }

    @Override
    public String editBy(PluginInfo object) {
        return null;
    }

    @Override
    public String add(PluginInfo object) {
        return null;
    }

    @Override
    public PluginInfo findByCmd(String cmd) {
        return pluginInfoDao.findByCmd(cmd);
    }

    public boolean reloadPlugin(Integer pluginId) {
        QueryWrapper<PluginInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("plugin_id", pluginId)
                .ne("is_enabled", 0);
        PluginInfo pluginInfo = pluginInfoDao.selectOne(wrapper);

        if (pluginInfo == null)
            return false;

        // 如果已经存在插件 先卸载插件后再安装
        if (PluginManager.pluginInfos.get(pluginId) != null) {
            log.info("卸载已安装的插件");
            PluginManager.pluginInfos.remove(pluginId);
        }
        try {
            pluginManager.loadPlugin(pluginInfo);
        } catch (Exception e) {
            log.info("加载插件失败: " + e.getMessage());
        }
        return true;
    }


    public List<PluginInfo> loadPlugins() {
        QueryWrapper<PluginInfo> wrapper = new QueryWrapper<>();
        wrapper.ne("is_enabled", 0);
        List<PluginInfo> pluginInfos = pluginInfoDao.selectList(wrapper);

        if (pluginInfos == null) {
            log.info("MyLouise 未安装插件");
            return null;
        }

        // 如果已经存在插件 先卸载插件后再安装
        if (PluginManager.pluginInfos.size() != 0) {
            log.info("卸载已安装的插件");
            PluginManager.pluginInfos.clear();
        }
        try {
            pluginManager.loadPlugins(pluginInfos);
        } catch (Exception e) {
            log.info("加载插件失败: " + e.getMessage());
        }
        return pluginInfos;
    }

    @Override
    public String unloadPlugins() {
        log.info("卸载已安装的插件");
        PluginManager.pluginInfos.clear();
        return "已卸载所有插件";
    }

    @Override
    public boolean unloadPlugin(String path) {
        QueryWrapper<PluginInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("path", path);
        PluginInfo pluginInfo = pluginInfoDao.selectOne(wrapper);
        if (pluginInfo == null)
            return true;
        return PluginManager.pluginInfos.remove(pluginInfo.getPlugin_id()) != null;
    }
}
