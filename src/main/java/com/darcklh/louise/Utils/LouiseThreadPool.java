package com.darcklh.louise.Utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

public class LouiseThreadPool {
    private final int corePoolSize;
    private final int maximumPoolSize;
    private static ExecutorService executor = null;

    private static final class LouiseThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r);
        }
    }

    private final class LouiseRejectPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        }
    }

    public LouiseThreadPool(int corePoolSize, int maximumPoolSize) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        ThreadFactory factory = new CustomizableThreadFactory("LouisePool-");
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(999);
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS, queue, factory, new ThreadPoolExecutor.DiscardPolicy());
    }

    public static void execute(@NotNull Runnable command) {
        executor.execute(command);
    }
}
