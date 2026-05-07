package com.owiseman.runtime.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class VirtualThreadPool {

    private static final Logger LOG = Logger.getLogger(VirtualThreadPool.class.getName());

    private volatile ThreadPoolExecutor executor;
    private final AtomicInteger taskCount = new AtomicInteger(0);

    public void start() {
        executor = new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Thread.ofVirtual().name("office-agent-worker-", 0).factory()
        );
        LOG.info("Virtual thread pool started");
    }

    public void stop() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOG.info("Virtual thread pool stopped. Total tasks executed: " + taskCount.get());
        }
    }

    public Future<?> submit(Runnable task) {
        taskCount.incrementAndGet();
        return executor.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        taskCount.incrementAndGet();
        return executor.submit(task);
    }

    public void execute(Runnable task) {
        taskCount.incrementAndGet();
        executor.execute(task);
    }

    public int activeTaskCount() {
        return executor != null ? executor.getActiveCount() : 0;
    }

    public long completedTaskCount() {
        return executor != null ? executor.getCompletedTaskCount() : 0;
    }

    public int totalSubmittedTasks() {
        return taskCount.get();
    }
}
