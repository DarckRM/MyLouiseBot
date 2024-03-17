package com.darcklh.louise.Controller;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Model.ReplyException;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.InnerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author DarckLH
 * @date 2022/4/18 21:33
 * @Description
 */
@Slf4j
@ControllerAdvice(annotations = RestController.class)
public class NaiveSaitoControllerHandler {

    @ExceptionHandler(value = InnerException.class)
    @ResponseBody
    public void innerExceptionHandler(InnerException sE) {

        Result<String> result = new Result<>();
        result.setMsg(sE.getMessage());
        log.error("errorMsg={},innerCode={},exception={}", sE.getErrorMsg(), sE.getInnerCode(), sE.getOriginErrorMessage());
    }

    @ExceptionHandler(value = ReplyException.class)
    @ResponseBody
    public JSONObject handleReplyException(ReplyException e) {
        R r = new R();
        switch (e.getType()) {
            case 0 -> {
                return e.getReply();
            }
            case 1 -> r.sendMessage(e.getOutMessage());
            case 2 -> e.getMsg().send();
            default -> log.error("未知的 replyException 类型");
        }
        return null;
    }
}
