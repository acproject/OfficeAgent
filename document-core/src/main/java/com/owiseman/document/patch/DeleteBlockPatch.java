package com.owiseman.document.patch;

public record DeleteBlockPatch(String targetId, int pageIndex) implements PatchOperation {

    @Override
    public String operationType() {
        return "delete_block";
    }
}
