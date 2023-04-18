package com.darcklh.louise.Model.Saito;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author DarckLH
 * @date 2021/9/20 16:59
 * @Description 功能调用统计
 */
@Data
public class FeatureStatic {

    @TableId(type = IdType.AUTO)
    private Integer invoke_id;
    private long user_id;
    private long group_id;
    private int feature_id;
    private Timestamp invoke_time;

}
