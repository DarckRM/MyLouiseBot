package com.darcklh.louise.Api;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Service.PluginInfoService;
import com.darcklh.louise.Utils.HttpProxy;
import com.darcklh.louise.Utils.UniqueGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author DarckLH
 * @date 2021/8/7 18:48
 * @Description 处理文件操作相关的接口
 */
@Slf4j
@RestController
public class FileControlApi {

    @Autowired
    PluginInfoService pluginInfoService;

    /**
     * 上传插件
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/saito/upload/plugin", produces = "application/json")
    public JSONObject uploadPlugin(MultipartFile file) throws IOException {
        JSONObject jsonObject = new JSONObject();
        Result<String> result = new Result<>();
        log.info("上传插件: " + file.getOriginalFilename());

        if (!file.isEmpty()) {

            String fileName = file.getOriginalFilename(); //获取上传的文件名
            String suffixName = fileName.substring(fileName.lastIndexOf("." )+ 1); //获取后缀名
            result.setData(fileName);
            if (!suffixName.equals("jar")) {
                result.setMsg("上传失败\n不支持的文件类型 " + suffixName);
                result.setCode(403);
                jsonObject.put("result", result);
                return jsonObject;
            }

            File dest = new File(new File("plugins/").getAbsolutePath() + "/" + fileName);
            // 如果插件存在则先尝试卸载插件
            if (dest.exists()) {
                log.info("系统已存在插件 " + fileName);
                pluginInfoService.unloadPlugin("plugins/" + fileName);
                log.info("已完成插件 " + fileName + " 的卸载");
                if(dest.delete())
                    log.info("已删除旧版本的插件");
            }

            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            file.transferTo(dest);
            String realUrl = "/plugins/" + fileName;
            result.setMsg("上传成功");
            result.setData(realUrl);
            result.setCode(200);
            jsonObject.put("file_name", fileName);
            jsonObject.put("result", result);
            return jsonObject;

        } else {
            result.setCode(500);
            result.setMsg("上传失败");
            jsonObject.put("result", result);
            return jsonObject;
        }
    }

    /**
     * 上传图片
     * @param file
     * @return
     */
    @RequestMapping(value = "/saito/upload/image", produces = "application/json")
    public JSONObject uploadImage(MultipartFile file) throws IOException {
        JSONObject jsonObject = new JSONObject();
        Result<String> result = new Result<>();
        log.info("上传图片: " + file.getOriginalFilename());

        if (!file.isEmpty()) {

            String fileName = file.getOriginalFilename(); //获取上传的文件名
            String suffixName = fileName.substring(fileName.lastIndexOf("." )+ 1); //获取后缀名
            fileName = "Image_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + UniqueGenerator.generateShortUuid() + "." + suffixName;
            result.setData(fileName);
            File dest = new File(new File(LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION).getAbsolutePath() + "/" + fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            file.transferTo(dest);
            String realUrl = "/saito/image/" + fileName;
            result.setMsg("上传成功");
            result.setData(realUrl);
            result.setCode(200);
            jsonObject.put("file_name", fileName);
            jsonObject.put("result", result);
            return jsonObject;

        } else {
            result.setCode(0);
            result.setMsg("上传失败");
            jsonObject.put("result", result);
            return jsonObject;
        }
    }

    /**
     * images图片下载
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/saito/image/**")
    @ResponseBody
    public JSONObject downloadImage(HttpServletRequest request, HttpServletResponse response){
        String url = request.getRequestURI();
        JSONObject jsonObject = new JSONObject();
        String fileName = url.replace("/saito/image/","");

        File file = new File(LouiseConfig.LOUISE_CACHE_LOCATION + "/images/" + fileName);
        if(!file.exists()){
            jsonObject.put("success",2);
            jsonObject.put("result","下载文件不存在");
            return jsonObject;
        }
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName );
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            jsonObject.put("success",2);
            jsonObject.put("result","下载文件不存在");
            return jsonObject;
        }
        jsonObject.put("success",0);
        jsonObject.put("result","下载文件成功");
        return jsonObject;
    }

    /**
     * 用 RestTemplate 添加代理进行请求
     * @param urlList
     * @param fileName
     * @param fileOrigin
     * @return
     */
    public boolean downloadPicture_RestTemplate(String urlList, String fileName, String fileOrigin) {
        //判断目录是否存在
        String filePath = LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION + fileOrigin + "/";
        File folder = new File(filePath);
        // 文件保存的本地路径
        String targetPath = LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION + fileOrigin + "/" + fileName;

        if (!folder.exists() && !folder.isDirectory()) {
            log.info("创建了图片缓存文件夹" + fileOrigin);
            folder.mkdirs();
        }

        File file = new File(targetPath);
        if (file.exists()) {
            log.info("已经存在图片 " + targetPath);
            return true;
        }

        RestTemplate restTemplate = new RestTemplate();

        // 定义请求头的接收类型
        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
        // 对响应进行流式处理而不是将其全部加载到内存中
        // 借助代理请求
        if (LouiseConfig.LOUISE_PROXY_PORT > 0)
            restTemplate.setRequestFactory(new HttpProxy().getFactory("下载图片"));

        restTemplate.execute(urlList, HttpMethod.GET, requestCallback, clientHttpResponse -> {
            Files.copy(clientHttpResponse.getBody(), Paths.get(targetPath));
            return null;
        });
        return true;
    }

    /**
     * 根据传入的URL下载图片到本地
     * @param urlList
     */
    public boolean downloadPictureURL_Single(String urlList, String fileName, String fileOrigin) {
        URL url = null;
        String filePath = LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION + fileOrigin + "/";
        String imageName = filePath + fileName + ".jpg";
        try {
            log.info("开始下载" + imageName + " 图片地址: " + urlList);

            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
            output.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            log.info("下载图片" + imageName + "失败");
            return false;
        }
        return true;
    }

}
