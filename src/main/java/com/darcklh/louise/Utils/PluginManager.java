package com.darcklh.louise.Utils;

import com.darcklh.louise.Model.Annotation.LouisePlugin;
import com.darcklh.louise.Model.Annotation.OnCommand;
import com.darcklh.louise.Model.Annotation.OnMessage;
import com.darcklh.louise.Model.Annotation.OnNotice;
import com.darcklh.louise.Model.Enum.Environment;
import com.darcklh.louise.Model.GoCqhttp.NoticePost;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Service.PluginService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件管理类
 */
@Slf4j
@Component
public class PluginManager {

    Logger logger = LoggerFactory.getLogger(PluginManager.class);

    public static final HashMap<Integer, PluginInfo> pluginInfos = new HashMap<>();

    private URLClassLoader urlClassLoader;

    private void loadDefaultPlugins() {
        log.info("加载内置插件");
        String packagePath = "com/darcklh/louise/Plugin";

        URL url = getClass().getClassLoader().getResource(packagePath);
        if (url == null) {
            log.warn("内置插件包不存在: {}", packagePath);
            return;
        }
        File dir = new File(url.getFile());
        if (!dir.exists() || !dir.isDirectory()) {
            log.error("内置插件目录不存在");
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            log.info("内置插件目录为空");
            return;
        }
        int id = -1;
        for (File file : files) {
            if (!file.getName().endsWith(".class")) {
                continue;
            }
            String className = file.getName().substring(0, file.getName().length() - 6);
            try {
                Class<?> plugin = Class.forName(packagePath.replace("/", ".") + "." + className);
                PluginService instance = (PluginService) plugin.getDeclaredConstructor().newInstance();

                if (!plugin.isAnnotationPresent(LouisePlugin.class))
                    continue;

                // 确认插件功能实现对象已被加载后构造内置插件对象
                PluginInfo pluginInfo = new PluginInfo();
                // 所有的内置插件都是可爱的露易丝编写的哦
                pluginInfo.setAuthor("Louise");
                pluginInfo.setClass_name(className);
                pluginInfo.setIs_enabled(1);
                pluginInfo.setName(plugin.getAnnotation(LouisePlugin.class).name());
                pluginInfo.setFeature_id(id);

                handleAnnotations(plugin, pluginInfo);

                // 插件实现对象赋予内置插件对象
                pluginInfo.setPluginService(instance);
                // 外置插件对象的 Id 都是正数
                pluginInfos.put(id, pluginInfo);
                id--;
                log.info("加载内置插件成功: {}", pluginInfo.getName());

            } catch (Exception e) {
                log.error("内置插件加载异常: ", e);
            }
        }
    }

    public void loadPlugin(PluginInfo pluginInfo) throws MalformedURLException, InstantiationException, IllegalAccessException {

        URL[] urls = new URL[1];
        initializing(pluginInfo, urls, 0);
        //将jar文件组成数组 创建URLClassLoader
        urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());

        loadingPlugin(pluginInfo);
    }

    public void loadPlugins(List<PluginInfo> pluginList) throws IOException, IllegalAccessException, InstantiationException {
        init(pluginList);

        for (PluginInfo pluginInfo : pluginList) {
            if (pluginInfo.getIs_enabled() == 0)
                continue;
            loadingPlugin(pluginInfo);
        }
        // 加载内置插件
        loadDefaultPlugins();
    }

    private void loadingPlugin(PluginInfo pluginInfo) throws IllegalAccessException, InstantiationException {
        log.info("执行 [" + pluginInfo.getName() + "---" + pluginInfo.getAuthor() + "] 初始化函数 >>>");
        PluginService pluginService = getInstance(pluginInfo);

        try {
            // 以生产环境为条件初始化插件
            if (pluginService.init(Environment.PROD)) {
                log.info("结束 [" + pluginInfo.getName() + "---" + pluginInfo.getAuthor() + "] 初始化成功 <<<");
            } else
                log.info(pluginInfo.getName() + " 加载失败");
        } catch (NoClassDefFoundError error) {
            log.error("结束 [" + pluginInfo.getName() + "---" + pluginInfo.getAuthor() + "] 初始化失败 <<<");
            log.error(error.getMessage());
            return;
        }
        pluginInfo.setPluginService(pluginService);
        pluginInfos.put(pluginInfo.getPlugin_id(), pluginInfo);
    }

    private void initializing(PluginInfo pluginInfo, URL[] urls, int i) throws MalformedURLException {
        String filePath = pluginInfo.getPath();
        urls[i] = URI.create("jar:file:" + filePath + "!/").toURL();
    }

    private void init(List<PluginInfo> pluginList) throws MalformedURLException {
        int size = pluginList.size();
        URL[] urls = new URL[size];

        for (PluginInfo plugin : pluginList) {
            String filePath = plugin.getPath();
            urls[pluginList.indexOf(plugin)] = URI.create("jar:file:" + filePath + "!/").toURL();
        }

        //将jar文件组成数组 创建URLClassLoader
        urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
    }

    private PluginService getInstance(PluginInfo pluginInfo) throws InstantiationException, IllegalAccessException {
        try {
            Class<?> plugin = urlClassLoader.loadClass(pluginInfo.getClass_name());
            PluginService instance = (PluginService) plugin.getDeclaredConstructor().newInstance();

            if (!plugin.isAnnotationPresent(LouisePlugin.class))
                return instance;

            handleAnnotations(plugin, pluginInfo);

            return instance;
        } catch (ClassNotFoundException e) {
            logger.info("插件 {} 未找到: {}", pluginInfo.getClass_name(), e.getLocalizedMessage());
            return null;
        } catch (NoSuchMethodException | InvocationTargetException e) {
            logger.info("插件 {} 初始化方法异常: {}", pluginInfo.getClass_name(), e.getLocalizedMessage());
            return null;
        }

    }

    // 私有工具方法
    private void handleAnnotations(Class<?> plugin, PluginInfo pluginInfo) {
        // 前缀
        String prefix = plugin.getAnnotation(LouisePlugin.class).prefix();

        // 处理 OnCommand, OnMessage 注解的方法 对注解的插件进行反射处理 命令注入 方法实例化
        for (Method m : plugin.getDeclaredMethods()) {
            // 命令式方法
            if (m.isAnnotationPresent(OnCommand.class))
                onCommand(m, pluginInfo, prefix, plugin.getName());
            // 响应式方法
            if (m.isAnnotationPresent(OnMessage.class))
                onMessage(m, pluginInfo, plugin.getName());
            // 通知类型方法
            if (m.isAnnotationPresent(OnNotice.class))
                onNotice(m, pluginInfo, plugin.getName());
        }
    }

    private void onCommand(Method m, PluginInfo pluginInfo, String prefix, String pluginName) {
        // 获取该方法的MyAnnotation注解实例
        OnCommand annotation = m.getAnnotation(OnCommand.class);
        for (String command : annotation.commands()) {
            // 校验命令
            if (command.length() > 12) {
                log.info(pluginName + "." + m.getName() + ":" + command + " 命令过长 已略过");
                continue;
            }

            if (command.length() == 0) {
                log.info(pluginName + "." + m.getName() + ":" + command + " 命令非法 已略过");
                continue;
            }
            pluginInfo.getCommandsMap().put(prefix + " " + command, m);
        }
    }

    private void onMessage(Method m, PluginInfo pluginInfo, String pluginName) {
        // 获取该方法的MyAnnotation注解实例
        OnMessage annotation = m.getAnnotation(OnMessage.class);
        for (String message : annotation.messages()) {
            // 校验命令
            if (message.length() > 64) {
                log.info(pluginName + "." + m.getName() + ":" + message + " 表达式过长 已略过");
                continue;
            }

            if (message.length() == 0) {
                log.info(pluginName + "." + m.getName() + ":" + message + " 命令非法 已略过");
                continue;
            }
            pluginInfo.getMessagesMap().put(message, m);
        }
    }

    private void onNotice(Method m, PluginInfo pluginInfo, String pluginName) {
        OnNotice annotation = m.getAnnotation(OnNotice.class);
        for (NoticePost.NoticeType notice : annotation.notices()) {
            pluginInfo.getNoticesMap().put(notice, m);
        }
    }
}
