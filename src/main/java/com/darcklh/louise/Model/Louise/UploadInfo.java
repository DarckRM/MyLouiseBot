package com.darcklh.louise.Model.Louise;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author DarckLH
 * @date 2021/8/7 17:36
 * @Description 用户上传文件后保留一些信息
 */
@Data
public class UploadInfo {

    @TableId
    private String id;
    private String user_id;
    private String file_name;
    private String file_type;
    private Integer file_size;
    private String file_info;
    private String password;

}
