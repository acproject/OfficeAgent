module office.agent.gateway {
    requires java.base;
    requires java.logging;
    requires java.net.http;
    requires jdk.httpserver;

    requires office.agent.runtime;
    requires office.agent.core;
    requires office.agent.document;
    requires office.agent.office;
    requires office.agent.pdf;
    requires office.agent.worker;

    exports com.owiseman.gateway;
    exports com.owiseman.gateway.handler;
    exports com.owiseman.gateway.protocol;
    exports com.owiseman.gateway.websocket;
}
