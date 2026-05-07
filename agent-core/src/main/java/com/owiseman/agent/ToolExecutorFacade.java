package com.owiseman.agent;

import com.owiseman.agent.tool.ToolExecutor;
import com.owiseman.agent.tool.ToolRegistry;

public final class ToolExecutorFacade {
    private final ToolExecutor delegate;
    public ToolExecutorFacade(ToolRegistry registry) { this.delegate = new ToolExecutor(registry); }
    public ToolExecutor delegate() { return delegate; }
}
