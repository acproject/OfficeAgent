package com.owiseman.worker.protocol;

import com.owiseman.ipc.codec.MessageCodec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public final class WorkerConnection {

    private static final Logger LOG = Logger.getLogger(WorkerConnection.class.getName());

    private final WorkerType workerType;
    private final Path socketPath;
    private final com.owiseman.ipc.pipe.UnixDomainSocketClient client;
    private final MessageCodec codec;
    private final ConcurrentHashMap<String, CompletableFuture<WorkerResponse>> pendingRequests;
    private final AtomicLong requestCounter = new AtomicLong(0);
    private volatile boolean connected;

    public WorkerConnection(WorkerType workerType, Path socketPath) {
        this.workerType = workerType;
        this.socketPath = socketPath;
        this.client = new com.owiseman.ipc.pipe.UnixDomainSocketClient(socketPath);
        this.codec = new MessageCodec();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.connected = false;
    }

    public void connect() throws IOException {
        try {
            client.connect();
            connected = true;
            Thread.ofVirtual().name("worker-recv-" + workerType.code()).start(this::receiveLoop);
            LOG.info("Connected to " + workerType + " worker at " + socketPath);
        } catch (Exception e) {
            throw new IOException("Failed to connect to worker: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<WorkerResponse> sendRequest(WorkerRequest request) {
        if (!connected) {
            return CompletableFuture.failedFuture(new IllegalStateException("Not connected to worker"));
        }

        CompletableFuture<WorkerResponse> future = new CompletableFuture<>();
        pendingRequests.put(request.taskId(), future);

        try {
            byte[] encoded = codec.encode(MessageCodec.MessageType.WORKER_REQUEST, toProtobuf(request));
            client.sendMessage(encoded);

            int timeout = request.timeoutMs();
            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(timeout);
                    future.complete(WorkerResponse.builder()
                            .taskId(request.taskId())
                            .status(WorkerResponse.Status.TIMEOUT)
                            .errorMessage("Request timed out after " + timeout + "ms")
                            .build());
                    pendingRequests.remove(request.taskId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (IOException e) {
            future.complete(WorkerResponse.builder()
                    .taskId(request.taskId())
                    .status(WorkerResponse.Status.FAILED)
                    .errorMessage("Send failed: " + e.getMessage())
                    .build());
            pendingRequests.remove(request.taskId());
        }

        return future;
    }

    private void receiveLoop() {
        while (connected) {
            try {
                byte[] data = client.receiveMessage();
                if (data.length > 0) {
                    MessageCodec.DecodedMessage decoded = codec.decode(data);
                    if (decoded.type() == MessageCodec.MessageType.WORKER_RESPONSE) {
                        WorkerResponse response = fromProtobuf(decoded);
                        CompletableFuture<WorkerResponse> future = pendingRequests.remove(response.taskId());
                        if (future != null) {
                            future.complete(response);
                        }
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    LOG.warning("Receive error from " + workerType + ": " + e.getMessage());
                }
            }
        }
    }

    public void disconnect() {
        connected = false;
        client.disconnect();
        pendingRequests.clear();
        LOG.info("Disconnected from " + workerType + " worker");
    }

    public WorkerType workerType() { return workerType; }
    public boolean isConnected() { return connected; }

    private byte[] toProtobuf(WorkerRequest request) {
        return request.payload();
    }

    private WorkerResponse fromProtobuf(MessageCodec.DecodedMessage decoded) {
        return WorkerResponse.builder()
                .taskId("resp")
                .status(WorkerResponse.Status.SUCCESS)
                .payload(decoded.payload())
                .build();
    }
}
