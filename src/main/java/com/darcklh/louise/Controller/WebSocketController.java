package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.LoggerQueue;
import com.darcklh.louise.Model.R;
import com.darcklh.louise.Service.WebSocketService;
import com.darcklh.louise.Utils.BootApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private static boolean run_cpu_payload = false;

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
    @GetMapping("/cpu_payload/{client_name}")
    public void cpuPayload(@PathVariable String client_name) {
        run_cpu_payload = true;
//        int cpu_count = new SystemInfo().getHardware().getProcessor().getLogicalProcessorCount();
        JSONObject jsonObject = new JSONObject();
        JSONObject result = new JSONObject();
        new Thread(() -> {
            try {
                while (run_cpu_payload) {
                    Date nowDate = new Date();
                    long gapTime = nowDate.getTime() - BootApplication.bootDate.getTime();

                    long day = gapTime / (24 * 60 * 60 * 1000);
                    long hour = (gapTime / (60 * 60 * 1000) - day * 24);
                    long min = ((gapTime / (60 * 1000)) - day * 24 * 60 - hour * 60);
                    long s = (gapTime / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

                    jsonObject.put("cpu_payload", "系统已经运行了 " + day + "天" + hour + "小时" + min + "分钟" + s + "秒");
                    result.put("result", jsonObject);
                    WebSocketService.sendMessage(client_name, result.toString());
                    Thread.sleep(3000);
                }
                Thread.interrupted();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


//            try {
//                while (run_cpu_payload) {
//                    SystemInfo systemInfo = new SystemInfo();
//                    CentralProcessor processor = systemInfo.getHardware().getProcessor();
//                    long[] prevTicks = processor.getSystemCpuLoadTicks();
//                    // 间隔1000ms获取CPU TICK
//                    Thread.sleep(1000);
//                    long[] ticks = processor.getSystemCpuLoadTicks();
//
//                    long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
//                    long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
//                    long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
//                    long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
//                    long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
//                    long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
//                    long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
//                    long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
//                    long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
//
//                    jsonObject.put("cpu_payload", new DecimalFormat("#.##%").format((1.0-(idle * 1.0 / totalCpu)) * 2));
//                    result.put("result", jsonObject);
//                    WebSocketService.sendMessage(client_name, result.toString());
//                    Thread.sleep(3000);
//                }
//                Thread.interrupted();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }, "cpu_info").start();
    }
    @GetMapping("/stop_run_cpu_payload")
    public void stopRunCpuPayload() {
        run_cpu_payload = false;
    }
}
