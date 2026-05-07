package com.owiseman.launcher;

import com.owiseman.runtime.RuntimeContext;
import com.owiseman.runtime.event.EventBus;
import com.owiseman.runtime.lifecycle.LifecycleManager;
import com.owiseman.runtime.task.TaskScheduler;
import com.owiseman.runtime.task.VirtualThreadPool;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class RuntimeBootstrap {

    private static final Logger LOG = Logger.getLogger(RuntimeBootstrap.class.getName());

    private final ConfigLoader configLoader;
    private final WorkerManager workerManager;

    public RuntimeBootstrap(ConfigLoader configLoader) {
        this.configLoader = configLoader;
        this.workerManager = new WorkerManager(configLoader);
    }

    public RuntimeContext bootstrap() {
        LOG.info("Bootstrapping OfficeAgent Runtime...");

        configureLogging();

        RuntimeContext runtimeContext = RuntimeContext.initialize();

        registerEventHandlers(runtimeContext);

        LOG.info("OfficeAgent Runtime bootstrapped successfully");
        return runtimeContext;
    }

    public void startWorkers() {
        LOG.info("Starting worker processes...");

        try {
            if (configLoader.getBoolean("worker.llama.enabled", true)) {
                workerManager.startWorker(com.owiseman.worker.protocol.WorkerType.LLAMA_CPP);
            }
            if (configLoader.getBoolean("worker.ocr.enabled", true)) {
                workerManager.startWorker(com.owiseman.worker.protocol.WorkerType.OCR);
            }
            if (configLoader.getBoolean("worker.layout.enabled", true)) {
                workerManager.startWorker(com.owiseman.worker.protocol.WorkerType.LAYOUT);
            }
        } catch (Exception e) {
            LOG.warning("Some workers failed to start: " + e.getMessage());
        }

        LOG.info("Worker startup complete");
    }

    public void shutdown() {
        LOG.info("Shutting down OfficeAgent Runtime...");

        workerManager.stopAll();

        RuntimeContext.getInstance().shutdown();

        LOG.info("OfficeAgent Runtime shut down");
    }

    public ConfigLoader configLoader() { return configLoader; }
    public WorkerManager workerManager() { return workerManager; }

    private void configureLogging() {
        String logLevel = configLoader.get("logging.level", "INFO");
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.parse(logLevel));
        for (var handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.parse(logLevel));
        }
        LOG.info("Logging configured at level: " + logLevel);
    }

    private void registerEventHandlers(RuntimeContext ctx) {
        com.owiseman.runtime.event.EventBus eventBus = ctx.eventBus();

        eventBus.subscribe("worker.started", event ->
                LOG.info("Worker started: " + event.get("workerType")));

        eventBus.subscribe("worker.stopped", event ->
                LOG.info("Worker stopped: " + event.get("workerType")));

        eventBus.subscribe("task.completed", event ->
                LOG.fine("Task completed: " + event.get("taskId")));

        eventBus.subscribe("task.failed", event ->
                LOG.warning("Task failed: " + event.get("taskId") + " - " + event.get("error")));
    }
}
