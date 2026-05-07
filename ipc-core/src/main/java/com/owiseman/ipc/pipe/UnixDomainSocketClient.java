package com.owiseman.ipc.pipe;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class UnixDomainSocketClient {

    private static final Logger LOG = Logger.getLogger(UnixDomainSocketClient.class.getName());

    private final Path socketPath;
    private SocketChannel channel;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public UnixDomainSocketClient(Path socketPath) {
        this.socketPath = socketPath;
    }

    public void connect() throws IOException {
        if (connected.getAndSet(true)) {
            throw new IllegalStateException("Already connected");
        }

        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);
        channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        channel.connect(address);
        LOG.info("Connected to Unix Domain Socket: " + socketPath);
    }

    public void sendMessage(byte[] data) throws IOException {
        if (!connected.get() || channel == null) {
            throw new IllegalStateException("Not connected");
        }

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public byte[] receiveMessage() throws IOException {
        if (!connected.get() || channel == null) {
            throw new IllegalStateException("Not connected");
        }

        ByteBuffer lengthBuf = ByteBuffer.allocate(Integer.BYTES);
        int read = channel.read(lengthBuf);
        if (read < Integer.BYTES) return new byte[0];

        lengthBuf.flip();
        int msgLen = lengthBuf.getInt();
        if (msgLen <= 0) return new byte[0];

        ByteBuffer msgBuf = ByteBuffer.allocate(msgLen);
        while (msgBuf.hasRemaining()) {
            int r = channel.read(msgBuf);
            if (r < 0) break;
        }
        msgBuf.flip();

        byte[] data = new byte[msgBuf.remaining()];
        msgBuf.get(data);
        return data;
    }

    public void disconnect() {
        connected.set(false);
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            LOG.warning("Error disconnecting: " + e.getMessage());
        }
        LOG.info("Disconnected from Unix Domain Socket");
    }

    public boolean isConnected() {
        return connected.get();
    }
}
