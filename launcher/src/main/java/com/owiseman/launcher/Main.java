package com.owiseman.launcher;

import com.owiseman.agent.Agent;
import com.owiseman.runtime.RuntimeContext;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LOG.info("========================================");
        LOG.info("  OfficeAgent - AI Office Assistant");
        LOG.info("  Version: 1.0.0");
        LOG.info("  Java: " + System.getProperty("java.version"));
        LOG.info("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        LOG.info("========================================");

        ConfigLoader configLoader = new ConfigLoader();

        for (int i = 0; i < args.length; i++) {
            if ("--config".equals(args[i]) && i + 1 < args.length) {
                try {
                    configLoader.loadFromFile(Path.of(args[++i]));
                } catch (Exception e) {
                    LOG.severe("Failed to load config: " + e.getMessage());
                    System.exit(1);
                }
            }
        }

        RuntimeBootstrap bootstrap = new RuntimeBootstrap(configLoader);

        RuntimeContext runtimeContext = bootstrap.bootstrap();

        bootstrap.startWorkers();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook triggered");
            bootstrap.shutdown();
        }));

        LOG.info("OfficeAgent started successfully. Ready to process tasks.");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
