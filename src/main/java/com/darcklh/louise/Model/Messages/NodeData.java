package com.darcklh.louise.Model.Messages;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author DarckLH
 * @date 2022/8/13 2:24
 * @Description
 */
@Data
public class NodeData {
    // 转发消息id
    Integer id;
    // 发送者显示名字
    String name = "Louise";
    // 发送者QQ号
    long uin;
    // 用于自定义消息 不支持转发套娃
    ArrayList<JSONObject> content = new ArrayList<>();
}
