package com.owiseman.document.patch;

public record ReplaceImagePatch(String targetId, byte[] imageData, String format) implements PatchOperation {

    @Override
    public String operationType() {
        return "replace_image";
    }
}
