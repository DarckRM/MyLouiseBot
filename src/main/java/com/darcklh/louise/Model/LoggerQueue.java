package com.darcklh.louise.Model;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author DarckLH
 * @date 2021/11/20 17:09
 * @Description
 */
public class LoggerQueue {

    // 队列大小
    public static final int QUEUE_MAX_SIZE = 10000;
    // 阻塞队列
    private BlockingQueue blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    private LoggerQueue()
    {
        if (Inner.instance != null){
            throw new RuntimeException("实例已经存在，请通过 getInstance()方法获取");
        }
    }
    private static class Inner {
        private static LoggerQueue instance = new LoggerQueue();
    }

    public static LoggerQueue getInstance()
    {
        return Inner.instance;
    }

    /**
     * 消息入队
     *
     * @param log
     * @return
     */
    public boolean push(LoggerMessage log)
    {
        return this.blockingQueue.add(log);// 队列满了就抛出异常，不阻塞
    }

    /**
     * 消息出队
     *
     * @return
     */
    public LoggerMessage poll()
    {
        LoggerMessage result = null;
        try
        {
            result = (LoggerMessage) this.blockingQueue.take();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
