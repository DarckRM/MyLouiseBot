package com.darcklh.louise.Model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/13 23:42
 * @Description
 */
@Data
public class Result<T> {

    private List<T> datas;
    private T data;
    private Integer code;
    private String msg;

}
