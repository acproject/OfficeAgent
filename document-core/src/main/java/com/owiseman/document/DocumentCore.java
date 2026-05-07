package com.owiseman.document;

import com.owiseman.document.ir.DocumentIrEngine;
import com.owiseman.document.model.Document;
import com.owiseman.document.patch.PatchSet;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DocumentCore {

    private final Map<String, Document> documentCache = new ConcurrentHashMap<>();
    private final DocumentIrEngine irEngine = new DocumentIrEngine();

    public Document importDocument(DocumentImporter importer, Path filePath) {
        String docId = importer.detectDocumentId(filePath);
        Document doc = importer.importDocument(filePath);
        documentCache.put(docId, doc);
        return doc;
    }

    public Document applyPatches(String documentId, PatchSet patchSet) {
        Document doc = documentCache.get(documentId);
        if (doc == null) {
            throw new IllegalArgumentException("Document not found: " + documentId);
        }
        Document updated = irEngine.apply(doc, patchSet);
        documentCache.put(documentId, updated);
        return updated;
    }

    public void renderDocument(String documentId, DocumentRenderer renderer, Path outputPath) {
        Document doc = documentCache.get(documentId);
        if (doc == null) {
            throw new IllegalArgumentException("Document not found: " + documentId);
        }
        renderer.render(doc, outputPath);
    }

    public Document getDocument(String documentId) {
        return documentCache.get(documentId);
    }

    public void removeDocument(String documentId) {
        documentCache.remove(documentId);
    }
}
