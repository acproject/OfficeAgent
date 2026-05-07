package com.owiseman.document.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Page {

    private final int pageIndex;
    private final float width;
    private final float height;
    private final List<Block> blocks;

    private Page(Builder builder) {
        this.pageIndex = builder.pageIndex;
        this.width = builder.width;
        this.height = builder.height;
        this.blocks = Collections.unmodifiableList(new ArrayList<>(builder.blocks));
    }

    public int pageIndex() { return pageIndex; }
    public float width() { return width; }
    public float height() { return height; }
    public List<Block> blocks() { return blocks; }

    public Block findBlock(String blockId) {
        return blocks.stream()
                .filter(b -> b.blockId().equals(blockId))
                .findFirst()
                .orElse(null);
    }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.pageIndex = pageIndex;
        b.width = width;
        b.height = height;
        b.blocks.addAll(blocks);
        return b;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int pageIndex;
        private float width = 792f;
        private float height = 612f;
        private final List<Block> blocks = new ArrayList<>();

        public Builder pageIndex(int pageIndex) { this.pageIndex = pageIndex; return this; }
        public Builder width(float width) { this.width = width; return this; }
        public Builder height(float height) { this.height = height; return this; }
        public Builder addBlock(Block block) { this.blocks.add(block); return this; }
        public Builder blocks(List<Block> blocks) { this.blocks.clear(); this.blocks.addAll(blocks); return this; }
        public Page build() { return new Page(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page that)) return false;
        return pageIndex == that.pageIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex);
    }
}
