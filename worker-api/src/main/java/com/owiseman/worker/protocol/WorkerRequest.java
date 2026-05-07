package com.owiseman.worker.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class WorkerRequest {

    private final String taskId;
    private final WorkerType workerType;
    private final byte[] payload;
    private final Map<String, String> params;
    private final long timestamp;
    private final int timeoutMs;

    private WorkerRequest(Builder builder) {
        this.taskId = Objects.requireNonNull(builder.taskId);
        this.workerType = Objects.requireNonNull(builder.workerType);
        this.payload = builder.payload != null ? builder.payload.clone() : new byte[0];
        this.params = Collections.unmodifiableMap(new LinkedHashMap<>(builder.params));
        this.timestamp = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
        this.timeoutMs = builder.timeoutMs > 0 ? builder.timeoutMs : 30000;
    }

    public String taskId() { return taskId; }
    public WorkerType workerType() { return workerType; }
    public byte[] payload() { return payload.clone(); }
    public Map<String, String> params() { return params; }
    public long timestamp() { return timestamp; }
    public int timeoutMs() { return timeoutMs; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String taskId;
        private WorkerType workerType;
        private byte[] payload;
        private final Map<String, String> params = new LinkedHashMap<>();
        private long timestamp;
        private int timeoutMs;

        public Builder taskId(String taskId) { this.taskId = taskId; return this; }
        public Builder workerType(WorkerType workerType) { this.workerType = workerType; return this; }
        public Builder payload(byte[] payload) { this.payload = payload; return this; }
        public Builder param(String key, String value) { this.params.put(key, value); return this; }
        public Builder params(Map<String, String> params) { this.params.putAll(params); return this; }
        public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }
        public Builder timeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; return this; }
        public WorkerRequest build() { return new WorkerRequest(this); }
    }
}
