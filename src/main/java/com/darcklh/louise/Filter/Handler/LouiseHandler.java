package com.darcklh.louise.Filter.Handler;

import com.alibaba.fastjson.JSON;
import com.darcklh.louise.Filter.InvokeValidator;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Service.*;
import com.darcklh.louise.Utils.HttpServletWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author DarckLH
 * @date 2021/8/12 12:41
 * @Description
 */
@Component
public class LouiseHandler implements HandlerInterceptor {
    Logger logger = LoggerFactory.getLogger(LouiseHandler.class);
    @Autowired
    FeatureInfoService featureInfoService;
    @Autowired
    PluginInfoService pluginInfoService;
    @Autowired
    InvokeValidator validator;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object o) throws Exception {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //获取相关信息
        HttpServletWrapper wrapper = new HttpServletWrapper(request);
        String body = wrapper.getBody();
        logger.debug("拦截器请求Body: {}", body);

        Message inMessage = JSON.parseObject(body).toJavaObject(Message.class);

        long userId = inMessage.getUser_id();
        String message = inMessage.getRaw_message();

        //对command预处理
        String[] commands = message.split(" ");
        String command = commands[0];

        // 如果参数是 command/{param} 的形式，那么模糊匹配左大括号后半部分
        if (command.contains("/"))
            command = command.substring(0, command.indexOf("/") + 1) + "{%";
        else
            // 如果不携带参数，那么构造命令是否允许无参请求查询条件
            command += " %";

        // 获取请求的功能对象
        FeatureInfo featureInfo = featureInfoService.findWithFeatureCmd(command, userId);
        if (featureInfo == null) {
            throw new ReplyException("未知的命令 " + command);
        }
        //判断功能是否启用
        if (featureInfo.getIs_enabled() != 1) {
            throw new ReplyException("功能<" + featureInfo.getFeature_name() + ">未启用");
        }

        // 如果是请求插件类功能且不是转发请求则进行转发
        if (featureInfo.getType() == 1 && !request.getRequestURI().contains("/louise/invoke/")) {
            PluginInfo pluginInfo = pluginInfoService.findByCmd(command);
            request.getRequestDispatcher("invoke/" + pluginInfo.getPlugin_id()).forward(request, response);
            return false;
        }

        Message messageObj = new Message(inMessage);
        return validator.valid(messageObj, featureInfo);

    }

    @Override
    public void postHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Object o, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Object o, Exception e) {
    }

}
