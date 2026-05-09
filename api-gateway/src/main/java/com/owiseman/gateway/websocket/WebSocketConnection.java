package com.owiseman.gateway.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Logger;

public final class WebSocketConnection {

    private static final Logger LOG = Logger.getLogger(WebSocketConnection.class.getName());

    private final String id;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private volatile boolean open = true;

    public WebSocketConnection(Socket socket) throws IOException {
        this.id = "ws-" + UUID.randomUUID().toString().substring(0, 8);
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    public String id() {
        return id;
    }

    public void send(String message) throws IOException {
        if (!open) return;
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        synchronized (out) {
            out.write(0x81);
            if (payload.length < 126) {
                out.write(payload.length);
            } else if (payload.length < 65536) {
                out.write(126);
                out.write((payload.length >> 8) & 0xFF);
                out.write(payload.length & 0xFF);
            } else {
                out.write(127);
                for (int i = 56; i >= 0; i -= 8) {
                    out.write((int) ((payload.length >> i) & 0xFF));
                }
            }
            out.write(payload);
            out.flush();
        }
    }

    public String receive() throws IOException {
        int firstByte = in.read();
        if (firstByte == -1) {
            open = false;
            return null;
        }

        int opcode = firstByte & 0x0F;
        if (opcode == 0x8) {
            close();
            return null;
        }

        int secondByte = in.read();
        boolean masked = (secondByte & 0x80) != 0;
        long payloadLength = secondByte & 0x7F;

        if (payloadLength == 126) {
            payloadLength = ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
        } else if (payloadLength == 127) {
            payloadLength = 0;
            for (int i = 0; i < 8; i++) {
                payloadLength = (payloadLength << 8) | (in.read() & 0xFF);
            }
        }

        byte[] maskKey = null;
        if (masked) {
            maskKey = in.readNBytes(4);
        }

        byte[] payload = in.readNBytes((int) payloadLength);
        if (masked && maskKey != null) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] ^= maskKey[i % 4];
            }
        }

        return new String(payload, StandardCharsets.UTF_8);
    }

    public void close() {
        open = false;
        try {
            synchronized (out) {
                out.write(new byte[]{(byte) 0x88, 0x00});
                out.flush();
            }
            socket.close();
        } catch (IOException e) {
            LOG.fine("Error closing WebSocket: " + e.getMessage());
        }
    }

    public boolean isOpen() {
        return open && !socket.isClosed();
    }
}
