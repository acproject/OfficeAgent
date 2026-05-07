package com.owiseman.pdf;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.BlockType;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.DocumentType;
import com.owiseman.document.model.Page;
import com.owiseman.document.model.Rect;
import com.owiseman.document.model.SemanticRole;
import com.owiseman.document.model.TextStyle;
import com.owiseman.pdf.extract.PdfTextExtractor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class PdfImporter {

    private static final Logger LOG = Logger.getLogger(PdfImporter.class.getName());

    private final PdfTextExtractor textExtractor = new PdfTextExtractor();

    public Document importDocument(Path filePath) throws IOException {
        LOG.info("Importing PDF: " + filePath);

        try (PDDocument pdDoc = Loader.loadPDF(filePath.toFile())) {
            int totalPages = pdDoc.getNumberOfPages();
            List<Page> pages = new ArrayList<>();

            for (int i = 0; i < totalPages; i++) {
                List<PdfTextExtractor.TextBlock> textBlocks =
                        textExtractor.extractTextWithPositions(filePath);

                List<Block> blocks = new ArrayList<>();
                int blockIdx = 0;

                for (PdfTextExtractor.TextBlock tb : textBlocks) {
                    SemanticRole role = inferRole(tb.fontSize(), tb.fontName());
                    TextStyle style = TextStyle.builder()
                            .fontFamily(tb.fontName())
                            .fontSize(tb.fontSize())
                            .build();

                    Block block = Block.builder()
                            .blockId("page" + i + "-block" + blockIdx)
                            .type(BlockType.TEXT)
                            .content(tb.text())
                            .bbox(Rect.of(tb.x(), tb.y(), tb.width(), tb.height()))
                            .semanticRole(role)
                            .style(style)
                            .build();
                    blocks.add(block);
                    blockIdx++;
                }

                Page page = Page.builder()
                        .pageIndex(i)
                        .blocks(blocks)
                        .build();
                pages.add(page);
            }

            Document doc = Document.builder()
                    .documentId("pdf-" + UUID.randomUUID().toString().substring(0, 8))
                    .documentType(DocumentType.PDF)
                    .sourcePath(filePath.toString())
                    .pages(pages)
                    .build();

            LOG.info("Imported PDF with " + totalPages + " pages");
            return doc;
        }
    }

    private SemanticRole inferRole(float fontSize, String fontName) {
        if (fontSize >= 24f) return SemanticRole.TITLE;
        if (fontSize >= 18f) return SemanticRole.SECTION_HEADER;
        if (fontSize >= 14f) return SemanticRole.SUBTITLE;
        if (fontName != null && fontName.toLowerCase().contains("bold")) return SemanticRole.SECTION_HEADER;
        return SemanticRole.PARAGRAPH;
    }
}
