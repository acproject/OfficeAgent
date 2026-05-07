package com.owiseman.agent;

import com.owiseman.agent.memory.MemoryStore;

public final class MemoryStoreFacade {
    private final MemoryStore delegate = new MemoryStore();
    public MemoryStore delegate() { return delegate; }
}
