package com.darcklh.louise.Service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Config.LouiseConfig;
import com.darcklh.louise.Mapper.ProcessImageDao;
import com.darcklh.louise.Model.Louise.ProcessImage;
import com.darcklh.louise.Service.CBIRService;
import com.darcklh.louise.Utils.ImageCompress;
import com.darcklh.louise.Utils.TaskDistributor;
import com.darcklh.louise.Utils.WorkThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;

@Service
@Slf4j
public class CBIRServiceImpl implements CBIRService {

    //默认启用多线程数
    private static int THREAD_COUNT = 12;

    @Autowired
    private ProcessImageDao processImageDao;

    public int reCalculateImageLib() throws InterruptedException, IOException, NoSuchAlgorithmException {

        log.info("开始执行图片数据库重构任务");
        // 重新生成缩略图文件夹
        log.info("重新生成缩略图文件夹");
        startAllCompress();
        startAllCBIR(false);
        log.info("清除数据库原有记录");
        processImageDao.removeAllData();
        log.info("写入新记录");
        for(Map.Entry<String, ProcessImage> entry : CalcImageTask.NewMap.entrySet()) {
            entry.getValue().setHistogram_json(Arrays.deepToString(entry.getValue().getHistogram_info()));
            processImageDao.insert(entry.getValue());
        }
        return 1;
    }

    public JSONObject startAllCBIR(boolean isFinding) throws InterruptedException, IOException, NoSuchAlgorithmException {

        File file = new File(LouiseConfig.LOUISE_CACHE_LOCATION + "/images_index/");

        List<CalcImageTask> taskList = new ArrayList<>();
        initCalcImageTaskList(file, taskList);
        log.info("共有: " + taskList.size() + " 个 CalcImage 任务");
        List[] taskListPerThread = TaskDistributor.distributeTasks(taskList, THREAD_COUNT);
        log.debug("实际启动的工作线程数：" + taskListPerThread.length);
        startCalcImageWorkThread(taskListPerThread, taskList.size());
        if (isFinding)
            return compareImage(new File("cache/ready_compare/ready.jpg"));
        return null;
    }

    public void startAllCompress() throws InterruptedException {

        File file = new File(LouiseConfig.LOUISE_CACHE_IMAGE_LOCATION);
        List<CompressImageTask> taskList = new ArrayList<>();
        initCompressImageTask(file, taskList);
        log.info("共有: " + taskList.size() + " 个 CompressImage 任务");
        List[] taskListPerThread = TaskDistributor.distributeTasks(taskList, THREAD_COUNT);
        log.debug("实际要启动的工作线程数：" + taskListPerThread.length);
        startCompressWorkThread(taskListPerThread, taskList.size());
    }

    public int startCompressAndCalc(String file_path) throws IOException, NoSuchAlgorithmException {
        File file = new File(file_path);
        ImageCompress.resize(file.getParent(), file.getName());
        ProcessImage ig = new ProcessImage(file.getParent(), file.getName());
        CalcImageTask.NewMap.put(ig.getHash_code(), ig);
        processImageDao.insert(ig);
        return 1;
    }

    private void startCompressWorkThread(List[] taskListPerThread, int taskList_size) throws InterruptedException {
        int i = 0;
        for (List singleTask : taskListPerThread) {
            Thread workThread = new WorkThread(singleTask, i);
            workThread.start();
            i++;
        }
        Thread.sleep(1500);
        int status = 0;
        int flag = 0;
        while(CompressImageTask.totalTask != taskList_size) {
            log.info("当前处理数: " + CompressImageTask.totalTask);
            if (status != CompressImageTask.totalTask) {
                status = CompressImageTask.totalTask;
                Thread.sleep(1000);
            } else {
                if (flag == 3) {
                    log.info("任务 CompressImage 执行超时");
                    break;
                }
                flag++;
            }
        }
    }

    private void startCalcImageWorkThread(List[] taskListPerThread, int taskList_size) throws InterruptedException {
        for (int j = 0; j < taskListPerThread.length; j++) {
            Thread workThread = new WorkThread(taskListPerThread[j], j);
            workThread.start();
        }
        Thread.sleep(1500);
        int status = 0;
        int flag = 0;
        while(CalcImageTask.NewMap.size() != taskList_size) {
            log.info("当前处理数: " + CalcImageTask.NewMap.size());
            if (status != CalcImageTask.NewMap.size()) {
                status = CalcImageTask.NewMap.size();
                Thread.sleep(1000);
            } else {
                if (flag == 3) {
                    log.info("任务 CalImage 执行超时");
                    break;
                }
                flag++;
            }
        }
    }

    public JSONObject compareImageCompress(String compare_image) throws IOException, NoSuchAlgorithmException, InterruptedException {

        File compareFile = new File(compare_image);
        BufferedImage src = ImageIO.read(compareFile);
        BufferedImage bufferedImage = ImageCompress.resizeImage(src, src.getWidth() / 2, src.getHeight() / 2);
        ImageCompress.compress(bufferedImage, "cache/ready_compare/", "ready.jpg");

        if (ProcessImageImpl.isImageLibUpdate) {
            Map<String, ProcessImage> ImageMap = new HashMap<>();
        }

        if (CalcImageTask.NewMap.size() != 0)
            return compareImage(compareFile);
        return startAllCBIR(true);
    }

    private JSONObject compareImage(File compare_image) throws IOException, NoSuchAlgorithmException {

        ProcessImage ig = null;
        try {
            ig = new ProcessImage(compare_image.getParent(), compare_image.getName());
        }
        catch(NullPointerException | IOException | NoSuchAlgorithmException e) {
            log.info("读取图片: " + compare_image.getName() + " 失败了");
        }
        JSONObject jsonObject = new JSONObject();

        List<ProcessImage> result_mkList = new ArrayList<>();
        List<ProcessImage> result_hiList = new ArrayList<>();
        List<ProcessImage> result_rdList = new ArrayList<>();
        Map<String, ProcessImage> finalList = CalcImageTask.NewMap;

        jsonObject.put("result_mkList", result_mkList);
        TreeMap<Double, String> resultMap = new TreeMap<>();
        for(Map.Entry<String, ProcessImage> entry : finalList.entrySet()) {
            double hisID = getHISimilarity(ig.getHistogram_info(), entry.getValue().getHistogram_info());
            String image_path = entry.getValue().getImage_path().substring(18);
            String image_name = entry.getValue().getImage_name();
            resultMap.put(hisID, image_path + "/" + image_name);
        }
        int number = 0;
        while( number <= 4) {
            result_hiList.add(new ProcessImage("/saito/image/", resultMap.lastEntry().getValue(), false));
            number++;
            resultMap.remove(resultMap.lastKey());
        }

        number = 0;
        jsonObject.put("result_hiList", result_hiList);
        resultMap.clear();

        for(Map.Entry<String, ProcessImage> entry : finalList.entrySet()) {
            double hisID = getEDSimilarity(ig.getHistogram_info(), entry.getValue().getHistogram_info());
            String image_path = entry.getValue().getImage_path().substring(18);
            String image_name = entry.getValue().getImage_name();

            resultMap.put(hisID, image_path + "/" + image_name);
        }
        while( number <= 4) {
            result_rdList.add(new ProcessImage("/saito/image/", resultMap.firstEntry().getValue(), false));
            number++;
            resultMap.remove(resultMap.firstKey());
        }
        jsonObject.put("result_rdList", result_rdList);
        return jsonObject;
    }

    private void initCalcImageTaskList(File file, List<CalcImageTask> taskList) {
        int i = 0;
        for (File image : Objects.requireNonNull(file.listFiles())) {
            if (image.isDirectory()) {
                initCalcImageTaskList(image, taskList);
                continue;
            }
            CalcImageTask task = new CalcImageTask(i, "task: " + i,  image);
            if (task.getStatus() != 9) {
                taskList.add(task);
                task = null;
            }
            i++;
        }
    }

    private void initCompressImageTask(File file, List<CompressImageTask> taskList) {
        int i = 0;
        for (File image : Objects.requireNonNull(file.listFiles())) {
            if (image.isDirectory()) {
                initCompressImageTask(image, taskList);
                continue;
            }
            CompressImageTask task = new CompressImageTask(i, "task: " + i,  image);
            if (task.getStatus() != 9)
                taskList.add(task);
            i++;
        }
    }

    @Override
    public float minkowskiDist(double[] InHist, double[] sampleHist) {
        float minkDist = 0;
        for (int i=0;i<InHist.length-1;i++)
            minkDist+=Math.pow((InHist[i]-sampleHist[i]),2);
        return (float) Math.sqrt(minkDist);
    }

    @Override
    public float histogramIntersectionDist(double[] InHist, double[] sampleHist) {
        float HIDist = 0;
        for (int i=0;i<InHist.length-1;i++)
            HIDist+=Math.min(InHist[i],sampleHist[i]);
        return HIDist;
    }

    @Override
    public float relativeDeviationDist(double[] InHist, double[] sampleHist) {
        float RDDist = 0;
        float Numerator=0;
        float Denom1=0;
        float Denom2=0;

        for (int i=0;i<InHist.length-1;i++)
        {
            Numerator+= Math.sqrt(Math.pow((InHist[i]-sampleHist[i]),2));
            Denom1+=Math.sqrt(Math.pow(InHist[i],2));
            Denom2+=Math.sqrt(Math.pow(sampleHist[i],2));
        }
        RDDist=2*Numerator/(Denom1+Denom2);
        return RDDist;
    }

    //欧式距离求图片的相似度
    public double getEDSimilarity(double [][] inHist, double  [][] sampleHist)//欧式距离法，匹配值越小，越相似
    {
        double similar = (double) 0.0;//相似度
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < inHist[i].length; j++){
                similar += (inHist[i][j] - sampleHist[i][j]) * (inHist[i][j] - sampleHist[i][j]);
            }
        }
        similar=similar/6;
        similar=Math.sqrt(similar);
        return similar;
    }

    //传统的直方图相交法  统计RGB  归一化 后用交来求两个图片的相似度
    public double getHISimilarity(double [][] inHist,double  [][] sampleHist)
    {
        double similar = (double) 0.0;//相似度
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < inHist[i].length; j++) {
                similar += Math.min(inHist[i][j], sampleHist[i][j]);
            }
        }
        similar = similar / 3;
        return similar;
    }
}
