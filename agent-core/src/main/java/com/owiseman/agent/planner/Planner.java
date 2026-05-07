package com.owiseman.agent.planner;

import com.owiseman.agent.memory.MemoryStore;
import com.owiseman.agent.tool.ToolRegistry;

import java.util.UUID;
import java.util.logging.Logger;

public final class Planner {

    private static final Logger LOG = Logger.getLogger(Planner.class.getName());

    private final ToolRegistry toolRegistry;
    private final MemoryStore memoryStore;

    public Planner(ToolRegistry toolRegistry, MemoryStore memoryStore) {
        this.toolRegistry = toolRegistry;
        this.memoryStore = memoryStore;
    }

    public Plan createPlan(String goal) {
        LOG.info("Creating plan for goal: " + goal);

        Plan.Builder planBuilder = Plan.builder()
                .planId("plan-" + UUID.randomUUID().toString().substring(0, 8))
                .goal(goal);

        String lowerGoal = goal.toLowerCase();
        if (lowerGoal.contains("ppt") || lowerGoal.contains("presentation") || lowerGoal.contains("幻灯片")) {
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-1").description("Analyze PPT requirements")
                    .toolName("ppt_analyze").order(1).build());
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-2").description("Generate PPT content")
                    .toolName("ppt_generate").order(2).build());
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-3").description("Apply styling and layout")
                    .toolName("ppt_style").order(3).build());
        } else if (lowerGoal.contains("word") || lowerGoal.contains("doc") || lowerGoal.contains("文档")) {
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-1").description("Analyze document structure")
                    .toolName("doc_analyze").order(1).build());
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-2").description("Rewrite document content")
                    .toolName("doc_rewrite").order(2).build());
        } else if (lowerGoal.contains("excel") || lowerGoal.contains("spreadsheet") || lowerGoal.contains("表格")) {
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-1").description("Analyze spreadsheet data")
                    .toolName("excel_analyze").order(1).build());
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-2").description("Process and transform data")
                    .toolName("excel_process").order(2).build());
        } else if (lowerGoal.contains("pdf")) {
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-1").description("Extract PDF content")
                    .toolName("pdf_extract").order(1).build());
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-2").description("Analyze and structure content")
                    .toolName("pdf_analyze").order(2).build());
        } else {
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-1").description("Analyze task")
                    .toolName("general_analyze").order(1).build());
            planBuilder.addStep(PlanStep.builder()
                    .stepId("step-2").description("Execute task")
                    .toolName("general_execute").order(2).build());
        }

        Plan plan = planBuilder.build();
        LOG.info("Created plan " + plan.planId() + " with " + plan.steps().size() + " steps");
        return plan;
    }
}
