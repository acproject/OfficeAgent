package com.owiseman.agent.tool;

import java.util.Map;
import java.util.logging.Logger;

public final class ToolExecutor {

    private static final Logger LOG = Logger.getLogger(ToolExecutor.class.getName());

    private final ToolRegistry registry;

    public ToolExecutor(ToolRegistry registry) {
        this.registry = registry;
    }

    public Map<String, Object> execute(String toolName, Map<String, Object> args) {
        ToolDefinition tool = registry.getTool(toolName);
        if (tool == null) {
            LOG.warning("Tool not found: " + toolName);
            return Map.of("error", "Tool not found: " + toolName);
        }

        for (ToolDefinition.ParameterDef param : tool.parameters().values()) {
            if (param.required() && !args.containsKey(param.name())) {
                LOG.warning("Missing required parameter: " + param.name() + " for tool: " + toolName);
                return Map.of("error", "Missing required parameter: " + param.name());
            }
        }

        try {
            LOG.info("Executing tool: " + toolName);
            Map<String, Object> result = tool.execute(args);
            LOG.info("Tool " + toolName + " executed successfully");
            return result;
        } catch (Exception e) {
            LOG.warning("Tool execution failed: " + toolName + " - " + e.getMessage());
            return Map.of("error", "Tool execution failed: " + e.getMessage());
        }
    }
}
