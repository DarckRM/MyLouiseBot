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

    @Autowired
    R r;

    @ExceptionHandler(value = InnerException.class)
    @ResponseBody
    public JSONObject InnerExceptionHandler(InnerException sE) {

        Result<String> result = new Result<>();
        result.setMsg(sE.getMessage());
        log.info("errorMsg={},innerCode={},exception={}", sE.getErrorMsg(), sE.getInnerCode(), sE.getOriginErrorMessage());
        sE.getJsonObject().put("result", result);
        return sE.getJsonObject();
    }

    @ExceptionHandler(value = ReplyException.class)
    @ResponseBody
    public JSONObject handleReplyException(ReplyException e) {
        if (e.getType() == 0)
            return e.getReply();
        r.sendMessage(e.getOutMessage());
        return null;
    }
}
