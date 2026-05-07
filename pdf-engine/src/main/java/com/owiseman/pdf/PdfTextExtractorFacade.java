package com.owiseman.pdf;

import com.owiseman.pdf.extract.PdfTextExtractor;

public final class PdfTextExtractorFacade {
    private final PdfTextExtractor delegate = new PdfTextExtractor();
    public PdfTextExtractor delegate() { return delegate; }
}
