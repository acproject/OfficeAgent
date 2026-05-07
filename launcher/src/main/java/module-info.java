module office.agent.launcher {
    requires java.base;
    requires java.logging;

    requires office.agent.runtime;
    requires office.agent.document;
    requires office.agent.ipc;
    requires office.agent.worker;
    requires office.agent.core;
    requires office.agent.office;
    requires office.agent.pdf;
    requires office.agent.nativebridge;

    exports com.owiseman.launcher;
}
