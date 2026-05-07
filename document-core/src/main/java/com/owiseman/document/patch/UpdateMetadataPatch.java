package com.owiseman.document.patch;

import java.util.Map;

public record UpdateMetadataPatch(String targetId, Map<String, Object> metadata) implements PatchOperation {

    @Override
    public String operationType() {
        return "update_metadata";
    }
}
