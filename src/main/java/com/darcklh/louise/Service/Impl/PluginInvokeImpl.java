package com.darcklh.louise.Service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Service.PluginInvokeService;
import com.darcklh.louise.Service.PluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 插件接口实现类
 */
@Service
public class PluginInvokeImpl implements PluginInvokeService {

    private final Map<String, PluginService> map;

    @Autowired
    public PluginInvokeImpl(List<PluginService> pluginServiceList) {
        map = pluginServiceList.stream().collect(Collectors.toMap(PluginService::pluginName, Function.identity(), (oldV, newV) -> newV));
    }

    @Override
    public JSONObject service(String pluginName) {
        PluginService pluginService = map.get(pluginName);
        JSONObject jsonObject = new JSONObject();
        if (pluginService == null) {
            return jsonObject;
        }
        return pluginService.service();
    }
}
