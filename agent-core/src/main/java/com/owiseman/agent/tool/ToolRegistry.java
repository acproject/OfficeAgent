package com.owiseman.agent.tool;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class ToolRegistry {

    private static final Logger LOG = Logger.getLogger(ToolRegistry.class.getName());

    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();

    public void register(ToolDefinition tool) {
        tools.put(tool.name(), tool);
        LOG.info("Registered tool: " + tool.name());
    }

    public void unregister(String toolName) {
        tools.remove(toolName);
        LOG.info("Unregistered tool: " + toolName);
    }

    public ToolDefinition getTool(String name) {
        return tools.get(name);
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    public Collection<ToolDefinition> allTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

    public Collection<String> toolNames() {
        return Collections.unmodifiableCollection(tools.keySet());
    }

    public int toolCount() {
        return tools.size();
    }
}
