package com.owiseman.gateway.protocol;

import com.owiseman.document.model.DocumentType;

public record DocumentImportRequest(
        String filePath,
        String format
) {
    public DocumentType resolveType() {
        if (format != null && !format.isBlank()) {
            return switch (format.toLowerCase()) {
                case "pptx", "ppt" -> DocumentType.PPTX;
                case "docx", "doc" -> DocumentType.DOCX;
                case "xlsx", "xls" -> DocumentType.XLSX;
                case "pdf" -> DocumentType.PDF;
                default -> DocumentType.UNKNOWN;
            };
        }
        if (filePath != null) {
            String lower = filePath.toLowerCase();
            if (lower.endsWith(".pptx") || lower.endsWith(".ppt")) return DocumentType.PPTX;
            if (lower.endsWith(".docx") || lower.endsWith(".doc")) return DocumentType.DOCX;
            if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) return DocumentType.XLSX;
            if (lower.endsWith(".pdf")) return DocumentType.PDF;
        }
        return DocumentType.UNKNOWN;
    }
}
