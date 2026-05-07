package com.owiseman.document.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Document {

    private final String documentId;
    private final DocumentType documentType;
    private final String sourcePath;
    private final List<Page> pages;
    private final Map<String, Object> properties;

    private Document(Builder builder) {
        this.documentId = Objects.requireNonNull(builder.documentId);
        this.documentType = Objects.requireNonNull(builder.documentType);
        this.sourcePath = builder.sourcePath;
        this.pages = Collections.unmodifiableList(new ArrayList<>(builder.pages));
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(builder.properties));
    }

    public String documentId() { return documentId; }
    public DocumentType documentType() { return documentType; }
    public String sourcePath() { return sourcePath; }
    public List<Page> pages() { return pages; }
    public Map<String, Object> properties() { return properties; }

    public Page page(int index) {
        return pages.get(index);
    }

    public int pageCount() {
        return pages.size();
    }

    public Block findBlock(String blockId) {
        for (Page page : pages) {
            Block b = page.findBlock(blockId);
            if (b != null) return b;
        }
        return null;
    }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.documentId = documentId;
        b.documentType = documentType;
        b.sourcePath = sourcePath;
        b.pages.addAll(pages);
        b.properties.putAll(properties);
        return b;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String documentId;
        private DocumentType documentType;
        private String sourcePath;
        private final List<Page> pages = new ArrayList<>();
        private final Map<String, Object> properties = new LinkedHashMap<>();

        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder documentType(DocumentType documentType) { this.documentType = documentType; return this; }
        public Builder sourcePath(String sourcePath) { this.sourcePath = sourcePath; return this; }
        public Builder addPage(Page page) { this.pages.add(page); return this; }
        public Builder pages(List<Page> pages) { this.pages.clear(); this.pages.addAll(pages); return this; }
        public Builder property(String key, Object value) { this.properties.put(key, value); return this; }
        public Document build() { return new Document(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document that)) return false;
        return Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }

    @Override
    public String toString() {
        return "Document{id=" + documentId + ", type=" + documentType + ", pages=" + pages.size() + "}";
    }
}
