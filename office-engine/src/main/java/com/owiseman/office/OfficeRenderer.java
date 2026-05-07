package com.owiseman.office;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.BlockType;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.DocumentType;
import com.owiseman.document.model.Page;
import com.owiseman.document.model.Rect;
import com.owiseman.document.model.SemanticRole;
import com.owiseman.document.model.TextStyle;
import com.owiseman.document.patch.PatchSet;

import java.io.IOException;
import java.nio.file.Path;

public interface OfficeRenderer {

    void applyPatch(Document document, PatchSet patchSet);

    void export(Document document, Path outputPath) throws IOException;

    String formatName();
}
