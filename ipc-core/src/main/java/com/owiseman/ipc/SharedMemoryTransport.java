package com.owiseman.ipc;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class SharedMemoryTransport {

    private static final Logger LOG = Logger.getLogger(SharedMemoryTransport.class.getName());

    private final Path shmPath;
    private final int size;
    private final AtomicBoolean open = new AtomicBoolean(false);

    public SharedMemoryTransport(Path shmPath, int size) {
        this.shmPath = shmPath;
        this.size = size;
    }

    public void open() throws IOException {
        open.set(true);
        LOG.info("SharedMemory transport opened: " + shmPath);
    }

    public void close() {
        open.set(false);
        LOG.info("SharedMemory transport closed");
    }

    public boolean isOpen() {
        return open.get();
    }
}
