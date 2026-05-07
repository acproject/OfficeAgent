module office.agent.runtime {
    requires java.base;
    requires java.logging;

    exports com.owiseman.runtime;
    exports com.owiseman.runtime.event;
    exports com.owiseman.runtime.task;
    exports com.owiseman.runtime.lifecycle;
}
