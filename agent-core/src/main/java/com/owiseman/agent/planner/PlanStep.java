package com.owiseman.agent.planner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class PlanStep {

    private final String stepId;
    private final String description;
    private final String toolName;
    private final Map<String, Object> parameters;
    private final int order;
    private Status status;

    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        SKIPPED
    }

    private PlanStep(Builder builder) {
        this.stepId = Objects.requireNonNull(builder.stepId);
        this.description = builder.description;
        this.toolName = builder.toolName;
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.parameters));
        this.order = builder.order;
        this.status = Status.PENDING;
    }

    public String stepId() { return stepId; }
    public String description() { return description; }
    public String toolName() { return toolName; }
    public Map<String, Object> parameters() { return parameters; }
    public int order() { return order; }
    public Status status() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String stepId;
        private String description = "";
        private String toolName;
        private final Map<String, Object> parameters = new LinkedHashMap<>();
        private int order;

        public Builder stepId(String stepId) { this.stepId = stepId; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder toolName(String toolName) { this.toolName = toolName; return this; }
        public Builder parameter(String key, Object value) { this.parameters.put(key, value); return this; }
        public Builder order(int order) { this.order = order; return this; }
        public PlanStep build() { return new PlanStep(this); }
    }
}
