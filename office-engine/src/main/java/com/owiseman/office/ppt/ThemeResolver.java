package com.owiseman.office.ppt;

import com.owiseman.document.model.TextStyle;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ThemeResolver {

    private final Map<String, TextStyle> themeStyles = new LinkedHashMap<>();

    public ThemeResolver() {
        themeStyles.put("title", TextStyle.builder()
                .fontFamily("Microsoft YaHei").fontSize(36f).bold(true).build());
        themeStyles.put("subtitle", TextStyle.builder()
                .fontFamily("Microsoft YaHei").fontSize(24f).build());
        themeStyles.put("body", TextStyle.builder()
                .fontFamily("Microsoft YaHei").fontSize(18f).build());
        themeStyles.put("caption", TextStyle.builder()
                .fontFamily("Microsoft YaHei").fontSize(14f).italic(true).build());
    }

    public TextStyle getStyle(String role) {
        return themeStyles.getOrDefault(role, themeStyles.get("body"));
    }

    public void registerStyle(String role, TextStyle style) {
        themeStyles.put(role, style);
    }
}
