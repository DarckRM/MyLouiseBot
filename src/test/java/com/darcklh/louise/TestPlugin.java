package com.darcklh.louise;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Annotation.LouisePlugin;
import com.darcklh.louise.Model.Annotation.OnCommand;
import com.darcklh.louise.Model.Annotation.OnMessage;
import com.darcklh.louise.Model.Annotation.OnNotice;
import com.darcklh.louise.Model.Enum.Environment;
import com.darcklh.louise.Model.GoCqhttp.NoticePost;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Service.PluginService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LouisePlugin(prefix = "test")
public class TestPlugin implements PluginService {
    @Override
    public String pluginName() {
        return null;
    }

    @Override
    public JSONObject service(Message message) {
        return null;
    }
    @OnNotice(notices = {NoticePost.NoticeType.friend_recall, NoticePost.NoticeType.client_status})
    public JSONObject testNotice(NoticePost post) {
        return null;
    }

    @OnCommand(commands = {"func1", "func3213213123213123"})
    public JSONObject testFunc(Message message) {
        log.info("测试函数 1 被调用了");
        return null;
    }

    @OnMessage(messages = {"^你好"})
    public JSONObject testFunc2(Message message) {
        log.info("测试函数 2 被调用了");
        return null;
    }


    @Override
    public JSONObject service() {
        return null;
    }

    @Override
    public boolean init(Environment env) {
        return false;
    }

    @Override
    public boolean reload() {
        return false;
    }
}
