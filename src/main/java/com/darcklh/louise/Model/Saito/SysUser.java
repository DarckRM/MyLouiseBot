package com.darcklh.louise.Model.Saito;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author DarckLH
 * @date 2021/9/13 23:18
 * @Description 后台用户
 */
@Data
public class SysUser {

    @TableId
    private String user_id;
    private int sex;
    private String username;
    private String password;;
    private Timestamp create_time;
    private int isEnabled;

}
