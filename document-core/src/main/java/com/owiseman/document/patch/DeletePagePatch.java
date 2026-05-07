package com.owiseman.document.patch;

public record DeletePagePatch(String targetId, int pageIndex) implements PatchOperation {

    @Override
    public String operationType() {
        return "delete_page";
    }
}
