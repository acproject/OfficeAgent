module office.agent.core {
    requires java.base;
    requires java.logging;
    requires office.agent.runtime;
    requires office.agent.document;
    requires office.agent.worker;

    exports com.owiseman.agent;
    exports com.owiseman.agent.planner;
    exports com.owiseman.agent.tool;
    exports com.owiseman.agent.memory;
    exports com.owiseman.agent.patch;
}
