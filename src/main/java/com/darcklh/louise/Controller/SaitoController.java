package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.SysUser;
import com.darcklh.louise.Service.SysUserService;
import com.darcklh.louise.Service.Impl.WebSocketService;
import com.darcklh.louise.Utils.LouiseThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DarckLH
 * @date 2021/9/8 18:42
 * @Description 处理后台请求
 */
@Slf4j
@RestController
public class SaitoController {

    @Autowired
    SysUserService sysUserService;

    static private boolean output_log = true;
    private static Map<String, Integer> lengthMap = new ConcurrentHashMap<>();

    @RequestMapping("/error")
    public JSONObject error() {
        JSONObject jsonObject = new JSONObject();
        Result<String> result = new Result<>();
        result.setMsg("资源未找到");
        result.setData("/404");
        jsonObject.put("result", result);
        return jsonObject;
    }

    @RequestMapping("saito/login")
    public Result<SysUser> Login(@RequestBody SysUser sysUser) {
        return sysUserService.Login(sysUser);
    }

    /**
     * 停止输出日志信息的线程
     */
    @GetMapping("saito/stop_output")
    public void stopOutputLog() {
        output_log = false;
        log.info("日志输出任务结束");
    }

    /**
     * 输出日志信息
     */
    @GetMapping("saito/output_log/{client_name}")
    public void outputLog(@PathVariable String client_name) {
        LinkedList<String> output = new LinkedList<>();
        output_log = true;
        lengthMap.put(client_name, 1);//默认从第一行开始
        //获取日志信息
        LouiseThreadPool.execute(() -> {
            log.info("日志输出任务开始");
            //日志文件路径，获取最新的
            String filePath = "logs/mylouise.log";
            while(output_log) {
                BufferedReader reader = null;
                String line;
                try {
                    //字符流
                    reader = new BufferedReader(new FileReader(filePath));
                    while ((line = reader.readLine()) != null) {
                        //对日志进行着色，更加美观  PS：注意，这里要根据日志生成规则来操作
                        //处理等级
                        line = line.replace("DEBUG", "<span style='color: blue;'>DEBUG</span>");
                        line = line.replace("INFO", "<span style='color: green;'>INFO</span>");
                        line = line.replace("WARN", "<span style='color: orange;'>WARN</span>");
                        line = line.replace("ERROR", "<span style='color: red;'>ERROR</span>");

                        //处理类名
                        String[] split = line.split("]");
                        if (split.length >= 2) {
                            String[] split1 = split[1].split("-");
                            if (split1.length >= 2) {
                                line = split[0] + "]" + "<span style='color: #298a8a;'>" + split1[0] + "</span>" + "-";
                                for (int i = 1; i < split1.length; i++)
                                    line += split1[i] + "-";
                            }
                        }
                        // 队列中写入新行
                        output.addLast(line + "<br/>");
                        if (output.size() > 200)
                            output.removeFirst();
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("result", output);
                    //发送
                    WebSocketService.sendMessage(client_name, jsonObject.toString());
                    //休眠一秒
                    Thread.sleep(1000);
                } catch (Exception e) {
                    //捕获但不处理
                    e.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                        log.info("日志输出任务结束");
                    }
                }
            }
            Thread.interrupted();
        });

    }

}
