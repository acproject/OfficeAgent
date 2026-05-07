package com.owiseman.office;

import java.nio.file.Path;

public interface OfficeRenderer {
    void applyPatch(DocumentPatch patch);
    void export(Path path);

}
