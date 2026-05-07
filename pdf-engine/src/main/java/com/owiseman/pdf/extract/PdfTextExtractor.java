package com.owiseman.pdf.extract;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class PdfTextExtractor {

    private static final Logger LOG = Logger.getLogger(PdfTextExtractor.class.getName());

    public String extractText(Path filePath) throws IOException {
        try (PDDocument doc = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    public String extractText(Path filePath, int startPage, int endPage) throws IOException {
        try (PDDocument doc = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            return stripper.getText(doc);
        }
    }

    public List<TextBlock> extractTextWithPositions(Path filePath) throws IOException {
        try (PDDocument doc = Loader.loadPDF(filePath.toFile())) {
            List<TextBlock> blocks = new ArrayList<>();
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    super.writeString(text, textPositions);
                    if (!textPositions.isEmpty()) {
                        TextPosition first = textPositions.getFirst();
                        TextPosition last = textPositions.getLast();
                        blocks.add(new TextBlock(
                                text,
                                first.getX(),
                                first.getY(),
                                last.getX() + last.getWidth() - first.getX(),
                                first.getHeight(),
                                first.getFont().getName(),
                                first.getFontSizeInPt()
                        ));
                    }
                }
            };
            stripper.getText(doc);
            return blocks;
        }
    }

    public record TextBlock(String text, float x, float y, float width, float height,
                            String fontName, float fontSize) {}
}
