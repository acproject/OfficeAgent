package com.owiseman.desktop;

import com.owiseman.desktop.controller.MainController;
import com.owiseman.gateway.ApiGateway;
import com.owiseman.runtime.RuntimeContext;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.util.logging.Logger;

public final class DesktopApp extends Application {

    private static final Logger LOG = Logger.getLogger(DesktopApp.class.getName());

    private ApiGateway apiGateway;

    @Override
    public void init() throws Exception {
        LOG.info("Initializing OfficeAgent Desktop...");

        if (!RuntimeContext.isInitializedStatic()) {
            RuntimeContext.initialize();
        }

        apiGateway = new ApiGateway("0.0.0.0", 18080);
        apiGateway.start();
        LOG.info("API Gateway started on port 18080");
    }

    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController();

        Scene scene = new Scene(controller.createMainLayout(primaryStage), 1200, 800);
        scene.getStylesheets().clear();

        primaryStage.setTitle("OfficeAgent - AI Office Assistant");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        LOG.info("Desktop UI started");
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down OfficeAgent Desktop...");

        if (apiGateway != null) {
            apiGateway.stop();
        }

        if (RuntimeContext.isInitializedStatic()) {
            RuntimeContext.getInstance().shutdown();
        }

        LOG.info("OfficeAgent Desktop shut down");
    }

    public static void launchApp(String[] args) {
        launch(args);
    }
}
