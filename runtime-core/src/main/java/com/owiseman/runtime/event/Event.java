package com.owiseman.runtime.event;

import java.util.Collections;
import java.util.Map;

public final class Event {

    private final String type;
    private final Map<String, Object> data;
    private final long timestamp;

    public Event(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data != null ? Map.copyOf(data) : Map.of();
        this.timestamp = System.currentTimeMillis();
    }

    public String type() { return type; }
    public Map<String, Object> data() { return data; }
    public long timestamp() { return timestamp; }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    @Override
    public String toString() {
        return "Event{type=" + type + ", timestamp=" + timestamp + "}";
    }
}
