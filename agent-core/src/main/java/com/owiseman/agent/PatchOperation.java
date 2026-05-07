package com.owiseman.agent;

public sealed interface PatchOperation
        permits ReplaceTextPatch,InsertSlidePatch,
        DeleteBlockPatch {

}

public final class ReplaceTextPatch implements PatchOperation {
    public String targetId;
    public String newText;
}
