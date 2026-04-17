package com.example.nexacro.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class NexacroGridExcelBuilder {

    /**
     * 기존 호출 호환을 위한 기본 진입점이다.
     *
     * <p>List 기반 데이터를 받아 기본 렌더링 정책으로 Excel을 생성한다.</p>
     */
    public byte[] build(List<ColumnMeta> columns, List<BandMeta> bands,
                        List<Map<String, Object>> dataRows,
                        ComboResolver comboResolver) throws Exception {
        return build(columns, bands, dataRows, comboResolver, new DefaultExcelRenderPolicy());
    }

    /**
     * List 기반 데이터 + 사용자 지정 렌더링 정책으로 Excel을 생성한다.
     */
    public byte[] build(List<ColumnMeta> columns, List<BandMeta> bands,
                        List<Map<String, Object>> dataRows,
                        ComboResolver comboResolver,
                        ExcelRenderPolicy renderPolicy) throws Exception {
        return build(columns, bands, ExcelRowWriter.fromList(dataRows), comboResolver, renderPolicy);
    }

    /**
     * Row 단위 공급 방식까지 포함한 최종 build 진입점이다.
     *
     * <p>{@link SXSSFWorkbook}을 사용해 대량 row 처리 시 메모리 사용량을 줄인다.</p>
     */
    public byte[] build(List<ColumnMeta> columns, List<BandMeta> bands,
                        ExcelRowWriter rowWriter,
                        ComboResolver comboResolver,
                        ExcelRenderPolicy renderPolicy) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(200);
        workbook.setCompressTempFiles(true);
        try {
            Sheet sheet = workbook.createSheet("Sheet1");
            boolean hasBands = bands != null && !bands.isEmpty();

            int dataStartRow = createHeader(workbook, sheet, columns, bands, hasBands);
            createDataRows(workbook, sheet, columns, rowWriter, comboResolver, renderPolicy, dataStartRow);
            setColumnWidths(sheet, columns);
            sheet.createFreezePane(0, dataStartRow);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    // ── Header ───────────────────────────────────────────────────────────

    /**
     * Grid 메타데이터를 기반으로 헤더 영역을 생성한다.
     *
     * <p>band가 없으면 1줄, band가 있으면 상단 band / 하단 column 헤더 2줄 구조를 만든다.</p>
     */
    private int createHeader(SXSSFWorkbook wb, Sheet sheet, List<ColumnMeta> columns,
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

    /**
     * 공급된 row 데이터를 순차적으로 Excel data row로 기록한다.
     *
     * <p>이 메서드가 builder 확장의 핵심이다. row source는 List일 수도 있고,
     * ResultHandler 기반 스트리밍일 수도 있다.</p>
     */
    private void createDataRows(SXSSFWorkbook wb, Sheet sheet, List<ColumnMeta> columns,
                                ExcelRowWriter rowWriter, ComboResolver comboResolver,
                                ExcelRenderPolicy renderPolicy,
                                int startRowIdx) {
        final Map<String, CellStyle> styleCache = new HashMap<String, CellStyle>();
        final int[] rowIndex = new int[]{startRowIdx};
        try {
            rowWriter.writeRows(new ExcelRowConsumer() {
                @Override
                public void accept(Map<String, Object> data) throws Exception {
                    Row row = sheet.createRow(rowIndex[0]);
                    row.setHeightInPoints(16);
                    int relativeRowIndex = rowIndex[0] - startRowIdx;
                    for (int c = 0; c < columns.size(); c++) {
                        ColumnMeta col = columns.get(c);
                        Object rawValue = data.get(col.getColId());
                        ExcelCellContext context = new ExcelCellContext(
                                col, data, rawValue, relativeRowIndex, c, comboResolver);
                        // 값 변환과 스타일 변환은 정책 객체에 위임한다.
                        Object resolvedValue = renderPolicy.resolveCellValue(context);
                        ExcelCellStyleSpec styleSpec = renderPolicy.resolveCellStyle(context);
                        Cell cell = row.createCell(c);
                        setCellValue(cell, col, resolvedValue);
                        cell.setCellStyle(getOrCreateDataStyle(wb, col, styleSpec, styleCache));
                    }
                    rowIndex[0]++;
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to write excel rows", ex);
        }
    }

    private void setCellValue(Cell cell, ColumnMeta col, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        if ("number".equals(col.getColType()) && value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
            return;
        }
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
            return;
        }
        if (value instanceof Boolean) {
            cell.setCellValue(((Boolean) value).booleanValue());
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }

    private CellStyle buildHeaderStyle(SXSSFWorkbook wb, String bgColor, boolean bold, int fontSize) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) (fontSize > 0 ? fontSize : 10));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, "thin");
        applyBgColor(style, bgColor != null ? bgColor : "#BDD7EE");
        return style;
    }

    private CellStyle buildBandStyle(SXSSFWorkbook wb, BandMeta band) {
        CellStyle style = wb.createCellStyle();
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

    private CellStyle getOrCreateDataStyle(SXSSFWorkbook wb, ColumnMeta col,
                                           ExcelCellStyleSpec styleSpec,
                                           Map<String, CellStyle> cache) {
        String key = buildStyleKey(col, styleSpec);
        CellStyle style = cache.get(key);
        if (style == null) {
            style = buildDataStyle(wb, col, styleSpec);
            cache.put(key, style);
        }
        return style;
    }

    /**
     * 스타일 캐시 키를 만든다.
     *
     * <p>셀마다 스타일 객체를 새로 만들면 POI 성능과 메모리 사용량이 불리해지므로
     * 동일 조건의 스타일은 재사용한다.</p>
     */
    private String buildStyleKey(ColumnMeta col, ExcelCellStyleSpec styleSpec) {
        return col.getColId() + "|" + valueOrEmpty(styleSpec.getTextAlign(), col.getTextAlign()) + "|"
                + valueOrEmpty(styleSpec.getNumberFormat(), col.getNumberFormat()) + "|"
                + valueOrEmpty(styleSpec.getBackgroundColor(), col.getBgColor()) + "|"
                + valueOrEmpty(styleSpec.getFontBold(), Boolean.valueOf(col.isFontBold())) + "|"
                + valueOrEmpty(styleSpec.getFontSize(), Integer.valueOf(col.getFontSize())) + "|"
                + valueOrEmpty(styleSpec.getBorderStyle(), col.getBorderStyle());
    }

    private String valueOrEmpty(Object overrideValue, Object baseValue) {
        Object value = overrideValue != null ? overrideValue : baseValue;
        return value == null ? "" : String.valueOf(value);
    }

    private CellStyle buildDataStyle(SXSSFWorkbook wb, ColumnMeta col, ExcelCellStyleSpec styleSpec) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        boolean bold = styleSpec.getFontBold() != null ? styleSpec.getFontBold().booleanValue() : col.isFontBold();
        int fontSize = styleSpec.getFontSize() != null ? styleSpec.getFontSize().intValue() : col.getFontSize();
        String align = styleSpec.getTextAlign() != null ? styleSpec.getTextAlign() : col.getTextAlign();
        String borderStyle = styleSpec.getBorderStyle() != null ? styleSpec.getBorderStyle() : col.getBorderStyle();
        String backgroundColor = styleSpec.getBackgroundColor() != null ? styleSpec.getBackgroundColor() : col.getBgColor();
        String numberFormat = styleSpec.getNumberFormat() != null ? styleSpec.getNumberFormat() : col.getNumberFormat();

        font.setBold(bold);
        font.setFontHeightInPoints((short) (fontSize > 0 ? fontSize : 10));
        style.setFont(font);
        style.setAlignment(toHAlign(align));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, borderStyle);
        if (backgroundColor != null) {
            applyBgColor(style, backgroundColor);
        }
        // 숫자 컬럼은 메타 또는 정책에서 지정한 number format을 적용한다.
        if ("number".equals(col.getColType()) && numberFormat != null) {
            DataFormat dataFormat = wb.createDataFormat();
            style.setDataFormat(dataFormat.getFormat(numberFormat));
        }
        return style;
    }

    private HorizontalAlignment toHAlign(String align) {
        if (align == null) {
            return HorizontalAlignment.LEFT;
        }
        String lower = align.toLowerCase();
        if ("center".equals(lower)) {
            return HorizontalAlignment.CENTER;
        }
        if ("right".equals(lower)) {
            return HorizontalAlignment.RIGHT;
        }
        return HorizontalAlignment.LEFT;
    }

    private void applyBorder(CellStyle style, String borderStyle) {
        BorderStyle bs = BorderStyle.THIN;
        if ("medium".equals(borderStyle)) {
            bs = BorderStyle.MEDIUM;
        } else if ("thick".equals(borderStyle)) {
            bs = BorderStyle.THICK;
        } else if ("none".equals(borderStyle)) {
            bs = BorderStyle.NONE;
        }
        style.setBorderTop(bs);
        style.setBorderBottom(bs);
        style.setBorderLeft(bs);
        style.setBorderRight(bs);
    }

    private void applyBgColor(CellStyle style, String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#")) {
            return;
        }
        if (!(style instanceof XSSFCellStyle)) {
            return;
        }
        try {
            Color c = Color.decode(hexColor);
            XSSFColor xssfColor = new XSSFColor(c, null);
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            xssfStyle.setFillForegroundColor(xssfColor);
            xssfStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } catch (NumberFormatException ignored) {
        }
    }

    private void setColumnWidths(Sheet sheet, List<ColumnMeta> columns) {
        for (int i = 0; i < columns.size(); i++) {
            int width = columns.get(i).getColWidth();
            sheet.setColumnWidth(i, width <= 0 ? 3000 : width * 36);
        }
    }
}
