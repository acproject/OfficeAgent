package com.owiseman.document.patch;

import com.owiseman.document.model.Page;

public record InsertPagePatch(String targetId, int afterPageIndex, Page page) implements PatchOperation {

    @Override
    public String operationType() {
        return "insert_page";
    }
}
