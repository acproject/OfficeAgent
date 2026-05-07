package com.owiseman.worker.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class WorkerResponse {

    public enum Status {
        SUCCESS,
        FAILED,
        TIMEOUT,
        WORKER_NOT_FOUND,
        INVALID_REQUEST
    }

    private final String taskId;
    private final Status status;
    private final byte[] payload;
    private final String errorMessage;
    private final Map<String, String> metadata;
    private final long processingTimeMs;

    private WorkerResponse(Builder builder) {
        this.taskId = Objects.requireNonNull(builder.taskId);
        this.status = Objects.requireNonNull(builder.status);
        this.payload = builder.payload != null ? builder.payload.clone() : new byte[0];
        this.errorMessage = builder.errorMessage;
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
        this.processingTimeMs = builder.processingTimeMs;
    }

    public String taskId() { return taskId; }
    public Status status() { return status; }
    public byte[] payload() { return payload.clone(); }
    public String errorMessage() { return errorMessage; }
    public Map<String, String> metadata() { return metadata; }
    public long processingTimeMs() { return processingTimeMs; }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public static Builder builder() { return new Builder(); }

    public static Builder success(String taskId, byte[] payload) {
        return builder().taskId(taskId).status(Status.SUCCESS).payload(payload);
    }

    public static Builder failure(String taskId, String errorMessage) {
        return builder().taskId(taskId).status(Status.FAILED).errorMessage(errorMessage);
    }

    public static final class Builder {
        private String taskId;
        private Status status;
        private byte[] payload;
        private String errorMessage;
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private long processingTimeMs;

        public Builder taskId(String taskId) { this.taskId = taskId; return this; }
        public Builder status(Status status) { this.status = status; return this; }
        public Builder payload(byte[] payload) { this.payload = payload; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder metadata(String key, String value) { this.metadata.put(key, value); return this; }
        public Builder processingTimeMs(long ms) { this.processingTimeMs = ms; return this; }
        public WorkerResponse build() { return new WorkerResponse(this); }
    }
}
