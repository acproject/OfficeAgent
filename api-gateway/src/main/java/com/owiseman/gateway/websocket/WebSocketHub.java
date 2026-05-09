package com.owiseman.gateway.websocket;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class WebSocketHub {

    private static final Logger LOG = Logger.getLogger(WebSocketHub.class.getName());
    private static final String WS_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private final CopyOnWriteArrayList<WebSocketConnection> connections = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, Consumer<String>> topicSubscribers = new ConcurrentHashMap<>();

    public void onConnect(WebSocketConnection connection) {
        connections.add(connection);
        LOG.info("WebSocket connected: " + connection.id() + ", total: " + connections.size());
    }

    public void onDisconnect(WebSocketConnection connection) {
        connections.remove(connection);
        LOG.info("WebSocket disconnected: " + connection.id() + ", total: " + connections.size());
    }

    public void broadcast(String topic, String message) {
        String payload = "{\"topic\":\"" + topic + "\",\"data\":" + message + "}";
        for (WebSocketConnection conn : connections) {
            try {
                conn.send(payload);
            } catch (Exception e) {
                LOG.fine("Failed to send to " + conn.id() + ": " + e.getMessage());
                connections.remove(conn);
            }
        }
    }

    public void sendTo(String connectionId, String topic, String message) {
        for (WebSocketConnection conn : connections) {
            if (conn.id().equals(connectionId)) {
                try {
                    conn.send("{\"topic\":\"" + topic + "\",\"data\":" + message + "}");
                } catch (Exception e) {
                    LOG.fine("Failed to send to " + connectionId + ": " + e.getMessage());
                }
                return;
            }
        }
    }

    public int connectionCount() {
        return connections.size();
    }

    public static String computeAcceptKey(String clientKey) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest((clientKey + WS_MAGIC).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }
}
