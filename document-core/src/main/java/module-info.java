module office.agent.document {
    requires java.base;
    requires java.logging;
    requires office.agent.runtime;

    exports com.owiseman.document;
    exports com.owiseman.document.model;
    exports com.owiseman.document.patch;
    exports com.owiseman.document.ir;
}
