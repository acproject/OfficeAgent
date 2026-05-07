package com.owiseman.office.excel;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.BlockType;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.DocumentType;
import com.owiseman.document.model.Page;
import com.owiseman.document.model.Rect;
import com.owiseman.document.model.SemanticRole;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class ExcelImporter {

    private static final Logger LOG = Logger.getLogger(ExcelImporter.class.getName());

    public Document importDocument(Path filePath) throws IOException {
        LOG.info("Importing Excel: " + filePath);

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            List<Page> pages = new ArrayList<>();
            int sheetIndex = 0;

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                List<Block> blocks = new ArrayList<>();
                int blockIndex = 0;

                for (Row row : sheet) {
                    StringBuilder rowContent = new StringBuilder();
                    for (Cell cell : row) {
                        String cellValue = getCellValue(cell);
                        if (!cellValue.isEmpty()) {
                            if (!rowContent.isEmpty()) rowContent.append("\t");
                            rowContent.append(cellValue);
                        }
                    }
                    if (!rowContent.isEmpty()) {
                        Block block = Block.builder()
                                .blockId("sheet" + sheetIndex + "-row" + blockIndex)
                                .type(BlockType.TABLE)
                                .content(rowContent.toString())
                                .semanticRole(SemanticRole.TABLE)
                                .metadata("sheetName", sheet.getSheetName())
                                .metadata("rowIndex", row.getRowNum())
                                .build();
                        blocks.add(block);
                        blockIndex++;
                    }
                }

                Page page = Page.builder()
                        .pageIndex(sheetIndex)
                        .addBlock(Block.builder()
                                .blockId("sheet" + sheetIndex + "-title")
                                .type(BlockType.TEXT)
                                .content("Sheet: " + sheet.getSheetName())
                                .semanticRole(SemanticRole.TITLE)
                                .build())
                        .blocks(blocks)
                        .build();
                pages.add(page);
                sheetIndex++;
            }

            Document doc = Document.builder()
                    .documentId("xlsx-" + UUID.randomUUID().toString().substring(0, 8))
                    .documentType(DocumentType.XLSX)
                    .sourcePath(filePath.toString())
                    .pages(pages)
                    .build();

            LOG.info("Imported Excel with " + pages.size() + " sheets");
            return doc;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
