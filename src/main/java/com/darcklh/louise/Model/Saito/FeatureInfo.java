package com.darcklh.louise.Model.Saito;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/20 16:54
 * @Description 功能注册信息
 */
@Data
public class FeatureInfo {

    @TableId(type = IdType.AUTO)
    private Integer feature_id;
    private int type;
    private String feature_url;
    private String feature_name;
    private String feature_cmd;
    private int credit_cost;
    private int invoke_limit;
    private String description;
    private String info;
    private int is_original;
    private int is_auth;
    private int is_enabled;
    private int count;
    private String avatar;

}
