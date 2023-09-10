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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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

    public void loadPlugin(PluginInfo pluginInfo) throws MalformedURLException, InstantiationException, IllegalAccessException {

        URL[] urls = new URL[1];
        initializing(pluginInfo, urls, 0);
        //将jar文件组成数组 创建URLClassLoader
        urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
        loadingPlugin(pluginInfo);
    }

    public void loadPlugins(List<PluginInfo> pluginList) throws IOException, IllegalAccessException, InstantiationException {
        init(pluginList);
        for(PluginInfo pluginInfo: pluginList)
            loadingPlugin(pluginInfo);
    }

    private void loadingPlugin(PluginInfo pluginInfo) throws IllegalAccessException, InstantiationException {
        log.info("执行 [" + pluginInfo.getName() + "---" + pluginInfo.getAuthor() +"] 初始化函数 >>>");
        PluginService pluginService = getInstance(pluginInfo);

        try {
            // 以生产环境为条件初始化插件
            if(pluginService.init(Environment.PROD)) {
                log.info("结束 [" + pluginInfo.getName() + "---" + pluginInfo.getAuthor() +"] 初始化成功 <<<");
            } else
                log.info(pluginInfo.getName() + " 加载失败");
        } catch (NoClassDefFoundError error) {
            log.error("结束 [" + pluginInfo.getName() + "---" + pluginInfo.getAuthor() +"] 初始化失败 <<<");
            log.error(error.getMessage());
            return;
        }
        pluginInfo.setPluginService(pluginService);
        pluginInfos.put(pluginInfo.getPlugin_id(), pluginInfo);
    }

    private void initializing(PluginInfo pluginInfo, URL[] urls, int i) throws MalformedURLException {
        String filePath = pluginInfo.getPath();
        urls[i] = new URL("jar:file:" +filePath+ "!/");
    }

    private void init(List<PluginInfo> pluginList) throws MalformedURLException {
        int size = pluginList.size();
        URL[] urls = new URL[size];

        for (int i = 0; i < size; i++)
            initializing(pluginList.get(i), urls, i);

        //将jar文件组成数组 创建URLClassLoader
        urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
    }

    public PluginService getInstance(PluginInfo pluginInfo) throws InstantiationException,IllegalAccessException {
        try {
            Class<?> plugin = urlClassLoader.loadClass(pluginInfo.getClass_name());
            PluginService instance = (PluginService) plugin.newInstance();

            // 处理 OnCommand, OnMessage 注解的方法 对注解的插件进行反射处理 命令注入 方法实例化
            if (plugin.isAnnotationPresent(LouisePlugin.class)) {
                // 前缀
                String prefix = plugin.getAnnotation(LouisePlugin.class).prefix();

                for (Method m : plugin.getDeclaredMethods()) {
                    // 命令式方法
                    if (m.isAnnotationPresent(OnCommand.class)) {
                        // 获取该方法的MyAnnotation注解实例
                        OnCommand annotation = m.getAnnotation(OnCommand.class);
                        for (String command : annotation.commands()) {
                            // 校验命令
                            if ( command.length() > 12 ) {
                                log.info(plugin.getName() + "." + m.getName() + ":" + command + " 命令过长 已略过");
                                continue;
                            }

                            if ( command.length() == 0) {
                                log.info(plugin.getName() + "." + m.getName() + ":" + command + " 命令非法 已略过");
                                continue;
                            }
                            pluginInfo.getCommandsMap().put(prefix + " " + command, m);
                        }
                    }
                    // 响应式方法
                    if (m.isAnnotationPresent(OnMessage.class)) {
                        // 获取该方法的MyAnnotation注解实例
                        OnMessage annotation = m.getAnnotation(OnMessage.class);
                        for (String message : annotation.messages()) {
                            // 校验命令
                            if ( message.length() > 64 ) {
                                log.info(plugin.getName() + "." + m.getName() + ":" + message + " 表达式过长 已略过");
                                continue;
                            }

                            if ( message.length() == 0) {
                                log.info(plugin.getName() + "." + m.getName() + ":" + message + " 命令非法 已略过");
                                continue;
                            }
                            pluginInfo.getMessagesMap().put(message, m);
                        }
                    }
                    // 通知类型方法
                    if (m.isAnnotationPresent(OnNotice.class)) {
                        OnNotice annotation = m.getAnnotation(OnNotice.class);
                        for (NoticePost.NoticeType notice : annotation.notices()) {
                            pluginInfo.getNoticesMap().put(notice, m);
                        }
                    }
                }
            }
            return instance;
        } catch (ClassNotFoundException e) {
            logger.info("插件 {} 未找到，加载失败", pluginInfo.getClass_name());
            return null;
        }

    }
}
