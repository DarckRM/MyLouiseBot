package com.darcklh.louise.Filter;

import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Louise.Group;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Model.Saito.PluginInfo;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import com.darcklh.louise.Service.FeatureInfoService;
import com.darcklh.louise.Service.GroupService;
import com.darcklh.louise.Service.UserService;
import com.darcklh.louise.Utils.DragonflyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InvokeValidator {
    @Autowired
    FeatureInfoService featureInfoService;
    @Autowired
    GroupService groupService;
    @Autowired
    UserService userService;
    private final Map<Long, Map<Integer, Long>> userReqLog = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(InvokeValidator.class);


    public boolean valid(Message message, FeatureInfo feature) {

        if (feature == null) {
            log.warn("功能对象为空");
            return false;
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String userInfo = message.getSender().getNickname() + "(" + message.getUser_id() + ")";
        // 权限校验
        boolean tag = false;

        long userId = message.getUser_id();
        long groupId = message.getGroup_id();
        log.info("用户 {} 请求 {}", userInfo, feature.getFeature_name());

        // 管理员和 Bot 的命令将不受限制
        if (userId == Long.parseLong(LouiseConfig.BOT_ACCOUNT)) {
            featureInfoService.addCount(feature.getFeature_id(), groupId, userId);
            return true;
        }

        // 更新 Interval 控制
        long now = new Date().getTime();
        int featureId = feature.getFeature_id();

        Map<Integer, Long> reqLogs = userReqLog.get(message.getUser_id());
        if (reqLogs != null) {
            Long lastReq = reqLogs.get(featureId);
            if (null != lastReq) {
                long interval = now - lastReq;
                long reqLimit = feature.getInvoke_limit() * 1000L;
                if (interval < reqLimit) {
                    message.reply().at().text("此功能还有 " + (reqLimit - interval) / 1000 + " 秒冷却").send();
                    return false;
                } else
                    reqLogs.put(featureId, now);
            } else
                reqLogs.put(featureId, now);
        } else {
            Map<Integer, Long> userMap = new HashMap<>();
            userMap.put(featureId, now);
            userReqLog.put(message.getUser_id(), userMap);
        }
        howMuchCost("| 请求限制校验耗时 {} 毫秒", stopWatch);
        // 放行不需要鉴权的命令
        if (feature.getIs_auth() == 0) {
            // 更新调用统计数据
            featureInfoService.addCount(feature.getFeature_id(), groupId, userId);
            howMuchCost("└ 解析此次请求耗时 {} 毫秒", stopWatch);
            return true;
        }
        User user = userService.selectById(userId);
        // 判断用户是否存在并启用
        if (user == null) {
            log.info("未登记的用户 {}", userInfo);
            message.reply().at().text("请在群内发送 !join 以启用你的使用权限").send();
            return false;
        } else if (user.getIsEnabled() != 1) {
            log.info("未启用的用户 {}", userInfo);
            message.reply().at().text("你的权限已被暂时禁用").send();
            return false;
        }

        // 判断群聊还是私聊
        if (message.getGroup_id() != -1) {
            Group group = groupService.selectById(groupId);
            if (group == null) {
                log.info("未注册的群组: {}", groupId);
                message.reply().at().text("群聊还没有在露易丝中注册哦").send();
                return false;
            }

            if (group.getIs_enabled() != 1) {
                log.info("未启用的群组: {}", groupId);
                message.reply().at().text("主人不准露易丝在这个群里说话哦").send();
                return false;
            }

            List<FeatureInfoMin> featureInfoMins = featureInfoService.findWithRoleId(group.getRole_id());
            log.debug("| 群聊允许的功能列表: {}", formatList(featureInfoMins));
            for (FeatureInfoMin featureInfoMin : featureInfoMins) {
                if (featureInfoMin.getFeature_id().equals(feature.getFeature_id())) {
                    tag = true;
                    break;
                }
            }
            if (!tag) {
                message.reply().at().text("这个群聊的权限不准用这个功能哦").send();
                return false;
            }
        }

        tag = false;

        List<FeatureInfoMin> featureInfoMins = featureInfoService.findWithRoleId(user.getRole_id());
        log.debug("| 用户允许的功能列表: {}", formatList(featureInfoMins));
        for (FeatureInfoMin featureInfoMin : featureInfoMins) {
            if (featureInfoMin.getFeature_id().equals(feature.getFeature_id())) {
                tag = true;
                break;
            }
        }
        if (!tag) {
            message.reply().at().text("你的权限还不准用这个功能哦").send();
            return false;
        }


        //合法性校验通过 扣除CREDIT
        int credit = userService.minusCredit(userId, feature.getCredit_cost());
        if (credit < 0) {
            message.reply().at().text("你的CREDIT余额不足哦").send();
            return false;
        }
        howMuchCost("| 请求鉴权耗时 {} 毫秒", stopWatch);
        log.info("| 功能 {} 消耗用户 {} CREDIT {}", feature.getFeature_name(), userInfo, feature.getCredit_cost());
        howMuchCost("└ 解析此次请求耗时 {} 毫秒", stopWatch);
        return true;
    }

    private void howMuchCost(String prompt, StopWatch stopWatch) {
        stopWatch.stop();
        log.info(prompt, stopWatch.getTotalTimeMillis());
        stopWatch.start();
    }

    private String formatList(List<FeatureInfoMin> list) {
        StringBuilder result = new StringBuilder();
        for (FeatureInfoMin min : list) {
            result.append(min.getFeature_id()).append(":").append(min.getFeature_name()).append("; ");
        }
        result.append("]");
        return result.toString();
    }
}
