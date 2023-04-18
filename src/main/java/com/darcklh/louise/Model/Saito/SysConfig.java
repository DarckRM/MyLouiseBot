package com.darcklh.louise.Model.Saito;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/20 16:48
 * @Description 系统默认参数
 */
@Data
public class SysConfig {

    @TableId
    private Integer config_id;
    private String config_name;
    private String config_key;
    private String config_value;
    private String info;
    private int type;

}
