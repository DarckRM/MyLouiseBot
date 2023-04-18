package com.darcklh.louise.Service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author DarckLH
 * @date 2021/9/16 17:32
 * @Description
 */
public interface PluginInvokeService {

    JSONObject service(String pluginName);

}
