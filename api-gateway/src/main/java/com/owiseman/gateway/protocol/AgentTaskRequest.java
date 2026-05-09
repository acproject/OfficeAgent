package com.owiseman.gateway.protocol;

public record AgentTaskRequest(
        String goal,
        String documentPath,
        String documentType,
        String instruction,
        boolean stream
) {
    public boolean hasDocument() {
        return documentPath != null && !documentPath.isBlank();
    }

    public boolean isStreamMode() {
        return stream;
    }
}
