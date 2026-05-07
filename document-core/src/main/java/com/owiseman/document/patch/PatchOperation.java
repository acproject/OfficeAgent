package com.owiseman.document.patch;

public sealed interface PatchOperation
        permits ReplaceTextPatch,
        InsertBlockPatch,
        DeleteBlockPatch,
        MoveBlockPatch,
        UpdateStylePatch,
        InsertPagePatch,
        DeletePagePatch,
        UpdateMetadataPatch,
        ReplaceImagePatch,
        MergeBlocksPatch {

    String targetId();

    String operationType();
}
