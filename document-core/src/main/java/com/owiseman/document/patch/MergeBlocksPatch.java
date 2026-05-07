package com.owiseman.document.patch;

import java.util.List;

public record MergeBlocksPatch(String targetId, List<String> blockIds, String mergedContent)
        implements PatchOperation {

    @Override
    public String operationType() {
        return "merge_blocks";
    }
}
