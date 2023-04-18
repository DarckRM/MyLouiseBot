package com.darcklh.louise.Service;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Messages.InMessage;
import com.darcklh.louise.Model.Messages.OutMessage;
import com.darcklh.louise.Model.ReplyException;
import org.springframework.stereotype.Service;

/**
 * @author DarckLH
 * @date 2022/8/13 0:02
 * @Description
 */
@Service
public interface SearchPictureService {
    public void findWithSourceNAO(InMessage inMessage, String url);
    public void findWithAscii2d(InMessage inMessage, String url);
}
