package com.owiseman.document.patch;

public record MoveBlockPatch(String targetId, int fromPageIndex, int toPageIndex, int position)
        implements PatchOperation {

    @Override
    public String operationType() {
        return "move_block";
    }
}
