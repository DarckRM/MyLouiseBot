package com.darcklh.louise.Model.Louise;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import lombok.Data;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/20 16:46
 * @Description 角色信息
 */
@Data
public class Role {

    @TableId(type = IdType.AUTO)
    private Integer role_id;
    private String role_name;
    private String info;
    private int is_enabled;
    @TableField(exist = false)
    private List<FeatureInfoMin> featureInfoList;


}
