package com.owiseman.office.ppt;

import com.owiseman.document.model.Document;

import java.io.IOException;
import java.nio.file.Path;

public final class PptImporterFacade {

    private final PptImporter delegate = new PptImporter();

    public Document importDocument(Path filePath) throws IOException {
        return delegate.importDocument(filePath);
    }
}
