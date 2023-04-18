package com.darcklh.louise.Model.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/29 5:56
 * @Description
 */
@Data
public class RoleFeature {

    @TableId
    private Integer role_id;
    private int feature_id;
    private String info;

}
