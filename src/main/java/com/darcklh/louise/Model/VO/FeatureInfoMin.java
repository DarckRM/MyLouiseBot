package com.darcklh.louise.Model.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/29 0:28
 * @Description
 */
@Data
public class FeatureInfoMin {

    @TableId
    private Integer feature_id;
    private String feature_name;
    private String feature_url;

}
