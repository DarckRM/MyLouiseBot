package com.darcklh.louise.Model.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/29 5:48
 * @Description
 */
@Data
public class RoleFeatureId {

    private Integer role_id;
    private String role_name;
    private String info;
    private int[] featureInfoList;

}
