package com.owiseman.launcher;

import com.owiseman.worker.protocol.WorkerConnection;
import com.owiseman.worker.protocol.WorkerType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class WorkerManager {

    private static final Logger LOG = Logger.getLogger(WorkerManager.class.getName());

    private final Map<WorkerType, WorkerConnection> connections = new ConcurrentHashMap<>();
    private final Map<WorkerType, Process> workerProcesses = new ConcurrentHashMap<>();
    private final ConfigLoader config;

    public WorkerManager(ConfigLoader config) {
        this.config = config;
    }

    public void startWorker(WorkerType type) throws IOException {
        LOG.info("Starting " + type + " worker...");

        String socketPath = switch (type) {
            case LLAMA_CPP -> config.get("worker.llama.socket", "/tmp/llama-worker.sock");
            case OCR -> config.get("worker.ocr.socket", "/tmp/ocr-worker.sock");
            case LAYOUT -> config.get("worker.layout.socket", "/tmp/layout-worker.sock");
            case RENDER -> config.get("worker.render.socket", "/tmp/render-worker.sock");
            case EMBEDDING -> config.get("worker.embedding.socket", "/tmp/embedding-worker.sock");
        };

        ProcessBuilder pb = createWorkerProcess(type);
        try {
            Process process = pb.start();
            workerProcesses.put(type, process);

            Thread.ofVirtual().name("worker-monitor-" + type.code()).start(() -> monitorWorker(type, process));

            WorkerConnection connection = new WorkerConnection(type, Path.of(socketPath));
            connection.connect();
            connections.put(type, connection);

            LOG.info(type + " worker started successfully");
        } catch (IOException e) {
            LOG.warning("Failed to start " + type + " worker: " + e.getMessage());
            throw e;
        }
    }

    public void stopWorker(WorkerType type) {
        LOG.info("Stopping " + type + " worker...");

        WorkerConnection connection = connections.remove(type);
        if (connection != null) {
            connection.disconnect();
        }

        Process process = workerProcesses.remove(type);
        if (process != null) {
            process.destroy();
            LOG.info(type + " worker process destroyed");
        }
    }

    public void stopAll() {
        for (WorkerType type : WorkerType.values()) {
            stopWorker(type);
        }
        LOG.info("All workers stopped");
    }

    public WorkerConnection getConnection(WorkerType type) {
        return connections.get(type);
    }

    public boolean isWorkerRunning(WorkerType type) {
        Process process = workerProcesses.get(type);
        return process != null && process.isAlive();
    }

    private ProcessBuilder createWorkerProcess(WorkerType type) {
        return switch (type) {
            case LLAMA_CPP -> {
                String modelPath = config.get("worker.llama.model.path", "");
                String ctxSize = config.get("worker.llama.context.size", "4096");
                String gpuLayers = config.get("worker.llama.gpu.layers", "0");
                yield new ProcessBuilder(
                        "llama-server",
                        "-m", modelPath,
                        "--ctx-size", ctxSize,
                        "--n-gpu-layers", gpuLayers
                );
            }
            case OCR -> new ProcessBuilder("office-ocr-worker");
            case LAYOUT -> new ProcessBuilder("office-layout-worker");
            case RENDER -> new ProcessBuilder("office-render-worker");
            case EMBEDDING -> new ProcessBuilder("office-embedding-worker");
        };
    }

    private void monitorWorker(WorkerType type, Process process) {
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOG.warning(type + " worker exited with code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
