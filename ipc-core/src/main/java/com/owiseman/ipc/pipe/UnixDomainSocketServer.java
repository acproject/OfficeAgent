package com.owiseman.ipc.pipe;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class UnixDomainSocketServer {

    private static final Logger LOG = Logger.getLogger(UnixDomainSocketServer.class.getName());

    private final Path socketPath;
    private final int bufferSize;
    private ServerSocketChannel serverChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public UnixDomainSocketServer(Path socketPath) {
        this(socketPath, 65536);
    }

    public UnixDomainSocketServer(Path socketPath, int bufferSize) {
        this.socketPath = socketPath;
        this.bufferSize = bufferSize;
    }

    public void start(Consumer<byte[]> messageHandler) throws IOException {
        if (running.getAndSet(true)) {
            throw new IllegalStateException("Server already running");
        }

        Files.deleteIfExists(socketPath);

        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);
        serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverChannel.bind(address);

        LOG.info("Unix Domain Socket server listening on: " + socketPath);

        Thread.ofVirtual().name("uds-acceptor").start(() -> acceptLoop(messageHandler));
    }

    private void acceptLoop(Consumer<byte[]> messageHandler) {
        while (running.get()) {
            try {
                SocketChannel client = serverChannel.accept();
                Thread.ofVirtual().name("uds-client").start(() -> handleClient(client, messageHandler));
            } catch (IOException e) {
                if (running.get()) {
                    LOG.warning("Accept error: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(SocketChannel channel, Consumer<byte[]> messageHandler) {
        try (channel) {
            ByteBuffer lengthBuf = ByteBuffer.allocate(Integer.BYTES);
            while (running.get() && channel.isOpen()) {
                lengthBuf.clear();
                int read = channel.read(lengthBuf);
                if (read < Integer.BYTES) break;

                lengthBuf.flip();
                int msgLen = lengthBuf.getInt();
                if (msgLen <= 0 || msgLen > bufferSize) {
                    LOG.warning("Invalid message length: " + msgLen);
                    break;
                }

                ByteBuffer msgBuf = ByteBuffer.allocate(msgLen);
                while (msgBuf.hasRemaining()) {
                    int r = channel.read(msgBuf);
                    if (r < 0) break;
                }
                msgBuf.flip();

                byte[] data = new byte[msgBuf.remaining()];
                msgBuf.get(data);
                messageHandler.accept(data);
            }
        } catch (IOException e) {
            if (running.get()) {
                LOG.fine("Client disconnected: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running.set(false);
        try {
            if (serverChannel != null) {
                serverChannel.close();
            }
            Files.deleteIfExists(socketPath);
        } catch (IOException e) {
            LOG.warning("Error closing server: " + e.getMessage());
        }
        LOG.info("Unix Domain Socket server stopped");
    }

    public boolean isRunning() {
        return running.get();
    }

    public Path socketPath() {
        return socketPath;
    }
}
