package com.owiseman.document.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Block {

    private final String blockId;
    private final BlockType type;
    private final String content;
    private final Rect bbox;
    private final SemanticRole semanticRole;
    private final TextStyle style;
    private final Map<String, Object> metadata;

    private Block(Builder builder) {
        this.blockId = Objects.requireNonNull(builder.blockId);
        this.type = Objects.requireNonNull(builder.type);
        this.content = builder.content;
        this.bbox = builder.bbox;
        this.semanticRole = builder.semanticRole;
        this.style = builder.style;
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
    }

    public String blockId() { return blockId; }
    public BlockType type() { return type; }
    public String content() { return content; }
    public Rect bbox() { return bbox; }
    public SemanticRole semanticRole() { return semanticRole; }
    public TextStyle style() { return style; }
    public Map<String, Object> metadata() { return metadata; }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.blockId = blockId;
        b.type = type;
        b.content = content;
        b.bbox = bbox;
        b.semanticRole = semanticRole;
        b.style = style;
        b.metadata.putAll(metadata);
        return b;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String blockId;
        private BlockType type;
        private String content = "";
        private Rect bbox;
        private SemanticRole semanticRole;
        private TextStyle style;
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        public Builder blockId(String blockId) { this.blockId = blockId; return this; }
        public Builder type(BlockType type) { this.type = type; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder bbox(Rect bbox) { this.bbox = bbox; return this; }
        public Builder semanticRole(SemanticRole semanticRole) { this.semanticRole = semanticRole; return this; }
        public Builder style(TextStyle style) { this.style = style; return this; }
        public Builder metadata(String key, Object value) { this.metadata.put(key, value); return this; }
        public Block build() { return new Block(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block that)) return false;
        return Objects.equals(blockId, that.blockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockId);
    }

    @Override
    public String toString() {
        return "Block{id=" + blockId + ", type=" + type + ", role=" + semanticRole + "}";
    }
}
