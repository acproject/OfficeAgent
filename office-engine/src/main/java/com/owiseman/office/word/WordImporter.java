package com.owiseman.office.word;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.BlockType;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.DocumentType;
import com.owiseman.document.model.Page;
import com.owiseman.document.model.SemanticRole;
import com.owiseman.document.model.TextStyle;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class WordImporter {

    private static final Logger LOG = Logger.getLogger(WordImporter.class.getName());

    public Document importDocument(Path filePath) throws IOException {
        LOG.info("Importing Word document: " + filePath);

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument doc = new XWPFDocument(fis)) {

            List<Block> blocks = new ArrayList<>();
            int blockIndex = 0;

            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText();
                if (text == null || text.isBlank()) continue;

                SemanticRole role = determineRole(para.getStyle());
                TextStyle style = extractStyle(para);

                Block block = Block.builder()
                        .blockId("doc-block" + blockIndex)
                        .type(BlockType.TEXT)
                        .content(text)
                        .semanticRole(role)
                        .style(style)
                        .build();
                blocks.add(block);
                blockIndex++;
            }

            Page page = Page.builder()
                    .pageIndex(0)
                    .blocks(blocks)
                    .build();

            Document document = Document.builder()
                    .documentId("doc-" + UUID.randomUUID().toString().substring(0, 8))
                    .documentType(DocumentType.DOCX)
                    .sourcePath(filePath.toString())
                    .addPage(page)
                    .build();

            LOG.info("Imported Word document with " + blocks.size() + " blocks");
            return document;
        }
    }

    private SemanticRole determineRole(String style) {
        if (style == null) return SemanticRole.PARAGRAPH;
        return switch (style.toLowerCase()) {
            case "heading1", "heading 1" -> SemanticRole.TITLE;
            case "heading2", "heading 2" -> SemanticRole.SECTION_HEADER;
            case "heading3", "heading 3" -> SemanticRole.SUBTITLE;
            case "title" -> SemanticRole.TITLE;
            case "subtitle" -> SemanticRole.SUBTITLE;
            case "caption" -> SemanticRole.CAPTION;
            default -> SemanticRole.PARAGRAPH;
        };
    }

    private TextStyle extractStyle(XWPFParagraph para) {
        TextStyle.Builder builder = TextStyle.builder();
        if (!para.getRuns().isEmpty()) {
            XWPFRun run = para.getRuns().getFirst();
            if (run.getFontFamily() != null) builder.fontFamily(run.getFontFamily());
            if (run.getFontSizeAsDouble() != null) builder.fontSize(run.getFontSizeAsDouble().floatValue());
            builder.bold(run.isBold()).italic(run.isItalic());
        }
        return builder.build();
    }
}
