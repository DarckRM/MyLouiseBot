package com.darcklh.louise.Model.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import com.darcklh.louise.Model.Louise.Role;
import com.darcklh.louise.Model.Louise.User;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author DarckLH
 * @date 2021/9/28 23:43
 * @Description
 */
@Data
public class UserRole {

    @TableId
    private Long user_id;
    private long group_id;
    private String avatar;
    private String nickname;
    private Timestamp join_time;
    private Timestamp create_time;
    private int count_setu;
    private int count_upload;
    private int isEnabled;
    private int credit;
    private int credit_buff;
    private int role_id;
    private String role_name;
    private String info;
}
