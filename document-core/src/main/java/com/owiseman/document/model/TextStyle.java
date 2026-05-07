package com.owiseman.document.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class TextStyle {

    private final String fontFamily;
    private final float fontSize;
    private final boolean bold;
    private final boolean italic;
    private final boolean underline;
    private final String color;
    private final Map<String, String> extra;

    private TextStyle(Builder builder) {
        this.fontFamily = builder.fontFamily;
        this.fontSize = builder.fontSize;
        this.bold = builder.bold;
        this.italic = builder.italic;
        this.underline = builder.underline;
        this.color = builder.color;
        this.extra = Collections.unmodifiableMap(new LinkedHashMap<>(builder.extra));
    }

    public String fontFamily() { return fontFamily; }
    public float fontSize() { return fontSize; }
    public boolean bold() { return bold; }
    public boolean italic() { return italic; }
    public boolean underline() { return underline; }
    public String color() { return color; }
    public Map<String, String> extra() { return extra; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String fontFamily = "Arial";
        private float fontSize = 12f;
        private boolean bold;
        private boolean italic;
        private boolean underline;
        private String color = "#000000";
        private final Map<String, String> extra = new LinkedHashMap<>();

        public Builder fontFamily(String fontFamily) { this.fontFamily = fontFamily; return this; }
        public Builder fontSize(float fontSize) { this.fontSize = fontSize; return this; }
        public Builder bold(boolean bold) { this.bold = bold; return this; }
        public Builder italic(boolean italic) { this.italic = italic; return this; }
        public Builder underline(boolean underline) { this.underline = underline; return this; }
        public Builder color(String color) { this.color = color; return this; }
        public Builder extra(String key, String value) { this.extra.put(key, value); return this; }
        public TextStyle build() { return new TextStyle(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextStyle that)) return false;
        return Float.compare(fontSize, that.fontSize) == 0
                && bold == that.bold && italic == that.italic
                && underline == that.underline
                && Objects.equals(fontFamily, that.fontFamily)
                && Objects.equals(color, that.color)
                && Objects.equals(extra, that.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontFamily, fontSize, bold, italic, underline, color, extra);
    }
}
