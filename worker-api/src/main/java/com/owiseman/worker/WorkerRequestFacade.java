package com.owiseman.worker;

import com.owiseman.worker.protocol.WorkerRequest;
import com.owiseman.worker.protocol.WorkerType;

public final class WorkerRequestFacade {
    private final WorkerRequest delegate;

    public WorkerRequestFacade(String taskId, String workerType, byte[] payload) {
        this.delegate = WorkerRequest.builder()
                .taskId(taskId)
                .workerType(WorkerType.fromCode(workerType))
                .payload(payload)
                .build();
    }

    public WorkerRequest delegate() { return delegate; }
}
