package com.darcklh.louise.Model.Louise;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/20 0:56
 * @Description QQ用户或群组角色信息
 */
@Data
public class MemberRole {

    @TableId
    private String number;
    private String type;
    private int role_id;
    private int is_enabled;

}
