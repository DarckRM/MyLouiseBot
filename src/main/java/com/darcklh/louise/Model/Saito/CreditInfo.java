package com.darcklh.louise.Model.Saito;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/9/20 17:00
 * @Description Credit信息
 */
@Data
public class CreditInfo {

    @TableId
    private Integer credit_total;
    private int credit_backup;
    private int credit_recover;

}
