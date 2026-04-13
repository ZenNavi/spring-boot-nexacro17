package com.example.nexacro.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.*;

@Component
public class NexacroGridExcelBuilder {

    public byte[] build(List<ColumnMeta> columns, List<BandMeta> bands,
                        List<Map<String, Object>> dataRows,
                        ComboResolver comboResolver) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            boolean hasBands = bands != null && !bands.isEmpty();

            int dataStartRow = createHeader(workbook, sheet, columns, bands, hasBands);
            createDataRows(workbook, sheet, columns, dataRows, comboResolver, dataStartRow);
            setColumnWidths(sheet, columns);
            sheet.createFreezePane(0, dataStartRow);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── Header ───────────────────────────────────────────────────────────

    private int createHeader(XSSFWorkbook wb, Sheet sheet, List<ColumnMeta> columns,
                             List<BandMeta> bands, boolean hasBands) {
        if (!hasBands) {
            Row row = sheet.createRow(0);
            row.setHeightInPoints(20);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(columns.get(i).getHeaderText());
                cell.setCellStyle(buildHeaderStyle(wb, columns.get(i).getBgColor(),
                        columns.get(i).isFontBold(), columns.get(i).getFontSize()));
            }
            return 1;
        }

        // Sort bands by bandOrder
        List<BandMeta> sortedBands = new ArrayList<>(bands);
        sortedBands.sort(Comparator.comparingInt(BandMeta::getBandOrder));

        Row row0 = sheet.createRow(0);
        row0.setHeightInPoints(20);
        Row row1 = sheet.createRow(1);
        row1.setHeightInPoints(18);

        // Track which band IDs have been placed in row0
        Set<String> placedBands = new HashSet<>();

        for (int i = 0; i < columns.size(); i++) {
            ColumnMeta col = columns.get(i);
            if (col.getBandId() == null || col.getBandId().isEmpty()) {
                // No-band column: spans 2 rows
                Cell cell = row0.createCell(i);
                cell.setCellValue(col.getHeaderText());
                cell.setCellStyle(buildHeaderStyle(wb, col.getBgColor(), col.isFontBold(), col.getFontSize()));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, i, i));
            } else {
                // Band column: place band header in row0 (once per band)
                if (!placedBands.contains(col.getBandId())) {
                    BandMeta band = findBand(sortedBands, col.getBandId());
                    if (band != null) {
                        Cell bandCell = row0.createCell(i);
                        bandCell.setCellValue(band.getBandText());
                        bandCell.setCellStyle(buildBandStyle(wb, band));
                        if (band.getColSpan() > 1) {
                            sheet.addMergedRegion(
                                new CellRangeAddress(0, 0, i, i + band.getColSpan() - 1));
                        }
                        placedBands.add(col.getBandId());
                    }
                }
                // Column header in row1
                Cell colCell = row1.createCell(i);
                colCell.setCellValue(col.getHeaderText());
                colCell.setCellStyle(buildHeaderStyle(wb, col.getBgColor(), col.isFontBold(), col.getFontSize()));
            }
        }
        return 2;
    }

    private BandMeta findBand(List<BandMeta> bands, String bandId) {
        for (BandMeta b : bands) {
            if (b.getBandId().equals(bandId)) return b;
        }
        return null;
    }

    // ── Data Rows ─────────────────────────────────────────────────────────

    private void createDataRows(XSSFWorkbook wb, Sheet sheet, List<ColumnMeta> columns,
                                List<Map<String, Object>> dataRows, ComboResolver comboResolver,
                                int startRowIdx) {
        Map<String, CellStyle> styleCache = new HashMap<>();
        for (int r = 0; r < dataRows.size(); r++) {
            Row row = sheet.createRow(startRowIdx + r);
            row.setHeightInPoints(16);
            Map<String, Object> data = dataRows.get(r);
            for (int c = 0; c < columns.size(); c++) {
                ColumnMeta col = columns.get(c);
                Cell cell = row.createCell(c);
                setCellValue(cell, col, data.get(col.getColId()), comboResolver);
                cell.setCellStyle(getOrCreateDataStyle(wb, col, styleCache));
            }
        }
    }

    private void setCellValue(Cell cell, ColumnMeta col, Object value, ComboResolver comboResolver) {
        if (value == null) { cell.setCellValue(""); return; }
        if ("combo".equals(col.getEditType())) {
            cell.setCellValue(comboResolver.resolve(col.getComboGroupCd(), value.toString()));
        } else if ("number".equals(col.getColType())) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                try { cell.setCellValue(Double.parseDouble(value.toString())); }
                catch (NumberFormatException e) { cell.setCellValue(value.toString()); }
            }
        } else {
            cell.setCellValue(value.toString());
        }
    }

    // ── Styles ────────────────────────────────────────────────────────────

    private CellStyle buildHeaderStyle(XSSFWorkbook wb, String bgColor, boolean bold, int fontSize) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short)(fontSize > 0 ? fontSize : 10));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, "thin");
        applyBgColor(style, bgColor != null ? bgColor : "#BDD7EE");
        return style;
    }

    private CellStyle buildBandStyle(XSSFWorkbook wb, BandMeta band) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(band.isFontBold());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(toHAlign(band.getTextAlign()));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, "thin");
        applyBgColor(style, band.getBgColor() != null ? band.getBgColor() : "#D9E1F2");
        return style;
    }

    private CellStyle getOrCreateDataStyle(XSSFWorkbook wb, ColumnMeta col,
                                           Map<String, CellStyle> cache) {
        String key = col.getColId() + "|" + col.getTextAlign() + "|"
                   + col.getNumberFormat() + "|" + col.getBgColor()
                   + "|" + col.isFontBold() + "|" + col.getBorderStyle();
        return cache.computeIfAbsent(key, k -> buildDataStyle(wb, col));
    }

    private CellStyle buildDataStyle(XSSFWorkbook wb, ColumnMeta col) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(col.isFontBold());
        font.setFontHeightInPoints((short)(col.getFontSize() > 0 ? col.getFontSize() : 10));
        style.setFont(font);
        style.setAlignment(toHAlign(col.getTextAlign()));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, col.getBorderStyle());
        if (col.getBgColor() != null) applyBgColor(style, col.getBgColor());
        if ("number".equals(col.getColType()) && col.getNumberFormat() != null) {
            DataFormat dataFormat = wb.createDataFormat();
            style.setDataFormat(dataFormat.getFormat(col.getNumberFormat()));
        }
        return style;
    }

    private HorizontalAlignment toHAlign(String align) {
        if (align == null) return HorizontalAlignment.LEFT;
        switch (align.toLowerCase()) {
            case "center": return HorizontalAlignment.CENTER;
            case "right":  return HorizontalAlignment.RIGHT;
            default:       return HorizontalAlignment.LEFT;
        }
    }

    private void applyBorder(XSSFCellStyle style, String borderStyle) {
        BorderStyle bs = BorderStyle.THIN;
        if ("medium".equals(borderStyle)) bs = BorderStyle.MEDIUM;
        else if ("thick".equals(borderStyle))  bs = BorderStyle.THICK;
        else if ("none".equals(borderStyle))   bs = BorderStyle.NONE;
        style.setBorderTop(bs); style.setBorderBottom(bs);
        style.setBorderLeft(bs); style.setBorderRight(bs);
    }

    private void applyBgColor(XSSFCellStyle style, String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#")) return;
        try {
            Color c = Color.decode(hexColor);
            XSSFColor xssfColor = new XSSFColor(c, null);
            style.setFillForegroundColor(xssfColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } catch (NumberFormatException ignored) {}
    }

    private void setColumnWidths(Sheet sheet, List<ColumnMeta> columns) {
        for (int i = 0; i < columns.size(); i++) {
            int width = columns.get(i).getColWidth();
            sheet.setColumnWidth(i, width <= 0 ? 3000 : width * 36);
        }
    }
}
