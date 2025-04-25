package com.gaia3d.mago.server.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutionThreadPoolService {
    private static final int THREAD_COUNT = 3;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public static int getActiveCount() {
        return ((ThreadPoolExecutor) executor).getActiveCount();
    }

    public static void shutdown() {
        executor.shutdown();
    }

    public static boolean isShutdown() {
        return executor.isShutdown();
    }

    public static boolean isTerminated() {
        return executor.isTerminated();
    }

    public static void shutdownNow() {
        executor.shutdownNow();
    }
}
