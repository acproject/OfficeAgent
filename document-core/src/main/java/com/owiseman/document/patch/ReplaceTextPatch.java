package com.owiseman.document.patch;

public record ReplaceTextPatch(String targetId, String newText) implements PatchOperation {

    @Override
    public String operationType() {
        return "replace_text";
    }
}
