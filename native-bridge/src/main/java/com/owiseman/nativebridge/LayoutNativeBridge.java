package com.owiseman.nativebridge;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class LayoutNativeBridge {

    private static final Logger LOG = Logger.getLogger(LayoutNativeBridge.class.getName());

    private volatile boolean initialized;

    public LayoutNativeBridge() {
        this.initialized = false;
    }

    public void initialize() {
        if (initialized) return;
        NativeLibraryLoader.loadLibrary("office_layout");
        initialized = true;
        LOG.info("Layout Native Bridge initialized");
    }

    public LayoutAnalysisResult analyzeLayout(byte[] imageData, String format) {
        if (!initialized) {
            throw new IllegalStateException("Layout Native Bridge not initialized");
        }
        LOG.info("Analyzing layout of " + format + " image (" + imageData.length + " bytes)");
        return analyzeLayoutNative(imageData, format);
    }

    public LayoutAnalysisResult analyzeLayoutFile(Path imagePath) {
        if (!initialized) {
            throw new IllegalStateException("Layout Native Bridge not initialized");
        }
        LOG.info("Analyzing layout of file: " + imagePath);
        return analyzeLayoutFileNative(imagePath.toString());
    }

    public void destroy() {
        if (initialized) {
            initialized = false;
            LOG.info("Layout Native Bridge destroyed");
        }
    }

    private native LayoutAnalysisResult analyzeLayoutNative(byte[] imageData, String format);

    private native LayoutAnalysisResult analyzeLayoutFileNative(String filePath);

    public record LayoutAnalysisResult(String regionsJson, int regionCount, float confidence) {}
}
