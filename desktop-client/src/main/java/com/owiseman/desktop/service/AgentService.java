package com.owiseman.desktop.service;

import com.owiseman.agent.Agent;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.DocumentType;
import com.owiseman.document.patch.PatchSet;
import com.owiseman.office.excel.ExcelImporter;
import com.owiseman.office.ppt.PptImporter;
import com.owiseman.office.word.WordImporter;
import com.owiseman.pdf.PdfImporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class AgentService {

    private final Map<String, Document> documentStore = new ConcurrentHashMap<>();

    public CompletableFuture<Agent.AgentResult> executeTask(String goal) {
        Agent agent = new Agent();
        return agent.execute(goal);
    }

    public CompletableFuture<PatchSet> generatePatch(String documentId, String instruction) {
        return CompletableFuture.supplyAsync(() -> {
            Agent agent = new Agent();
            return agent.generatePatch(documentId, instruction);
        });
    }

    public Document importDocument(Path filePath) throws IOException {
        String name = filePath.getFileName().toString().toLowerCase();
        DocumentType type;
        if (name.endsWith(".pptx") || name.endsWith(".ppt")) type = DocumentType.PPTX;
        else if (name.endsWith(".docx") || name.endsWith(".doc")) type = DocumentType.DOCX;
        else if (name.endsWith(".xlsx") || name.endsWith(".xls")) type = DocumentType.XLSX;
        else if (name.endsWith(".pdf")) type = DocumentType.PDF;
        else throw new IOException("Unsupported file format: " + name);

        Document doc = switch (type) {
            case PPTX -> new PptImporter().importDocument(filePath);
            case DOCX -> new WordImporter().importDocument(filePath);
            case XLSX -> new ExcelImporter().importDocument(filePath);
            case PDF -> new PdfImporter().importDocument(filePath);
            default -> throw new IOException("Unsupported type: " + type);
        };

        documentStore.put(doc.documentId(), doc);
        return doc;
    }

    public Document getDocument(String documentId) {
        return documentStore.get(documentId);
    }

    public Map<String, Document> getAllDocuments() {
        return Map.copyOf(documentStore);
    }
}
