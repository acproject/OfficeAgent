package com.owiseman.agent;

import com.owiseman.agent.tool.ToolRegistry;

public final class ToolRegistryFacade {
    private final ToolRegistry delegate = new ToolRegistry();
    public ToolRegistry delegate() { return delegate; }
}
