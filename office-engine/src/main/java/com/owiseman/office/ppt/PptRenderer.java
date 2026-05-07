package com.owiseman.office.ppt;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.Page;
import com.owiseman.document.patch.DeleteBlockPatch;
import com.owiseman.document.patch.InsertBlockPatch;
import com.owiseman.document.patch.InsertPagePatch;
import com.owiseman.document.patch.PatchOperation;
import com.owiseman.document.patch.PatchSet;
import com.owiseman.document.patch.ReplaceTextPatch;
import com.owiseman.document.patch.UpdateStylePatch;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class PptRenderer implements com.owiseman.office.OfficeRenderer {

    private static final Logger LOG = Logger.getLogger(PptRenderer.class.getName());

    @Override
    public void applyPatch(Document document, PatchSet patchSet) {
        LOG.info("Applying " + patchSet.operations().size() + " patches to PPT document");
        for (PatchOperation op : patchSet.operations()) {
            applyOperation(document, op);
        }
    }

    @Override
    public void export(Document document, Path outputPath) throws IOException {
        LOG.info("Exporting PPT to: " + outputPath);
        try (XMLSlideShow slideshow = new XMLSlideShow()) {
            for (Page page : document.pages()) {
                XSLFSlide slide = slideshow.createSlide();
                for (Block block : page.blocks()) {
                    if (block.content() != null && !block.content().isBlank()) {
                        XSLFTextShape textShape = slide.createTextBox();
                        textShape.setText(block.content());
                        if (block.style() != null) {
                            applyTextStyle(textShape, block.style());
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                slideshow.write(fos);
            }
        }
        LOG.info("PPT exported successfully");
    }

    @Override
    public String formatName() {
        return "pptx";
    }

    private void applyOperation(Document document, PatchOperation op) {
        switch (op) {
            case ReplaceTextPatch p -> LOG.fine("ReplaceText: " + p.targetId());
            case InsertBlockPatch p -> LOG.fine("InsertBlock at page " + p.pageIndex());
            case DeleteBlockPatch p -> LOG.fine("DeleteBlock: " + p.targetId());
            case UpdateStylePatch p -> LOG.fine("UpdateStyle: " + p.targetId());
            case InsertPagePatch p -> LOG.fine("InsertPage after " + p.afterPageIndex());
            default -> LOG.fine("Unhandled patch type: " + op.operationType());
        }
    }

    private void applyTextStyle(XSLFTextShape shape, com.owiseman.document.model.TextStyle style) {
        if (style.fontFamily() != null) {
            shape.getTextParagraphs().forEach(p ->
                    p.getTextRuns().forEach(r -> r.setFontFamily(style.fontFamily())));
        }
        if (style.fontSize() > 0) {
            double fontSize = style.fontSize();
            shape.getTextParagraphs().forEach(p ->
                    p.getTextRuns().forEach(r -> r.setFontSize(fontSize)));
        }
    }
}
