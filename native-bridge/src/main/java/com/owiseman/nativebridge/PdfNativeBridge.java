package com.owiseman.nativebridge;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class PdfNativeBridge {

    private static final Logger LOG = Logger.getLogger(PdfNativeBridge.class.getName());

    private volatile boolean initialized;

    public PdfNativeBridge() {
        this.initialized = false;
    }

    public void initialize() {
        if (initialized) return;
        NativeLibraryLoader.loadLibrary("office_pdf");
        initialized = true;
        LOG.info("PDF Native Bridge initialized");
    }

    public byte[] renderPage(Path pdfPath, int pageIndex, float dpi) {
        if (!initialized) {
            throw new IllegalStateException("PDF Native Bridge not initialized");
        }
        LOG.info("Rendering PDF page " + pageIndex + " at " + dpi + " DPI");
        return renderPageNative(pdfPath.toString(), pageIndex, dpi);
    }

    public LayoutResult analyzeLayout(Path pdfPath, int pageIndex) {
        if (!initialized) {
            throw new IllegalStateException("PDF Native Bridge not initialized");
        }
        LOG.info("Analyzing layout of PDF page " + pageIndex);
        return analyzeLayoutNative(pdfPath.toString(), pageIndex);
    }

    public void destroy() {
        if (initialized) {
            initialized = false;
            LOG.info("PDF Native Bridge destroyed");
        }
    }

    private native byte[] renderPageNative(String pdfPath, int pageIndex, float dpi);

    private native LayoutResult analyzeLayoutNative(String pdfPath, int pageIndex);

    public record LayoutResult(int regionCount, String layoutJson, float confidence) {}
}
