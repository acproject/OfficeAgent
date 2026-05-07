module office.agent.worker {
    requires java.base;
    requires java.logging;
    requires office.agent.runtime;
    requires office.agent.ipc;

    exports com.owiseman.worker;
    exports com.owiseman.worker.protocol;
}
