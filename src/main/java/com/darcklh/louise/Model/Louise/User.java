package com.darcklh.louise.Model.Louise;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author DarckLH
 * @date 2021/8/7 18:57
 * @Description 使用MyLouise的用户信息
 */
@Data
public class User {

    @TableId
    private Long user_id;
    private long group_id;
    private int role_id;
    private String avatar;
    private String nickname;
    private Timestamp join_time;
    private Timestamp create_time;
    private int count_setu;
    private int count_upload;
    private int isEnabled;
    private int credit;
    private int credit_buff;

}
