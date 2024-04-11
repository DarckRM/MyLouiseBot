package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Service.Impl.WebSocketService;
import com.darcklh.louise.Config.BootApplication;
import com.darcklh.louise.Utils.LouiseThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * @author DarckLH
 * @date 2021/9/24 21:49
 * @Description
 */
@RestController
@EnableScheduling
@RequestMapping("saito/saito_ws")
@Slf4j
public class WebSocketController {

    private static boolean runCPUPayload = false;

    /**
     * 心跳检测 检查各系统运行状况
     */
    @GetMapping("/system_check")
    @Scheduled(cron = "*/3 * * * * *")
    public void pushOne() {

        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间

        sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");// a为am/pm的标记
        jsonObject.put("bootTime", sdf.format(BootApplication.bootDate));
        WebSocketService.sendMessage("status_conn",jsonObject.toString());

    }

    /**
     * CPU负载信息
     *
     */
    @GetMapping("/cpu_payload/{clientName}")
    public void cpuPayload(@PathVariable String clientName) {
        setRunCPUPayload(true);
        JSONObject jsonObject = new JSONObject();
        JSONObject result = new JSONObject();
        LouiseThreadPool.execute(() -> {
            try {
                while (runCPUPayload) {
                    Date nowDate = new Date();
                    long gapTime = nowDate.getTime() - BootApplication.bootDate.getTime();

                    long day = gapTime / (24 * 60 * 60 * 1000);
                    long hour = (gapTime / (60 * 60 * 1000) - day * 24);
                    long min = ((gapTime / (60 * 1000)) - day * 24 * 60 - hour * 60);
                    long s = (gapTime / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

                    jsonObject.put("cpu_payload", "系统已经运行了 " + day + "天" + hour + "小时" + min + "分钟" + s + "秒");
                    result.put("result", jsonObject);
                    WebSocketService.sendMessage(clientName, result.toString());
                    Thread.sleep(3000);
                }
                Thread.interrupted();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    @GetMapping("/stop_run_cpu_payload")
    public void stopRunCpuPayload() {
        setRunCPUPayload(false);
    }

    private static synchronized void setRunCPUPayload(boolean state) {
        runCPUPayload = state;
    }
}
