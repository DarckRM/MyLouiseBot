package com.darcklh.louise.Service.Impl;

import com.darcklh.louise.Mapper.ProcessImageDao;
import com.darcklh.louise.Model.Louise.ProcessImage;
import com.darcklh.louise.Model.InnerException;
import com.darcklh.louise.Service.ProcessImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * @author DarckLH
 * @date 2022/5/21 18:43
 * @Description
 */
@Service
@Slf4j
public class ProcessImageImpl implements ProcessImageService {

    public static boolean isImageLibUpdate = false;

    @Autowired
    private ProcessImageDao processImageDao;

    @Override
    public List<ProcessImage> findBy() {
        List<ProcessImage> processImageList = processImageDao.findAll();
        return processImageList;
    }

    public ProcessImage findByHashCode(String hash_code) {
        return processImageDao.selectById(hash_code);
    }

    @Override
    public String delBy(Integer id) {
        isImageLibUpdate = false;
        return null;
    }

    @Override
    public int delByHashCode(ProcessImage processImage) {
        try{
            // 删除缩略图
            File file = new File(processImage.getImage_path() + '/' + processImage.getImage_name());
            // 删除原图
            File origin = new File(processImage.getImage_path().replace("_index", "") + '/' + processImage.getImage_name());
            if(origin.delete()){
                if (file.delete())
                    log.info(file.getName() + " 文件已被删除！");
            }else{
                log.info("文件删除失败！");
            }
        }catch(Exception e){
            throw new InnerException(500, "删除文件失败", e.getMessage());
        }
        return processImageDao.deleteById(processImage.getHash_code());
    }

    @Override
    public String editBy(ProcessImage object) {
        return null;
    }

    @Override
    public String add(ProcessImage object) {
        isImageLibUpdate = false;
        return null;
    }
}
