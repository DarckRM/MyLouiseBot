package com.darcklh.louise.Api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Enum.DownloadType;
import com.darcklh.louise.Model.Louise.BooruImages;
import com.darcklh.louise.Model.Louise.BooruTags;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.Message;
import com.darcklh.louise.Model.Messages.Node;
import com.darcklh.louise.Model.MultiThreadTask.DownloadPicTask;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Service.BooruImagesService;
import com.darcklh.louise.Service.BooruTagsService;
import com.darcklh.louise.Service.MultiTaskService;
import com.darcklh.louise.Service.UserService;
import com.darcklh.louise.Utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

import static com.darcklh.louise.Model.GoCqhttp.AllPost.PostType.message;

/**
 * @author DarckLH
 * @date 2022/4/13 20:21
 * @Description
 */
@Slf4j
@RestController
@Deprecated
public class YandeApi {
    @RequestMapping("louise/yande/*")
    public void deprecate(@RequestBody Message message) {
        message.clear().reply().text("!将被弃用，请直接使用 yande 即可").send();
    }
}