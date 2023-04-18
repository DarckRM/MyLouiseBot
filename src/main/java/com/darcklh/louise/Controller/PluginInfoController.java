package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Service.PluginInfoService;
import com.darcklh.louise.Utils.isEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author DarckLH
 * @date 2022/10/22 18:27
 * @Description
 */
@RestController
@Slf4j
@RequestMapping("saito/plugin-info")
public class PluginInfoController {

    @Autowired
    PluginInfoService pluginInfoService;

    @GetMapping("/loadPlugins")
    public Result<PluginInfo> loadPlugins() {
        Result<PluginInfo> result = new Result<>();
        List<PluginInfo> pluginInfoList = pluginInfoService.loadPlugins();
        if (pluginInfoList.size() != 0) {
            result.setCode(200);
            result.setMsg("成功加载了 " + pluginInfoList.size() + " 个插件");
            result.setDatas(pluginInfoList);
        } else {
            result.setCode(200);
            result.setMsg("未安装任何插件");
        }
        return result;
    }

    @GetMapping("/findBy")
    public Result<PluginInfo> findBy() {
        Result<PluginInfo> result = new Result<>();
        List<PluginInfo> pluginInfos = pluginInfoService.findBy();
        if (isEmpty.isEmpty(pluginInfos)) {
            result.setCode(202);
            return result;
        }
        result.setCode(200);
        result.setMsg("请求成功");
        result.setDatas(pluginInfos);
        return result;
    }

    /**
     * 指定 ID 重载插件
     * @param pluginId
     * @return
     */
    @GetMapping("/reload/{pluginId}")
    public Result<String> reloadSpecificPlugin(@PathVariable Integer pluginId) {
        Result<String> result = new Result<>();

        if (!pluginInfoService.reloadPlugin(pluginId)) {
            result.setCode(202);
            result.setMsg("插件重启失败");
            return result;
        }
        result.setCode(200);
        result.setMsg("插件重启成功");
        return result;
    }

    /**
     * 指定 ID 修改插件是否装载
     * @param pluginId
     * @return
     */
    @GetMapping("/switchStatus")
    public Result<String> switchStatus(@RequestParam("plugin_id") Integer pluginId) {
        return null;
    }

    // 手动卸载所有插件
    @PostMapping("/unloadPlugins")
    public Result<String> unloadPlugins() {
        Result<String> result = new Result<>();
        result.setMsg(pluginInfoService.unloadPlugins());
        return result;
    }

}
