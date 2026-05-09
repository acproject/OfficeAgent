package com.owiseman.gateway.protocol;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.Page;

import java.util.ArrayList;
import java.util.List;

public record DocumentResponse(
        String documentId,
        String documentType,
        String sourcePath,
        int totalPages,
        List<PageSummary> pages
) {
    public static DocumentResponse from(Document doc) {
        List<PageSummary> pageSummaries = new ArrayList<>();
        for (Page page : doc.pages()) {
            List<BlockSummary> blockSummaries = new ArrayList<>();
            for (Block block : page.blocks()) {
                blockSummaries.add(new BlockSummary(
                        block.blockId(),
                        block.type().name(),
                        block.semanticRole().name(),
                        block.content() != null ? truncate(block.content(), 200) : null
                ));
            }
            pageSummaries.add(new PageSummary(page.pageIndex(), page.blocks().size(), blockSummaries));
        }
        return new DocumentResponse(
                doc.documentId(),
                doc.documentType().name(),
                doc.sourcePath(),
                doc.pages().size(),
                pageSummaries
        );
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    public record PageSummary(int pageIndex, int blockCount, List<BlockSummary> blocks) {}

    public record BlockSummary(String blockId, String type, String role, String contentPreview) {}
}
