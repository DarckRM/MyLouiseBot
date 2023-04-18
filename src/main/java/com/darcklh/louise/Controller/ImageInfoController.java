package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Api.FileControlApi;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Louise.ProcessImage;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Service.CBIRService;
import com.darcklh.louise.Service.ProcessImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author DarckLH
 * @date 2022/4/22 0:45
 * @Description
 */
@Slf4j
@RestController
@RequestMapping("/saito/image-info/")
public class ImageInfoController {

    @Autowired
    private CBIRService cbirService;

    @Autowired
    private ProcessImageService processImageService;



    /**
     * 执行图片索引库初始化操作
     * 将图片缓存目录下的所有图片创建缩略图并放到image_index文件夹中
     * @return
     */
    @RequestMapping("init")
    public Result<String> initImageLib() {
        Result<String> result = new Result<>();
        result.setMsg("图像数据库初始化失败");
        result.setCode(0);
        try {
            result.setCode(cbirService.reCalculateImageLib());
        } catch (Exception e) {
            return result;
        }
        if (result.getCode() == 1) {
            result.setMsg("图像数据库初始化成功");
        }
        return result;
    }

    /**
     * 对图片索引库的所有图片进行特征计算，
     * @return JSONObject
     */
    public JSONObject createImageLibThumbnail() {
        JSONObject jsonObject = new JSONObject();
        return jsonObject;
    }

    @RequestMapping("start_cbir")
    public Result<JSONObject> startCBIR(@RequestBody String compare_image) throws InterruptedException, NoSuchAlgorithmException, IOException {
        log.info("待检索的图片本地位置: " + compare_image);
        Result<JSONObject> result = new Result<>();
        result.setData(cbirService.compareImageCompress(compare_image));
        result.setMsg("查询完成");
        return result;
    }

    @RequestMapping("findAll")
    public Result<ProcessImage> findAll() {
        Result<ProcessImage> processImageResult = new Result<>();
        processImageResult.setMsg("查询图像数据库失败");
        processImageResult.setCode(0);

        List<ProcessImage> processImageList = processImageService.findBy();
        if (processImageList.size() > 0) {
            processImageResult.setCode(1);
            processImageResult.setDatas(processImageList);
            processImageResult.setMsg("查询图像数据库成功");
        }
        return processImageResult;
    }

    @RequestMapping("save")
    public Result<String> save(@RequestBody String image_name) throws IOException, InterruptedException, NoSuchAlgorithmException {
        Result<String> result = new Result<>();
        result.setMsg("上传图片失败");
        result.setCode(0);

        result.setCode(cbirService.startCompressAndCalc(LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION + image_name));
        if (result.getCode() > 0) {
            result.setMsg("上传图片成功");
            result.setData(image_name);
        }
        return result;
    }

    @RequestMapping("drop/{hash_code}")
    public Result<String> remove(@PathVariable("hash_code") String hash_code) {
        Result<String> processImageResult = new Result<>();
        processImageResult.setMsg("删除图像文件失败");
        processImageResult.setCode(0);
        ProcessImage processImage = processImageService.findByHashCode(hash_code);
        int code = processImageService.delByHashCode(processImage);
        if (code == 1) {
            processImageResult.setCode(1);
            processImageResult.setMsg("删除图像文件成功");
        }
        return processImageResult;
    }

}
