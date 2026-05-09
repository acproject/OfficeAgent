package com.owiseman.gateway.handler;

import com.owiseman.gateway.protocol.ApiResponse;
import com.owiseman.runtime.RuntimeContext;
import com.owiseman.worker.protocol.WorkerType;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public final class SystemHandler implements RequestHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleCors(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/system/info")) {
            handleInfo(exchange);
        } else if (path.endsWith("/system/health")) {
            handleHealth(exchange);
        } else if (path.endsWith("/system/workers")) {
            handleWorkers(exchange);
        } else {
            sendError(exchange, 404, "Not found: " + path);
        }
    }

    private void handleInfo(HttpExchange exchange) throws IOException {
        var info = Map.of(
                "name", "OfficeAgent",
                "version", "1.0.0",
                "javaVersion", System.getProperty("java.version"),
                "os", System.getProperty("os.name") + " " + System.getProperty("os.arch"),
                "processors", Runtime.getRuntime().availableProcessors(),
                "maxMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB",
                "virtualThreadsEnabled", true
        );
        sendOk(exchange, toJson(ApiResponse.ok(info)));
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        boolean runtimeOk = RuntimeContext.isInitializedStatic();
        var health = Map.of(
                "status", runtimeOk ? "UP" : "DOWN",
                "runtime", runtimeOk ? "healthy" : "not initialized"
        );
        sendOk(exchange, toJson(ApiResponse.ok(health)));
    }

    private void handleWorkers(HttpExchange exchange) throws IOException {
        var workers = java.util.Arrays.stream(WorkerType.values())
                .map(wt -> Map.of(
                        "type", wt.name(),
                        "code", wt.code()
                ))
                .toList();
        sendOk(exchange, toJson(ApiResponse.ok(workers)));
    }
}
