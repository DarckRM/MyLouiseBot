package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.LoggerQueue;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.SysUser;
import com.darcklh.louise.Service.SysUserService;
import com.darcklh.louise.Service.WebSocketService;
import com.darcklh.louise.Utils.PluginManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Arrays;
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

    @Autowired
    PluginManager pluginManager;

    static private boolean output_log = true;
    private static Map<String, Integer> lengthMap = new ConcurrentHashMap<>();

    @RequestMapping("/error")
    public JSONObject error() {
        JSONObject jsonObject = new JSONObject();
        Result<String> result = new Result();
        result.setMsg("资源未找到");
        result.setData("/404");
        jsonObject.put("result", result);
        return jsonObject;
    }

    @RequestMapping("saito/login")
    public Result<SysUser> Login(@RequestBody SysUser sysUser) {

        Result<SysUser> result = sysUserService.Login(sysUser);

        return result;
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
        output_log = true;
        lengthMap.put(client_name, 1);//默认从第一行开始
        //获取日志信息
        new Thread(() -> {
            log.info("日志输出任务开始");
            while(output_log) {
                boolean first = true;
                BufferedReader reader = null;
                try {
                    //日志文件路径，获取最新的
                    String filePath = "logs/mylouise.log";
                    //字符流
                    reader = new BufferedReader(new FileReader(filePath));
                    Object[] lines = reader.lines().toArray();

                    //只取从上次之后产生的日志
                    Object[] copyOfRange = Arrays.copyOfRange(lines, lengthMap.get(client_name), lines.length);

                    //对日志进行着色，更加美观  PS：注意，这里要根据日志生成规则来操作
                    for (int i = 0; i < copyOfRange.length; i++) {
                        String line = (String) copyOfRange[i];
                        //先转义
                        line = line.replaceAll("&", "&amp;")
                                .replaceAll("<", "&lt;")
                                .replaceAll(">", "&gt;")
                                .replaceAll("\"", "&quot;");

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
                                line = split[0] + "]" + "<span style='color: #298a8a;'>" + split1[0] + "</span>" + "-" + split1[1];
                            }
                        }

                        copyOfRange[i] = line;
                    }

                    //存储最新一行开始
                    lengthMap.put(client_name, lines.length);

                    //第一次如果太大，截取最新的200行就够了，避免传输的数据太大
                    if(first && copyOfRange.length > 200){
                        copyOfRange = Arrays.copyOfRange(copyOfRange, copyOfRange.length - 200, copyOfRange.length);
                        first = false;
                    }
                    for (Object o : copyOfRange) {
                        o += "<br/>";
                    }
                    String result = copyOfRange.toString();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("result", result);
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
        }, "output-log").start();

    }

}
