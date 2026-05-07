package com.owiseman.agent;

import com.owiseman.agent.planner.Planner;
import com.owiseman.agent.tool.ToolRegistry;
import com.owiseman.agent.memory.MemoryStore;

public final class PlannerFacade {
    private final Planner delegate;

    public PlannerFacade(ToolRegistry toolRegistry, MemoryStore memoryStore) {
        this.delegate = new Planner(toolRegistry, memoryStore);
    }

    public Planner delegate() { return delegate; }
}
