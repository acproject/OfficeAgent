package com.owiseman.pdf;

import com.owiseman.pdf.extract.PdfFontResolver;

public final class PdfFontResolverFacade {
    private final PdfFontResolver delegate = new PdfFontResolver();
    public PdfFontResolver delegate() { return delegate; }
}
