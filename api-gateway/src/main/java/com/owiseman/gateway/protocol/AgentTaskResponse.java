package com.owiseman.gateway.protocol;

import com.owiseman.agent.planner.PlanStatus;

public record AgentTaskResponse(
        String agentId,
        String planId,
        PlanStatus status,
        long durationMs,
        String documentId
) {
    public static AgentTaskResponse from(com.owiseman.agent.Agent.AgentResult result) {
        return new AgentTaskResponse(
                result.agentId(),
                result.planId(),
                result.status(),
                result.durationMs(),
                null
        );
    }

    public static AgentTaskResponse from(com.owiseman.agent.Agent.AgentResult result, String documentId) {
        return new AgentTaskResponse(
                result.agentId(),
                result.planId(),
                result.status(),
                result.durationMs(),
                documentId
        );
    }
}
