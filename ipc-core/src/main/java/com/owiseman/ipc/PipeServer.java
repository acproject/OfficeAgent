package com.owiseman.ipc;

import com.owiseman.ipc.pipe.UnixDomainSocketServer;
import com.owiseman.ipc.pipe.UnixDomainSocketClient;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class PipeServer {

    private final UnixDomainSocketServer udsServer;

    public PipeServer(Path socketPath) {
        this.udsServer = new UnixDomainSocketServer(socketPath);
    }

    public void start(Consumer<byte[]> messageHandler) throws Exception {
        udsServer.start(messageHandler);
    }

    public void stop() {
        udsServer.stop();
    }

    public boolean isRunning() {
        return udsServer.isRunning();
    }
}
