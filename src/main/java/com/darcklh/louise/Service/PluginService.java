package com.darcklh.louise.Service;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Messages.InMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public interface PluginService {

    String pluginName();

    /**
     * 请求的 inMessage 作为参数调用函数
     * @param inMessage
     * @return
     */
    JSONObject service(InMessage inMessage);
    JSONObject service();

    /**
     * 插件的初始化函数
     * @return boolean represent init status
     */
    public boolean init();

    /**
     * 插件的重载函数
     * @return boolean represent reload status
     */
    public boolean reload();
}
