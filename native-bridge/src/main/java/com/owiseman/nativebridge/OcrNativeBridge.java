package com.owiseman.nativebridge;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class OcrNativeBridge {

    private static final Logger LOG = Logger.getLogger(OcrNativeBridge.class.getName());

    private volatile boolean initialized;

    public OcrNativeBridge() {
        this.initialized = false;
    }

    public void initialize() {
        if (initialized) return;
        NativeLibraryLoader.loadLibrary("office_ocr");
        initialized = true;
        LOG.info("OCR Native Bridge initialized");
    }

    public OcrResult recognize(byte[] imageData, String format) {
        if (!initialized) {
            throw new IllegalStateException("OCR Native Bridge not initialized");
        }
        LOG.info("Performing OCR on " + imageData.length + " bytes of " + format + " image");
        return recognizeNative(imageData, format);
    }

    public OcrResult recognizeFile(Path imagePath) {
        if (!initialized) {
            throw new IllegalStateException("OCR Native Bridge not initialized");
        }
        LOG.info("Performing OCR on file: " + imagePath);
        return recognizeFileNative(imagePath.toString());
    }

    public void destroy() {
        if (initialized) {
            initialized = false;
            LOG.info("OCR Native Bridge destroyed");
        }
    }

    private native OcrResult recognizeNative(byte[] imageData, String format);

    private native OcrResult recognizeFileNative(String filePath);

    public record OcrResult(String text, float confidence, long processingTimeMs) {}
}
