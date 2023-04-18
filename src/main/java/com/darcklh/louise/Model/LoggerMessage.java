package com.darcklh.louise.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/11/20 17:08
 * @Description
 */
@Data
@AllArgsConstructor
public class LoggerMessage {
    private String body;
    private String timestamp;
    private String threadName;
    private String className;
    private String level;
    private String exception;
    private String cause;
}
