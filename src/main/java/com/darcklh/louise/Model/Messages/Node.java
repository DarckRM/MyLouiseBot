package com.darcklh.louise.Model.Messages;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DarckLH
 * @date 2022/8/13 2:24
 * @Description
 */
@Data
public class Node {
    String type;
    NodeData data = new NodeData();
    List<Transfer> transfers = new ArrayList<>();

    class Transfer {
        NodeType nodeType;
        String value;

        public Transfer(NodeType nodeType, String value) {
            this.nodeType = nodeType;
            this.value = value;
        }
    }

    public enum NodeType {
        image,
        text,
        at,
        reply
    }

    public static Node build() {
        Node node = new Node();
        node.data.uin = Long.parseLong(LouiseConfig.BOT_ACCOUNT);
        node.type = "node";
        return node;
    }

    public Node text(String text) {
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        obj.put("type", "text");
        data.put("text", text);
        obj.put("data", data);
        this.data.content.add(obj);
        this.transfers.add(new Transfer(NodeType.text, text));
        return this;
    }

    public Node text(String text, int index) {
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        obj.put("type", "text");
        data.put("text", text);
        obj.put("data", data);
        this.data.content.add(index, obj);
        this.transfers.add(index, new Transfer(NodeType.text, text));
        return this;
    }

    public Node image(String image) {
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        obj.put("type", "image");
        data.put("file", image);
        obj.put("data", data);
        this.data.content.add(obj);
        this.transfers.add(new Transfer(NodeType.image, image));
        return this;
    }

    public Node image(String image, int index) {
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        obj.put("type", "image");
        data.put("file", image);
        obj.put("data", data);
        this.data.content.add(index, obj);
        this.transfers.add(index, new Transfer(NodeType.image, image));
        return this;
    }

}
