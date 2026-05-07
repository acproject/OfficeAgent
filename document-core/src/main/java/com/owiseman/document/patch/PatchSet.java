package com.owiseman.document.patch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PatchSet {

    private final String patchSetId;
    private final String documentId;
    private final List<PatchOperation> operations;
    private final long timestamp;

    private PatchSet(Builder builder) {
        this.patchSetId = Objects.requireNonNull(builder.patchSetId);
        this.documentId = Objects.requireNonNull(builder.documentId);
        this.operations = Collections.unmodifiableList(new ArrayList<>(builder.operations));
        this.timestamp = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
    }

    public String patchSetId() { return patchSetId; }
    public String documentId() { return documentId; }
    public List<PatchOperation> operations() { return operations; }
    public long timestamp() { return timestamp; }

    public PatchSet merge(PatchSet other) {
        if (!this.documentId.equals(other.documentId)) {
            throw new IllegalArgumentException("Cannot merge patch sets for different documents");
        }
        Builder builder = new Builder()
                .patchSetId(this.patchSetId + "+" + other.patchSetId)
                .documentId(this.documentId);
        builder.operations.addAll(this.operations);
        builder.operations.addAll(other.operations);
        return builder.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String patchSetId;
        private String documentId;
        private final List<PatchOperation> operations = new ArrayList<>();
        private long timestamp;

        public Builder patchSetId(String patchSetId) { this.patchSetId = patchSetId; return this; }
        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder addOperation(PatchOperation op) { this.operations.add(op); return this; }
        public Builder operations(List<PatchOperation> ops) { this.operations.clear(); this.operations.addAll(ops); return this; }
        public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }
        public PatchSet build() { return new PatchSet(this); }
    }
}
