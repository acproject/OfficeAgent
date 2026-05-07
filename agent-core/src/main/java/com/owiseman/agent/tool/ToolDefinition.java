package com.owiseman.agent.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class ToolDefinition {

    private final String name;
    private final String description;
    private final Map<String, ParameterDef> parameters;
    private final Function<Map<String, Object>, Map<String, Object>> executor;

    private ToolDefinition(Builder builder) {
        this.name = Objects.requireNonNull(builder.name);
        this.description = builder.description;
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.parameters));
        this.executor = Objects.requireNonNull(builder.executor);
    }

    public String name() { return name; }
    public String description() { return description; }
    public Map<String, ParameterDef> parameters() { return parameters; }

    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> args) {
        return executor.apply(args);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String name;
        private String description = "";
        private final Map<String, ParameterDef> parameters = new LinkedHashMap<>();
        private Function<Map<String, Object>, Map<String, Object>> executor;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder parameter(String name, Class<?> type, boolean required) {
            this.parameters.put(name, new ParameterDef(name, type, required));
            return this;
        }
        public Builder executor(Function<Map<String, Object>, Map<String, Object>> executor) {
            this.executor = executor; return this;
        }
        public ToolDefinition build() { return new ToolDefinition(this); }
    }

    public record ParameterDef(String name, Class<?> type, boolean required) {}
}
