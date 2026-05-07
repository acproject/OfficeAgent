package com.owiseman.runtime.task;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public final class TaskScheduler {

    private static final Logger LOG = Logger.getLogger(TaskScheduler.class.getName());

    private final AtomicLong taskIdGenerator = new AtomicLong(0);
    private final Map<String, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final PriorityQueue<ScheduledTask> priorityQueue = new PriorityQueue<>();
    private VirtualThreadPool threadPool;
    private volatile boolean running;

    public void start(VirtualThreadPool pool) {
        this.threadPool = pool;
        this.running = true;
        LOG.info("TaskScheduler started");
    }

    public void stop() {
        this.running = false;
        tasks.clear();
        synchronized (priorityQueue) {
            priorityQueue.clear();
        }
        LOG.info("TaskScheduler stopped");
    }

    public String schedule(Runnable task, TaskPriority priority) {
        String taskId = "task-" + taskIdGenerator.incrementAndGet();
        ScheduledTask scheduled = new ScheduledTask(taskId, task, priority);
        tasks.put(taskId, scheduled);
        synchronized (priorityQueue) {
            priorityQueue.offer(scheduled);
        }
        threadPool.execute(() -> executeTask(scheduled));
        LOG.fine("Scheduled task: " + taskId + " with priority " + priority);
        return taskId;
    }

    public String schedule(Runnable task) {
        return schedule(task, TaskPriority.NORMAL);
    }

    public <T> CompletableFuture<T> scheduleCallable(Callable<T> task, TaskPriority priority) {
        String taskId = "task-" + taskIdGenerator.incrementAndGet();
        CompletableFuture<T> future = new CompletableFuture<>();
        ScheduledTask scheduled = new ScheduledTask(taskId, () -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, priority);
        tasks.put(taskId, scheduled);
        threadPool.execute(() -> executeTask(scheduled));
        return future;
    }

    public <T> CompletableFuture<T> scheduleCallable(Callable<T> task) {
        return scheduleCallable(task, TaskPriority.NORMAL);
    }

    public boolean cancelTask(String taskId) {
        ScheduledTask task = tasks.remove(taskId);
        if (task != null) {
            task.cancel();
            LOG.fine("Cancelled task: " + taskId);
            return true;
        }
        return false;
    }

    public TaskStatus getTaskStatus(String taskId) {
        ScheduledTask task = tasks.get(taskId);
        return task != null ? task.status() : TaskStatus.UNKNOWN;
    }

    private void executeTask(ScheduledTask task) {
        if (task.isCancelled()) return;
        task.setStatus(TaskStatus.RUNNING);
        try {
            task.runnable().run();
            task.setStatus(TaskStatus.COMPLETED);
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            LOG.warning("Task " + task.taskId() + " failed: " + e.getMessage());
        } finally {
            tasks.remove(task.taskId());
        }
    }

    public int pendingTaskCount() {
        return tasks.size();
    }
}
