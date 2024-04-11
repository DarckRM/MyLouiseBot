package com.darcklh.louise.Service;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Enum.Environment;
import com.darcklh.louise.Model.Messages.Message;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public interface PluginService {

    /**
     * @return String
     */
    String pluginName();

    /**
     * 请求的 inMessage 作为参数调用函数
     * @param message Message
     * @return JSONObject
     */
    @Deprecated(since = "大部分方法使用反射的机制去调用，不再需要此方法")
    JSONObject service(Message message);

    /**
     * 提供一个无参调用的入口
     * @return JSONObject
     */
    JSONObject service();
    /**
     * 插件的初始化函数
     * @return boolean represent init status
     */
    public boolean init(Environment env);

    /**
     * 插件的重载函数
     * @return boolean represent reload status
     */
    public boolean reload();
}
