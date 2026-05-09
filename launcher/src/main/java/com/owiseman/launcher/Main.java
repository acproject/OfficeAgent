package com.owiseman.launcher;

import com.owiseman.gateway.ApiGateway;
import com.owiseman.runtime.RuntimeContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        LOG.info("========================================");
        LOG.info("  OfficeAgent - AI Office Assistant");
        LOG.info("  Version: " + VERSION);
        LOG.info("  Java: " + System.getProperty("java.version"));
        LOG.info("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        LOG.info("========================================");

        ConfigLoader configLoader = new ConfigLoader();
        String mode = "headless";

        for (int i = 0; i < args.length; i++) {
            if ("--config".equals(args[i]) && i + 1 < args.length) {
                try {
                    configLoader.loadFromFile(Path.of(args[++i]));
                } catch (Exception e) {
                    LOG.severe("Failed to load config: " + e.getMessage());
                    System.exit(1);
                }
            } else if ("--mode".equals(args[i]) && i + 1 < args.length) {
                mode = args[++i];
            } else if ("--port".equals(args[i]) && i + 1 < args.length) {
                configLoader.set("gateway.port", args[++i]);
            } else if ("--help".equals(args[i]) || "-h".equals(args[i])) {
                printUsage();
                return;
            }
        }

        RuntimeBootstrap bootstrap = new RuntimeBootstrap(configLoader);
        RuntimeContext runtimeContext = bootstrap.bootstrap();
        bootstrap.startWorkers();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook triggered");
            bootstrap.shutdown();
        }));

        switch (mode) {
            case "desktop" -> launchDesktop(configLoader);
            case "headless" -> launchHeadless(configLoader);
            default -> {
                LOG.warning("Unknown mode: " + mode + ", falling back to headless");
                launchHeadless(configLoader);
            }
        }
    }

    private static void launchHeadless(ConfigLoader configLoader) {
        int port = configLoader.getInt("gateway.port", 18080);
        String host = configLoader.get("gateway.host", "0.0.0.0");

        try {
            ApiGateway gateway = new ApiGateway(host, port);
            gateway.start();

            LOG.info("========================================");
            LOG.info("  OfficeAgent is running in HEADLESS mode");
            LOG.info("  REST API:   http://" + host + ":" + port + "/api/v1/");
            LOG.info("  WebSocket:  ws://" + host + ":" + port + "/api/v1/ws");
            LOG.info("  Dashboard:  http://" + host + ":" + port + "/");
            LOG.info("========================================");

            Thread.currentThread().join();
        } catch (IOException e) {
            LOG.severe("Failed to start API Gateway: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void launchDesktop(ConfigLoader configLoader) {
        LOG.info("Launching Desktop UI...");
        com.owiseman.desktop.DesktopApp.launchApp(new String[]{});
    }

    private static void printUsage() {
        System.out.println("""
                OfficeAgent - AI Office Assistant v""" + VERSION + """

                
                Usage: office-agent [options]
                
                Options:
                  --mode <mode>       Run mode: headless (default), desktop
                  --config <path>     Configuration file path
                  --port <port>       API Gateway port (default: 18080)
                  --help, -h          Show this help message
                
                Modes:
                  headless   Run as background service with REST API + WebSocket
                  desktop    Launch JavaFX desktop application
                
                API Endpoints (headless mode):
                  POST /api/v1/agent/execute     Execute an agent task
                  POST /api/v1/agent/patch       Generate document patch
                  GET  /api/v1/agent/status      Get agent status
                  POST /api/v1/document/import   Import a document
                  GET  /api/v1/document/{id}     Get document details
                  POST /api/v1/document/export   Export a document
                  GET  /api/v1/system/info       System information
                  GET  /api/v1/system/health     Health check
                  GET  /api/v1/system/workers    Worker status
                """);
    }
}
