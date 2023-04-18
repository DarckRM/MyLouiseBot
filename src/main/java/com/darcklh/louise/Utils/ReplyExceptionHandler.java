package com.darcklh.louise.Utils;

import com.darcklh.louise.Model.ReplyException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author DarckLH
 * @date 2022/11/9 23:44
 * @Description
 */
@Slf4j
public class ReplyExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof ReplyException)
            throw new ReplyException(e.getMessage());
        else
            log.warn(e.getLocalizedMessage());
    }
}
