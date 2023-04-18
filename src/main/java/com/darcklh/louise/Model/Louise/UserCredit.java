package com.darcklh.louise.Model.Louise;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/20 0:50
 * @Description User的Credit记录
 */
@Data
public class UserCredit {

    @TableId(type = IdType.AUTO)
    private Integer credit_id;
    private String user_id;
    private String type;
    private int number;
    private int credit_left;

}
