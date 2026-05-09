package com.owiseman.gateway.handler;

import com.owiseman.document.model.Document;
import com.owiseman.document.model.DocumentType;
import com.owiseman.gateway.protocol.ApiResponse;
import com.owiseman.gateway.protocol.DocumentImportRequest;
import com.owiseman.gateway.protocol.DocumentResponse;
import com.owiseman.office.excel.ExcelImporter;
import com.owiseman.office.ppt.PptImporter;
import com.owiseman.office.word.WordImporter;
import com.owiseman.pdf.PdfImporter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DocumentHandler implements RequestHandler {

    private final Map<String, Document> documentStore = new ConcurrentHashMap<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleCors(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("POST".equalsIgnoreCase(method) && path.endsWith("/document/import")) {
            handleImport(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.contains("/document/")) {
            handleGetDocument(exchange);
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/document/export")) {
            handleExport(exchange);
        } else {
            sendError(exchange, 404, "Not found: " + path);
        }
    }

    private void handleImport(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        DocumentImportRequest request = parseRequest(body);

        if (request == null || request.filePath() == null || request.filePath().isBlank()) {
            sendError(exchange, 400, "Missing required field: filePath");
            return;
        }

        Path filePath = Path.of(request.filePath());
        if (!filePath.toFile().exists()) {
            sendError(exchange, 404, "File not found: " + request.filePath());
            return;
        }

        DocumentType type = request.resolveType();
        if (type == DocumentType.UNKNOWN) {
            sendError(exchange, 400, "Unsupported document format: " + request.filePath());
            return;
        }

        try {
            Document doc = importDocument(filePath, type);
            documentStore.put(doc.documentId(), doc);

            DocumentResponse response = DocumentResponse.from(doc);
            sendOk(exchange, toJson(ApiResponse.ok("Document imported", response)));
        } catch (Exception e) {
            sendError(exchange, 500, "Import failed: " + e.getMessage());
        }
    }

    private void handleGetDocument(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String docId = extractDocId(path);

        if (docId == null || docId.isBlank()) {
            sendOk(exchange, toJson(ApiResponse.ok(documentStore.keySet().stream().toList())));
            return;
        }

        Document doc = documentStore.get(docId);
        if (doc == null) {
            sendError(exchange, 404, "Document not found: " + docId);
            return;
        }

        DocumentResponse response = DocumentResponse.from(doc);
        sendOk(exchange, toJson(ApiResponse.ok(response)));
    }

    private void handleExport(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String docId = extractField(body, "documentId");
        String outputPath = extractField(body, "outputPath");

        if (docId == null || outputPath == null) {
            sendError(exchange, 400, "Missing required fields: documentId, outputPath");
            return;
        }

        Document doc = documentStore.get(docId);
        if (doc == null) {
            sendError(exchange, 404, "Document not found: " + docId);
            return;
        }

        sendOk(exchange, toJson(ApiResponse.ok(Map.of(
                "documentId", docId,
                "outputPath", outputPath,
                "status", "export_initiated"
        ))));
    }

    private Document importDocument(Path filePath, DocumentType type) throws IOException {
        return switch (type) {
            case PPTX -> new PptImporter().importDocument(filePath);
            case DOCX -> new WordImporter().importDocument(filePath);
            case XLSX -> new ExcelImporter().importDocument(filePath);
            case PDF -> new PdfImporter().importDocument(filePath);
            default -> throw new IOException("Unsupported type: " + type);
        };
    }

    private DocumentImportRequest parseRequest(String body) {
        if (body == null || body.isBlank()) return null;
        return new DocumentImportRequest(
                extractField(body, "filePath"),
                extractField(body, "format")
        );
    }

    private String extractDocId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("document".equals(parts[i]) && i + 1 < parts.length) {
                String next = parts[i + 1];
                if (!next.isBlank() && !"import".equals(next) && !"export".equals(next)) {
                    return next;
                }
            }
        }
        return null;
    }

    private String extractField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + key.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return null;
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return end > start ? json.substring(start + 1, end) : null;
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }
}
