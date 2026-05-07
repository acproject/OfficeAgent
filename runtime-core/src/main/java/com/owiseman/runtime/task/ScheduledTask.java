package com.owiseman.runtime.task;

import java.util.concurrent.atomic.AtomicReference;

final class ScheduledTask implements Comparable<ScheduledTask> {

    private final String taskId;
    private final Runnable runnable;
    private final TaskPriority priority;
    private final AtomicReference<TaskStatus> status;
    private volatile boolean cancelled;

    ScheduledTask(String taskId, Runnable runnable, TaskPriority priority) {
        this.taskId = taskId;
        this.runnable = runnable;
        this.priority = priority;
        this.status = new AtomicReference<>(TaskStatus.PENDING);
        this.cancelled = false;
    }

    String taskId() { return taskId; }
    Runnable runnable() { return runnable; }
    TaskPriority priority() { return priority; }
    TaskStatus status() { return status.get(); }
    void setStatus(TaskStatus s) { status.set(s); }
    boolean isCancelled() { return cancelled; }
    void cancel() { cancelled = true; status.set(TaskStatus.CANCELLED); }

    @Override
    public int compareTo(ScheduledTask other) {
        return Integer.compare(other.priority.value(), this.priority.value());
    }
}
