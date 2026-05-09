module office.agent.desktop {
    requires java.base;
    requires java.logging;
    requires java.net.http;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    requires office.agent.gateway;
    requires office.agent.core;
    requires office.agent.document;
    requires office.agent.runtime;
    requires office.agent.office;
    requires office.agent.pdf;

    exports com.owiseman.desktop;
    exports com.owiseman.desktop.controller;
    exports com.owiseman.desktop.service;
}
