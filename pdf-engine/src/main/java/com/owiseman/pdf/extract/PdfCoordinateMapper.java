package com.owiseman.pdf.extract;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class PdfCoordinateMapper {

    private static final Logger LOG = Logger.getLogger(PdfCoordinateMapper.class.getName());

    public List<CoordinateRegion> mapRegions(Path filePath) throws IOException {
        List<CoordinateRegion> regions = new ArrayList<>();

        try (PDDocument doc = Loader.loadPDF(filePath.toFile())) {
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                var page = doc.getPage(i);
                var mediaBox = page.getMediaBox();

                regions.add(new CoordinateRegion(
                        i,
                        mediaBox.getLowerLeftX(),
                        mediaBox.getLowerLeftY(),
                        mediaBox.getUpperRightX(),
                        mediaBox.getUpperRightY(),
                        mediaBox.getWidth(),
                        mediaBox.getHeight()
                ));
            }
        }

        return regions;
    }

    public record CoordinateRegion(int pageIndex, float lowerLeftX, float lowerLeftY,
                                   float upperRightX, float upperRightY,
                                   float width, float height) {}
}
