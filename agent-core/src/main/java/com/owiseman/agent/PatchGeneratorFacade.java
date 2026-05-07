package com.owiseman.agent;

import com.owiseman.agent.patch.PatchGenerator;

public final class PatchGeneratorFacade {
    private final PatchGenerator delegate = new PatchGenerator();
    public PatchGenerator delegate() { return delegate; }
}
