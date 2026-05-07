package com.owiseman.pdf;

import com.owiseman.pdf.render.PdfRenderer;

public final class PdfRendererFacade {
    private final PdfRenderer delegate = new PdfRenderer();
    public PdfRenderer delegate() { return delegate; }
}
