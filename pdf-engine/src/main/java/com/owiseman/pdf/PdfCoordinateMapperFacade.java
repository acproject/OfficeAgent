package com.owiseman.pdf;

import com.owiseman.pdf.extract.PdfCoordinateMapper;

public final class PdfCoordinateMapperFacade {
    private final PdfCoordinateMapper delegate = new PdfCoordinateMapper();
    public PdfCoordinateMapper delegate() { return delegate; }
}
