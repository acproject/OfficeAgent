package com.owiseman.agent.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Plan {

    private final String planId;
    private final String goal;
    private final List<PlanStep> steps;
    private PlanStatus status;

    private Plan(Builder builder) {
        this.planId = Objects.requireNonNull(builder.planId);
        this.goal = builder.goal;
        this.steps = Collections.unmodifiableList(new ArrayList<>(builder.steps));
        this.status = PlanStatus.CREATED;
    }

    public String planId() { return planId; }
    public String goal() { return goal; }
    public List<PlanStep> steps() { return steps; }
    public PlanStatus status() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }

    public PlanStep currentStep() {
        return steps.stream()
                .filter(s -> s.status() == PlanStep.Status.PENDING || s.status() == PlanStep.Status.RUNNING)
                .findFirst()
                .orElse(null);
    }

    public boolean isComplete() {
        return steps.stream().allMatch(s -> s.status() == PlanStep.Status.COMPLETED);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String planId;
        private String goal = "";
        private final List<PlanStep> steps = new ArrayList<>();

        public Builder planId(String planId) { this.planId = planId; return this; }
        public Builder goal(String goal) { this.goal = goal; return this; }
        public Builder addStep(PlanStep step) { this.steps.add(step); return this; }
        public Plan build() { return new Plan(this); }
    }
}
