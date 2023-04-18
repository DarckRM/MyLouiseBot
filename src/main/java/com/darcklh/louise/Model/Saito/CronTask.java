package com.darcklh.louise.Model.Saito;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/20 16:50
 * @Description 定时任务信息
 */
@Data
public class CronTask {

    @TableId
    private Integer task_id;
    private String task_name;
    private String cron;
    private int type;
    private String url;
    private String target;
    private int is_return;
    private int is_parameter;
    private String sender_type;
    private String number;
    private String parameter;
    private String info;
    private int is_enabled;

}
