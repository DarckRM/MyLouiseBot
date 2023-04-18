package com.darcklh.louise.Model.Louise;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2023/3/23 5:51
 * @Description Booru 图站中的中英对照关系
 */
@Data
public class BooruTags {
    @TableId(type = IdType.AUTO)
    private Integer tag_id;
    private String origin_name;
    private String cn_name;
    private String alter_name;
    private String produce;
    private String info;
}
