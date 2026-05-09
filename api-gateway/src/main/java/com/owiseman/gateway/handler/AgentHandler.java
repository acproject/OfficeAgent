package com.owiseman.gateway.handler;

import com.owiseman.agent.Agent;
import com.owiseman.gateway.protocol.AgentTaskRequest;
import com.owiseman.gateway.protocol.AgentTaskResponse;
import com.owiseman.gateway.protocol.ApiResponse;
import com.owiseman.runtime.RuntimeContext;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AgentHandler implements RequestHandler {

    private final Map<String, Agent> activeAgents = new ConcurrentHashMap<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleCors(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("POST".equalsIgnoreCase(method) && path.endsWith("/agent/execute")) {
            handleExecute(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/agent/status")) {
            handleStatus(exchange);
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/agent/patch")) {
            handlePatch(exchange);
        } else {
            sendError(exchange, 404, "Not found: " + path);
        }
    }

    private void handleExecute(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        AgentTaskRequest request = parseRequest(body);

        if (request == null || request.goal() == null || request.goal().isBlank()) {
            sendError(exchange, 400, "Missing required field: goal");
            return;
        }

        Agent agent = new Agent();
        activeAgents.put(agent.agentId(), agent);

        if (request.isStreamMode()) {
            handleStreamExecute(exchange, agent, request);
        } else {
            handleSyncExecute(exchange, agent, request);
        }
    }

    private void handleSyncExecute(HttpExchange exchange, Agent agent, AgentTaskRequest request) throws IOException {
        try {
            com.owiseman.agent.Agent.AgentResult result = agent.execute(request.goal()).join();
            AgentTaskResponse response = AgentTaskResponse.from(result);
            sendOk(exchange, toJson(ApiResponse.ok(response)));
        } catch (Exception e) {
            sendError(exchange, 500, "Agent execution failed: " + e.getMessage());
        } finally {
            activeAgents.remove(agent.agentId());
        }
    }

    private void handleStreamExecute(HttpExchange exchange, Agent agent, AgentTaskRequest request) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, 0);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(("event: start\ndata: {\"agentId\":\"" + agent.agentId() + "\"}\n\n").getBytes());
            os.flush();

            agent.execute(request.goal()).thenAccept(result -> {
                try {
                    String json = toJson(AgentTaskResponse.from(result));
                    os.write(("event: complete\ndata: " + json + "\n\n").getBytes());
                    os.flush();
                } catch (IOException ignored) {}
            }).exceptionally(ex -> {
                try {
                    os.write(("event: error\ndata: {\"error\":\"" + ex.getMessage() + "\"}\n\n").getBytes());
                    os.flush();
                } catch (IOException ignored) {}
                return null;
            });
        } finally {
            activeAgents.remove(agent.agentId());
        }
    }

    private void handleStatus(HttpExchange exchange) throws IOException {
        var status = Map.of(
                "activeAgents", activeAgents.size(),
                "runtimeInitialized", RuntimeContext.isInitializedStatic(),
                "agentIds", activeAgents.keySet()
        );
        sendOk(exchange, toJson(ApiResponse.ok(status)));
    }

    private void handlePatch(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        if (body == null || body.isBlank()) {
            sendError(exchange, 400, "Empty request body");
            return;
        }

        String documentId = extractField(body, "documentId");
        String instruction = extractField(body, "instruction");

        if (documentId == null || instruction == null) {
            sendError(exchange, 400, "Missing required fields: documentId, instruction");
            return;
        }

        Agent agent = new Agent();
        var patchSet = agent.generatePatch(documentId, instruction);
        sendOk(exchange, toJson(ApiResponse.ok(Map.of(
                "documentId", documentId,
                "patchCount", patchSet.operations().size(),
                "operations", patchSet.operations().stream().map(op -> Map.of(
                        "type", op.operationType(),
                        "targetId", op.targetId()
                )).toList()
        ))));
    }

    private AgentTaskRequest parseRequest(String body) {
        if (body == null || body.isBlank()) return null;
        return new AgentTaskRequest(
                extractField(body, "goal"),
                extractField(body, "documentPath"),
                extractField(body, "documentType"),
                extractField(body, "instruction"),
                Boolean.parseBoolean(extractField(body, "stream"))
        );
    }

    private String extractField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + key.length());
        if (colon < 0) return null;

        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;

        if (start >= json.length()) return null;

        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return end > start ? json.substring(start + 1, end) : null;
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }
}
