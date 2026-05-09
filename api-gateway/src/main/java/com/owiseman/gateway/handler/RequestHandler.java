package com.owiseman.gateway.handler;

import com.owiseman.gateway.protocol.ApiResponse;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public interface RequestHandler {

    void handle(HttpExchange exchange) throws IOException;

    default void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    default void sendOk(HttpExchange exchange, String json) throws IOException {
        sendJson(exchange, 200, json);
    }

    default void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = toJson(ApiResponse.error(statusCode, message));
        sendJson(exchange, statusCode, json);
    }

    default String readBody(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    default void handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(204, -1);
    }

    default String toJson(Object obj) {
        return simpleToJson(obj);
    }

    private static String simpleToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof ApiResponse<?> resp) {
            String dataJson = resp.data() != null ? valueToJson(resp.data()) : "null";
            return "{\"code\":" + resp.code() +
                    ",\"message\":\"" + escapeJson(resp.message()) + "\"" +
                    ",\"data\":" + dataJson + "}";
        }
        return valueToJson(obj);
    }

    private static String valueToJson(Object val) {
        if (val == null) return "null";
        if (val instanceof String s) return "\"" + escapeJson(s) + "\"";
        if (val instanceof Number || val instanceof Boolean) return val.toString();
        if (val instanceof Enum<?> e) return "\"" + e.name() + "\"";
        if (val instanceof Iterable<?> iter) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : iter) {
                if (!first) sb.append(",");
                sb.append(valueToJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (val.getClass().isRecord()) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (java.lang.reflect.RecordComponent rc : val.getClass().getRecordComponents()) {
                try {
                    Object v = rc.getAccessor().invoke(val);
                    if (!first) sb.append(",");
                    sb.append("\"").append(rc.getName()).append("\":").append(valueToJson(v));
                    first = false;
                } catch (Exception ignored) {}
            }
            sb.append("}");
            return sb.toString();
        }
        return "\"" + escapeJson(val.toString()) + "\"";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
