package com.darcklh.louise.Model.Saito;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.darcklh.louise.Service.PluginService;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 插件封装类
 */
@Data
@Component
public class PluginInfo {

    @TableField(exist = false)
    PluginService pluginService;

    @TableId
    private Integer plugin_id;
    private int feature_id;
    private int type;
    private String author;
    private String name;
    private String cmd;
    private String path;
    private String class_name;
    private Timestamp create_time;
    private int is_enabled;
    private String info;
    private String description;

}
