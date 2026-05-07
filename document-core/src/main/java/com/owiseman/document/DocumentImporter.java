package com.owiseman.document;

import com.owiseman.document.model.Document;
import com.owiseman.document.model.DocumentType;
import com.owiseman.document.patch.PatchSet;

import java.nio.file.Path;

public interface DocumentImporter {

    DocumentType supportedType();

    Document importDocument(Path filePath);

    String detectDocumentId(Path filePath);
}
