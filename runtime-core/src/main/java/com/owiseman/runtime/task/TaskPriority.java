package com.owiseman.runtime.task;

public enum TaskPriority {
    LOW(0),
    NORMAL(1),
    HIGH(2),
    URGENT(3);

    private final int value;

    TaskPriority(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
