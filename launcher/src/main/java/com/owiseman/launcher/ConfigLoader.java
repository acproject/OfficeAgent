package com.owiseman.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public final class ConfigLoader {

    private static final Logger LOG = Logger.getLogger(ConfigLoader.class.getName());

    private final Map<String, String> config = new LinkedHashMap<>();
    private Path configPath;

    public ConfigLoader() {
        loadDefaults();
    }

    private void loadDefaults() {
        config.put("agent.name", "OfficeAgent");
        config.put("agent.version", "1.0.0");
        config.put("runtime.workers", String.valueOf(Runtime.getRuntime().availableProcessors()));
        config.put("runtime.virtual.threads.enabled", "true");
        config.put("ipc.socket.path", "/tmp/office-agent.sock");
        config.put("ipc.pipe.path", "/tmp/office-agent-pipe");
        config.put("worker.llama.socket", "/tmp/llama-worker.sock");
        config.put("worker.ocr.socket", "/tmp/ocr-worker.sock");
        config.put("worker.layout.socket", "/tmp/layout-worker.sock");
        config.put("worker.render.socket", "/tmp/render-worker.sock");
        config.put("worker.llama.model.path", "");
        config.put("worker.llama.context.size", "4096");
        config.put("worker.llama.gpu.layers", "0");
        config.put("logging.level", "INFO");
        config.put("logging.path", "logs");
        config.put("gateway.host", "0.0.0.0");
        config.put("gateway.port", "18080");
        config.put("gateway.cors.enabled", "true");
    }

    public void loadFromFile(Path path) throws IOException {
        this.configPath = path;
        if (!Files.exists(path)) {
            LOG.warning("Config file not found: " + path + ", using defaults");
            return;
        }

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(path)) {
            props.load(is);
        }

        for (String key : props.stringPropertyNames()) {
            config.put(key, props.getProperty(key));
        }
        LOG.info("Loaded configuration from: " + path);
    }

    public String get(String key) {
        return config.get(key);
    }

    public String get(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String value = config.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = config.get(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    public void set(String key, String value) {
        config.put(key, value);
    }

    public Map<String, String> allConfig() {
        return Collections.unmodifiableMap(config);
    }

    public Path configPath() {
        return configPath;
    }
}
