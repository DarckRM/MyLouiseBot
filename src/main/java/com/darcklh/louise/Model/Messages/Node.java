package com.darcklh.louise.Model.Messages;

import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.R;
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
    DataType data = new DataType();
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

    public Node(String message, Long self_id) {
        this.type = "node";
        this.data.content = message;
        this.data.uin = self_id;
    }

    public Node() {
        this.type = "node";
        this.data.content = "";
    }

    public static Node build() {
        Node node = new Node();
        node.data.uin = Long.parseLong(LouiseConfig.BOT_ACCOUNT);
        return node;
    }

    public Node text(String text) {
        this.data.content += text;
        this.transfers.add(new Transfer(NodeType.text, text));
        return this;
    }

    public Node text(String text, int index) {
        this.data.content += text;
        this.transfers.add(index, new Transfer(NodeType.text, text));
        return this;
    }

    public Node image(String image) {
        this.data.content += "[CQ:image,file=" + image + "]";
        this.transfers.add(new Transfer(NodeType.image, image));
        return this;
    }

    public Node image(String image, int index) {
        this.data.content += "[CQ:image,file=" + image + "]";
        this.transfers.add(index, new Transfer(NodeType.image, image));
        return this;
    }

    public Node reply() {
        return this;
    }
}
