package com.owiseman.office.excel;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.Page;
import com.owiseman.document.patch.PatchSet;
import com.owiseman.office.OfficeRenderer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class ExcelRenderer implements OfficeRenderer {

    private static final Logger LOG = Logger.getLogger(ExcelRenderer.class.getName());

    @Override
    public void applyPatch(Document document, PatchSet patchSet) {
        LOG.info("Applying " + patchSet.operations().size() + " patches to Excel document");
    }

    @Override
    public void export(Document document, Path outputPath) throws IOException {
        LOG.info("Exporting Excel to: " + outputPath);

        try (Workbook workbook = new XSSFWorkbook()) {
            for (Page page : document.pages()) {
                String sheetName = "Sheet" + (page.pageIndex() + 1);
                Sheet sheet = workbook.createSheet(sheetName);

                int rowIndex = 0;
                for (Block block : page.blocks()) {
                    if (block.content() != null && !block.content().isBlank()) {
                        Row row = sheet.createRow(rowIndex++);
                        String[] values = block.content().split("\t");
                        for (int col = 0; col < values.length; col++) {
                            row.createCell(col).setCellValue(values[col]);
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                workbook.write(fos);
            }
        }
        LOG.info("Excel exported successfully");
    }

    @Override
    public String formatName() {
        return "xlsx";
    }
}
