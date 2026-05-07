package com.owiseman.office.ppt;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.BlockType;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.DocumentType;
import com.owiseman.document.model.Page;
import com.owiseman.document.model.Rect;
import com.owiseman.document.model.SemanticRole;
import com.owiseman.document.model.TextStyle;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class PptImporter {

    private static final Logger LOG = Logger.getLogger(PptImporter.class.getName());

    public Document importDocument(Path filePath) throws IOException {
        LOG.info("Importing PPT: " + filePath);

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XMLSlideShow slideshow = new XMLSlideShow(fis)) {

            Dimension pageSize = slideshow.getPageSize();
            float pageWidth = (float) pageSize.getWidth();
            float pageHeight = (float) pageSize.getHeight();

            List<Page> pages = new ArrayList<>();
            int slideIndex = 0;

            for (XSLFSlide slide : slideshow.getSlides()) {
                Page.Builder pageBuilder = Page.builder()
                        .pageIndex(slideIndex)
                        .width(pageWidth)
                        .height(pageHeight);

                int blockIndex = 0;
                for (XSLFTextShape shape : slide.getPlaceholders()) {
                    String text = shape.getText();
                    if (text != null && !text.isBlank()) {
                        Placeholder ph = shape.getPlaceholder();
                        SemanticRole role = determineRole(ph);
                        TextStyle style = extractStyle(shape);

                        Block block = Block.builder()
                                .blockId("slide" + slideIndex + "-block" + blockIndex)
                                .type(BlockType.TEXT)
                                .content(text)
                                .semanticRole(role)
                                .style(style)
                                .build();
                        pageBuilder.addBlock(block);
                        blockIndex++;
                    }
                }

                pages.add(pageBuilder.build());
                slideIndex++;
            }

            Document doc = Document.builder()
                    .documentId("ppt-" + UUID.randomUUID().toString().substring(0, 8))
                    .documentType(DocumentType.PPTX)
                    .sourcePath(filePath.toString())
                    .pages(pages)
                    .build();

            LOG.info("Imported PPT with " + pages.size() + " slides");
            return doc;
        }
    }

    private SemanticRole determineRole(Placeholder placeholder) {
        if (placeholder == null) return SemanticRole.PARAGRAPH;
        return switch (placeholder) {
            case TITLE -> SemanticRole.TITLE;
            case CENTERED_TITLE -> SemanticRole.TITLE;
            case SUBTITLE -> SemanticRole.SUBTITLE;
            case FOOTER -> SemanticRole.FOOTER;
            case HEADER -> SemanticRole.HEADER;
            default -> SemanticRole.PARAGRAPH;
        };
    }

    private TextStyle extractStyle(XSLFTextShape shape) {
        TextStyle.Builder styleBuilder = TextStyle.builder();
        if (!shape.getTextParagraphs().isEmpty()) {
            XSLFTextParagraph para = shape.getTextParagraphs().getFirst();
            if (!para.getTextRuns().isEmpty()) {
                XSLFTextRun run = para.getTextRuns().getFirst();
                styleBuilder.fontFamily(run.getFontFamily());
                Double fontSize = run.getFontSize();
                if (fontSize != null) {
                    styleBuilder.fontSize(fontSize.floatValue());
                }
                styleBuilder.bold(run.isBold());
                styleBuilder.italic(run.isItalic());
            }
        }
        return styleBuilder.build();
    }
}
