module office.agent.ipc {
    requires java.base;
    requires java.logging;
    requires office.agent.runtime;
    requires com.google.protobuf;

    exports com.owiseman.ipc;
    exports com.owiseman.ipc.pipe;
    exports com.owiseman.ipc.codec;
}
