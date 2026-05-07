package com.owiseman.document;

import com.owiseman.document.model.Document;
import com.owiseman.document.patch.PatchSet;

import java.nio.file.Path;

public interface DocumentRenderer {

    void render(Document document, Path outputPath);

    void applyAndRender(Document document, PatchSet patchSet, Path outputPath);

    String formatName();
}
