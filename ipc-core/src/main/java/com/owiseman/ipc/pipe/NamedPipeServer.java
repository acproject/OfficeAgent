package com.owiseman.ipc.pipe;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class NamedPipeServer {

    private static final Logger LOG = Logger.getLogger(NamedPipeServer.class.getName());

    private final Path pipePath;
    private final int bufferSize;
    private RandomAccessFile raf;
    private FileChannel fileChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public NamedPipeServer(Path pipePath) {
        this(pipePath, 65536);
    }

    public NamedPipeServer(Path pipePath, int bufferSize) {
        this.pipePath = pipePath;
        this.bufferSize = bufferSize;
    }

    public void start(Consumer<byte[]> messageHandler) throws IOException {
        if (running.getAndSet(true)) {
            throw new IllegalStateException("Server already running");
        }

        Thread.ofVirtual().name("named-pipe-reader").start(() -> {
            try {
                readLoop(messageHandler);
            } catch (IOException e) {
                if (running.get()) {
                    LOG.warning("Named pipe read error: " + e.getMessage());
                }
            }
        });
        LOG.info("Named Pipe server started on: " + pipePath);
    }

    private void readLoop(Consumer<byte[]> messageHandler) throws IOException {
        while (running.get()) {
            try {
                raf = new RandomAccessFile(pipePath.toFile(), "rw");
                fileChannel = raf.getChannel();
                break;
            } catch (IOException e) {
                try { Thread.sleep(100); } catch (InterruptedException ie) { return; }
            }
        }

        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize);

        while (running.get()) {
            int msgLen = buffer.getInt();
            if (msgLen <= 0 || msgLen > bufferSize) {
                buffer.rewind();
                try { Thread.sleep(10); } catch (InterruptedException e) { return; }
                continue;
            }

            byte[] data = new byte[msgLen];
            buffer.get(data);
            messageHandler.accept(data);
            buffer.rewind();
        }
    }

    public void stop() {
        running.set(false);
        try {
            if (fileChannel != null) fileChannel.close();
            if (raf != null) raf.close();
        } catch (IOException e) {
            LOG.warning("Error closing named pipe: " + e.getMessage());
        }
        LOG.info("Named Pipe server stopped");
    }

    public boolean isRunning() {
        return running.get();
    }
}
