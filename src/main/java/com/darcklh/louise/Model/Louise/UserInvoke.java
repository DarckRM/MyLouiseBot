package com.darcklh.louise.Model.Louise;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author DarckLH
 * @date 2021/9/20 0:53
 * @Description 用户功能调用记录
 */
@Data
public class UserInvoke {

    @TableId
    private Integer feature_id;
    private String user_id;
    private Timestamp invoke_time;

}
