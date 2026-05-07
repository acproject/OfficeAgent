package com.owiseman.office.word;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.Page;
import com.owiseman.document.model.TextStyle;
import com.owiseman.document.patch.PatchSet;
import com.owiseman.office.OfficeRenderer;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class WordRenderer implements OfficeRenderer {

    private static final Logger LOG = Logger.getLogger(WordRenderer.class.getName());

    @Override
    public void applyPatch(Document document, PatchSet patchSet) {
        LOG.info("Applying " + patchSet.operations().size() + " patches to Word document");
    }

    @Override
    public void export(Document document, Path outputPath) throws IOException {
        LOG.info("Exporting Word document to: " + outputPath);

        try (XWPFDocument doc = new XWPFDocument()) {
            for (Page page : document.pages()) {
                for (Block block : page.blocks()) {
                    XWPFParagraph para = doc.createParagraph();
                    XWPFRun run = para.createRun();
                    run.setText(block.content());

                    if (block.style() != null) {
                        applyStyle(run, block.style());
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                doc.write(fos);
            }
        }
        LOG.info("Word document exported successfully");
    }

    @Override
    public String formatName() {
        return "docx";
    }

    private void applyStyle(XWPFRun run, TextStyle style) {
        if (style.fontFamily() != null) run.setFontFamily(style.fontFamily());
        if (style.fontSize() > 0) run.setFontSize(style.fontSize());
        run.setBold(style.bold());
        run.setItalic(style.italic());
    }
}
