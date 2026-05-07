package com.owiseman.document.patch;

import com.owiseman.document.model.TextStyle;

public record UpdateStylePatch(String targetId, TextStyle newStyle) implements PatchOperation {

    @Override
    public String operationType() {
        return "update_style";
    }
}
