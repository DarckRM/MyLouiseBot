package com.darcklh.louise.Service;

import com.darcklh.louise.Model.Louise.ProcessImage;
import org.springframework.stereotype.Service;

/**
 * @author DarckLH
 * @date 2022/5/21 18:42
 * @Description
 */
@Service
public interface ProcessImageService extends BaseService<ProcessImage> {
    public int delByHashCode(ProcessImage processImage);
    public ProcessImage findByHashCode(String hash_code);
}
