package com.owiseman.agent;

import com.owiseman.agent.memory.MemoryStore;
import com.owiseman.agent.patch.PatchGenerator;
import com.owiseman.agent.planner.Plan;
import com.owiseman.agent.planner.Planner;
import com.owiseman.agent.planner.PlanStep;
import com.owiseman.agent.tool.ToolExecutor;
import com.owiseman.agent.tool.ToolRegistry;
import com.owiseman.document.patch.PatchSet;
import com.owiseman.runtime.RuntimeContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class Agent {

    private static final Logger LOG = Logger.getLogger(Agent.class.getName());

    private final String agentId;
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final MemoryStore memoryStore;
    private final Planner planner;
    private final PatchGenerator patchGenerator;

    public Agent() {
        this.agentId = "agent-" + UUID.randomUUID().toString().substring(0, 8);
        this.toolRegistry = new ToolRegistry();
        this.memoryStore = new MemoryStore();
        this.toolExecutor = new ToolExecutor(toolRegistry);
        this.planner = new Planner(toolRegistry, memoryStore);
        this.patchGenerator = new PatchGenerator();
    }

    public CompletableFuture<AgentResult> execute(String goal) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            LOG.info("Agent " + agentId + " executing goal: " + goal);

            memoryStore.addGlobal(MemoryStore.MemoryEntry.of("user", goal));

            Plan plan = planner.createPlan(goal);
            plan.setStatus(com.owiseman.agent.planner.PlanStatus.RUNNING);

            for (PlanStep step : plan.steps()) {
                step.setStatus(PlanStep.Status.RUNNING);
                LOG.info("Executing step: " + step.description());

                try {
                    Map<String, Object> result = toolExecutor.execute(step.toolName(), step.parameters());
                    step.setStatus(PlanStep.Status.COMPLETED);
                    memoryStore.addGlobal(MemoryStore.MemoryEntry.of("assistant",
                            "Step " + step.stepId() + " completed: " + result));
                } catch (Exception e) {
                    step.setStatus(PlanStep.Status.FAILED);
                    LOG.warning("Step " + step.stepId() + " failed: " + e.getMessage());
                    memoryStore.addGlobal(MemoryStore.MemoryEntry.of("system",
                            "Step " + step.stepId() + " failed: " + e.getMessage()));
                }
            }

            plan.setStatus(plan.isComplete()
                    ? com.owiseman.agent.planner.PlanStatus.COMPLETED
                    : com.owiseman.agent.planner.PlanStatus.FAILED);

            long duration = System.currentTimeMillis() - startTime;
            LOG.info("Agent " + agentId + " completed in " + duration + "ms, status: " + plan.status());

            return new AgentResult(agentId, plan.planId(), plan.status(), duration);
        });
    }

    public PatchSet generatePatch(String documentId, String instruction) {
        LOG.info("Generating patch for document " + documentId + ": " + instruction);
        return patchGenerator.generateReplaceText(documentId, "block-placeholder", instruction);
    }

    public ToolRegistry toolRegistry() { return toolRegistry; }
    public MemoryStore memoryStore() { return memoryStore; }
    public PatchGenerator patchGenerator() { return patchGenerator; }
    public String agentId() { return agentId; }

    public record AgentResult(String agentId, String planId,
                              com.owiseman.agent.planner.PlanStatus status, long durationMs) {}
}
