package com.owiseman.ipc;

import com.owiseman.ipc.pipe.UnixDomainSocketClient;

import java.nio.file.Path;

public final class PipeClient {

    private final UnixDomainSocketClient udsClient;

    public PipeClient(Path socketPath) {
        this.udsClient = new UnixDomainSocketClient(socketPath);
    }

    public void connect() throws Exception {
        udsClient.connect();
    }

    public void sendMessage(byte[] data) throws Exception {
        udsClient.sendMessage(data);
    }

    public byte[] receiveMessage() throws Exception {
        return udsClient.receiveMessage();
    }

    public void disconnect() {
        udsClient.disconnect();
    }

    public boolean isConnected() {
        return udsClient.isConnected();
    }
}
