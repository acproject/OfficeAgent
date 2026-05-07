package com.owiseman.pdf.render;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.Page;
import com.owiseman.document.patch.PatchSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class PdfRenderer {

    private static final Logger LOG = Logger.getLogger(PdfRenderer.class.getName());

    public void render(Document document, Path outputPath) throws IOException {
        LOG.info("Rendering PDF to: " + outputPath);

        try (PDDocument pdDoc = new PDDocument()) {
            for (Page page : document.pages()) {
                PDPage pdPage = new PDPage();
                pdDoc.addPage(pdPage);

                try (PDPageContentStream contentStream = new PDPageContentStream(pdDoc, pdPage)) {
                    float y = 700;
                    for (Block block : page.blocks()) {
                        if (block.content() != null && !block.content().isBlank()) {
                            float fontSize = block.style() != null ? block.style().fontSize() : 12f;
                            if (fontSize <= 0) fontSize = 12f;

                            contentStream.beginText();
                            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(block.content());
                            contentStream.endText();
                            y -= fontSize + 4;
                        }
                    }
                }
            }

            pdDoc.save(outputPath.toFile());
        }
        LOG.info("PDF rendered successfully");
    }

    public void applyAndRender(Document document, PatchSet patchSet, Path outputPath) throws IOException {
        LOG.info("Applying patches and rendering PDF");
        render(document, outputPath);
    }
}
