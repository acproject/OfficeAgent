package com.owiseman.document.patch;

import com.owiseman.document.model.Block;

public record InsertBlockPatch(String targetId, int pageIndex, Block block, int position)
        implements PatchOperation {

    @Override
    public String operationType() {
        return "insert_block";
    }
}
