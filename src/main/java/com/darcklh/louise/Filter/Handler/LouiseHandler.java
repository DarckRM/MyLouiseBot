package com.darcklh.louise.Filter.Handler;
import com.alibaba.fastjson.JSON;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Service.*;
import com.darcklh.louise.Utils.DragonflyUtils;
import com.darcklh.louise.Utils.HttpServletWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author DarckLH
 * @date 2021/8/12 12:41
 * @Description
 */
@Component
public class LouiseHandler implements HandlerInterceptor {

    Logger logger = LoggerFactory.getLogger(LouiseHandler.class);

    // 控制用户请求时间间隔
    Map<Long, Map<Integer , Long>> userReqLog = new HashMap<>();

    @Autowired
    DragonflyUtils dragonflyUtils;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;
    @Autowired
    FeatureInfoService featureInfoService;

    @Autowired
    PluginInfoService pluginInfoService;
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object o) throws Exception {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //获取相关信息
        HttpServletWrapper wrapper = new HttpServletWrapper(request);
        String body = wrapper.getBody();
        logger.debug("拦截器请求Body: {}", body);

        InMessage inMessage = JSON.parseObject(body).toJavaObject(InMessage.class);

        long groupId = inMessage.getGroup_id();
        String nickname = inMessage.getSender().getNickname();
        long userId = inMessage.getUser_id();
        String message = inMessage.getMessage();
        String userInfo = nickname + "(" + userId + ")";

        //对command预处理
        String[] commands = message.split(" ");
        String command = commands[0];

        // 如果参数是 command/{param} 的形式，那么模糊匹配左大括号后半部分
        if (command.contains("/"))
            command = command.substring(0, command.indexOf("/") + 1) + "{%";
        else
        // 如果不携带参数，那么构造命令是否允许无参请求查询条件
            command += " %";

        boolean tag = false;

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

        logger.info("用户 {} 请求 {} at {}", userInfo, featureInfo.getFeature_name(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
        // 管理员和 Bot 的命令将不受限制
        if (userId == Long.parseLong(LouiseConfig.BOT_ACCOUNT)) {
            featureInfoService.addCount(featureInfo.getFeature_id(), groupId, userId);
            return true;
        }
        requestLimitCheck(featureInfo, inMessage);
        howMuchCost("| 请求限制校验耗时 {} 毫秒", stopWatch);
        // 放行不需要鉴权的命令
        if (featureInfo.getIs_auth() == 0) {
            // 更新调用统计数据
            featureInfoService.addCount(featureInfo.getFeature_id(), groupId, userId);
            return true;
        }
        User user = userService.selectById(userId);
        // 判断用户是否存在并启用
        if (user == null) {
            logger.info("未登记的用户 {}", userInfo);
            throw new ReplyException("请在群内发送!join以启用你的使用权限");
        } else if (user.getIsEnabled() != 1) {
            logger.info("未启用的用户 {}", userInfo);
            throw new ReplyException("你的权限已被暂时禁用");
        }

        // 判断群聊还是私聊
        if (inMessage.getGroup_id() != -1) {
            Group group = groupService.selectById(groupId);
            if (group != null) {
                if (group.getIs_enabled() != 1) {
                    logger.info("未启用的群组: {}", groupId);
                    throw new ReplyException("主人不准露易丝在这个群里说话哦");
                }
            } else {
                logger.info("未注册的群组: {}", groupId);
                throw new ReplyException("群聊还没有在露易丝中注册哦");
            }

            List<FeatureInfoMin> featureInfoMins = featureInfoService.findWithRoleId(group.getRole_id());
            logger.debug("| 群聊允许的功能列表: {}", formatList(featureInfoMins));
            for ( FeatureInfoMin featureInfoMin: featureInfoMins) {
                if (featureInfoMin.getFeature_id().equals(featureInfo.getFeature_id())) {
                    tag = true;
                    break;
                }
            }
            if (!tag)
                throw new ReplyException("这个群聊的权限不准用这个功能哦");
        }

        tag = false;

        List<FeatureInfoMin> featureInfoMins = featureInfoService.findWithRoleId(user.getRole_id());
        logger.debug("| 用户允许的功能列表: {}", formatList(featureInfoMins));
        for ( FeatureInfoMin featureInfoMin: featureInfoMins) {
            if (featureInfoMin.getFeature_id().equals(featureInfo.getFeature_id())) {
                tag = true;
                break;
            }
        }
        if (!tag)
            throw new ReplyException("你的权限还不准用这个功能哦");

        //合法性校验通过 扣除CREDIT
        int credit = userService.minusCredit(userId, featureInfo.getCredit_cost());
        if (credit < 0) {
            throw new ReplyException("你的CREDIT余额不足哦");
        }
        howMuchCost("| 请求鉴权耗时 {} 毫秒", stopWatch);
        // 更新调用统计数据
        featureInfoService.addCount(featureInfo.getFeature_id(), groupId, userId);

        logger.info("| 功能 {} 消耗用户 {} CREDIT {}", featureInfo.getFeature_name(), userInfo, featureInfo.getCredit_cost());
        howMuchCost("└ 解析此次请求耗时 {} 毫秒", stopWatch);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }

    private String formatList(List<FeatureInfoMin> list) {
        StringBuilder result = new StringBuilder();
        for ( FeatureInfoMin min : list) {
            result.append(min.getFeature_id()).append(":").append(min.getFeature_name()).append("; ");
        }
        result.append("]");
        return result.toString();
    }

    private void howMuchCost(String prompt, StopWatch stopWatch) {
        stopWatch.stop();
        logger.info(prompt, stopWatch.getTotalTimeMillis());
        stopWatch.start();
    }

    private void requestLimitCheck(FeatureInfo featureInfo, InMessage inMessage) {
        // 更新 Interval 控制
        long now = new Date().getTime();
        int featureId = featureInfo.getFeature_id();
        Map<Integer, Long> reqLogs = userReqLog.get(inMessage.getUser_id());
        if (reqLogs != null) {
            Long lastReq = reqLogs.get(featureId);
            if(null != lastReq) {
                long interval = now - lastReq;
                long reqLimit = featureInfo.getInvoke_limit() * 1000L;
                if (interval < reqLimit)
                    throw new ReplyException("[CQ:at,qq=" + inMessage.getUser_id() + "] 此功能还有 " + (reqLimit - interval) / 1000 + " 秒冷却");
                else
                    reqLogs.put(featureId, now);
            } else
                reqLogs.put(featureId, now);
        } else {
            Map<Integer, Long> userMap = new HashMap<>();
            userMap.put(featureId, now);
            userReqLog.put(inMessage.getUser_id(), userMap);
        }
    }
}
