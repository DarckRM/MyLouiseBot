package com.darcklh.louise.Model.MultiThreadTask;

import com.darcklh.louise.Api.FileControlApi;
import com.darcklh.louise.Service.MultiTaskService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author DarckLH
 * @date 2023/3/26 11:40
 * @Description 用于下载图片的子任务
 */
@Data
@Slf4j
public class DownloadPicTask implements MultiTaskService {

    private int status;
    private int taskId;
    private int thread_id;
    private int total;
    private String urlList;
    private String fileName;
    private String fileOrigin;
    // 总任务计数器

    private FileControlApi fileControlApi;

    public DownloadPicTask(int taskId, String urlList, String fileName, String fileOrigin, FileControlApi fileControlApi) {
        this.taskId = taskId;
        this.urlList = urlList;
        this.fileName = fileName;
        this.fileOrigin = fileOrigin;
        this.fileControlApi = fileControlApi;
    }

    @Override
    public boolean execute() throws NoSuchAlgorithmException, IOException {
        setStatus(MultiTaskService.RUNNING);
        fileControlApi.downloadPicture_RestTemplate(urlList, fileName, fileOrigin);
        log.info("任务列表 " + thread_id + ": 剩下 " + total + "张");
        setStatus(FINISHED);
        return true;
    }

    @Override
    public boolean callback() {
        return false;
    }

    @Override
    public int getThreadId() {
        return this.thread_id;
    }

    @Override
    public void setThreadId(int thread_id) {
        this.thread_id = thread_id;
    }
}
