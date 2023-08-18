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

    public FeatureStatic(long user_id, long group_id, int feature_id, Timestamp invoke_time) {
        this.user_id = user_id;
        this.group_id = group_id;
        this.feature_id = feature_id;
        this.invoke_time = invoke_time;
    }

    @TableId(type = IdType.AUTO)
    private Integer invoke_id;
    private long user_id;
    private long group_id;
    private int feature_id;
    private Timestamp invoke_time;

}
