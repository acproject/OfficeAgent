package com.owiseman.pdf.extract;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class PdfFontResolver {

    private static final Logger LOG = Logger.getLogger(PdfFontResolver.class.getName());

    public Map<String, FontInfo> resolveFonts(Path filePath) throws IOException {
        Map<String, FontInfo> fonts = new HashMap<>();

        try (PDDocument doc = Loader.loadPDF(filePath.toFile())) {
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                PDResources resources = doc.getPage(i).getResources();
                for (COSName cosName : resources.getFontNames()) {
                    PDFont font = resources.getFont(cosName);
                    if (font != null) {
                        FontInfo info = new FontInfo(
                                font.getName(),
                                font.getType(),
                                font.isEmbedded()
                        );
                        fonts.put(font.getName(), info);
                    }
                }
            }
        }

        LOG.info("Resolved " + fonts.size() + " fonts from PDF");
        return fonts;
    }

    public record FontInfo(String name, String type, boolean embedded) {}
}
