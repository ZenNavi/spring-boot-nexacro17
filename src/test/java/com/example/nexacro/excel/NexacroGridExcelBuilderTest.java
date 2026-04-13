package com.example.nexacro.excel;

import com.example.nexacro.dto.CodeMst;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class NexacroGridExcelBuilderTest {

    private final NexacroGridExcelBuilder builder = new NexacroGridExcelBuilder();

    @Test
    void single_header_row_when_no_bands() throws Exception {
        List<ColumnMeta> columns = Arrays.asList(
            col("name", "이름", "left"),
            col("age",  "나이", "center")
        );
        byte[] bytes = builder.build(columns, Collections.emptyList(),
                                     Collections.emptyList(),
                                     new ComboResolver(Collections.emptyList()));

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = wb.getSheetAt(0);
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("이름");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("나이");
        wb.close();
    }

    @Test
    void two_header_rows_when_bands_present() throws Exception {
        List<BandMeta> bands = Collections.singletonList(
            BandMeta.builder().bandId("B1").bandText("1분기")
                    .colSpan(2).rowSpan(1).bandOrder(1)
                    .bgColor("#D9E1F2").textAlign("center").fontBold(true).build()
        );
        List<ColumnMeta> columns = Arrays.asList(
            colBand("q1Sales", "매출액", "right", "B1"),
            colBand("q1Qty",   "수량",   "center", "B1")
        );
        byte[] bytes = builder.build(columns, bands,
                                     Collections.emptyList(),
                                     new ComboResolver(Collections.emptyList()));

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = wb.getSheetAt(0);
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("1분기");
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("매출액");
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("수량");
        wb.close();
    }

    @Test
    void combo_text_resolved_in_data_rows() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(colCombo("regionCd", "지역", "REGION"));
        CodeMst code = new CodeMst();
        code.setGroupCd("REGION"); code.setCode("SEO"); code.setCodeNm("서울");
        byte[] bytes = builder.build(columns, Collections.emptyList(),
                Collections.singletonList(row("regionCd", "SEO")),
                new ComboResolver(Collections.singletonList(code)));

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        assertThat(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()).isEqualTo("서울");
        wb.close();
    }

    @Test
    void number_column_uses_numeric_cell_type() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(colNumber("q1Sales", "매출액"));
        byte[] bytes = builder.build(columns, Collections.emptyList(),
                Collections.singletonList(row("q1Sales", 12000000L)),
                new ComboResolver(Collections.emptyList()));

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Cell cell = wb.getSheetAt(0).getRow(1).getCell(0);
        assertThat(cell.getCellType()).isEqualTo(CellType.NUMERIC);
        assertThat(cell.getNumericCellValue()).isEqualTo(12000000.0);
        wb.close();
    }

    @Test
    void mixed_columns_no_band_and_with_band() throws Exception {
        // 지역(no band, rowspan 2), 1분기 매출(band B1), 1분기 수량(band B1), 합계(no band, rowspan 2)
        List<BandMeta> bands = Collections.singletonList(
            BandMeta.builder().bandId("B1").bandText("1분기")
                    .colSpan(2).rowSpan(1).bandOrder(1).bgColor("#D9E1F2")
                    .textAlign("center").fontBold(true).build()
        );
        List<ColumnMeta> columns = Arrays.asList(
            col("regionCd", "지역", "left"),           // no band → rowspan 2
            colBand("q1Sales", "매출액", "right", "B1"),
            colBand("q1Qty", "수량", "center", "B1"),
            col("totalSales", "합계", "right")          // no band → rowspan 2
        );
        byte[] bytes = builder.build(columns, bands, Collections.emptyList(),
                                     new ComboResolver(Collections.emptyList()));

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = wb.getSheetAt(0);
        // Row 0: 지역(merged 2 rows), 1분기(col 1-2 merged), 합계(merged 2 rows)
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("지역");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("1분기");
        // Row 1: col 1=매출액, col 2=수량
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("매출액");
        assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("수량");
        wb.close();
    }

    // Helpers
    private ColumnMeta col(String id, String header, String align) {
        return ColumnMeta.builder().colId(id).headerText(header)
                .textAlign(align).colType("text").colWidth(100)
                .fontSize(10).borderStyle("thin").build();
    }
    private ColumnMeta colBand(String id, String header, String align, String bandId) {
        return ColumnMeta.builder().colId(id).headerText(header)
                .textAlign(align).colType("text").colWidth(100)
                .bandId(bandId).fontSize(10).borderStyle("thin").build();
    }
    private ColumnMeta colCombo(String id, String header, String groupCd) {
        return ColumnMeta.builder().colId(id).headerText(header)
                .textAlign("left").colType("text").editType("combo")
                .comboGroupCd(groupCd).colWidth(100).fontSize(10).borderStyle("thin").build();
    }
    private ColumnMeta colNumber(String id, String header) {
        return ColumnMeta.builder().colId(id).headerText(header)
                .textAlign("right").colType("number").numberFormat("#,##0")
                .colWidth(120).fontSize(10).borderStyle("thin").build();
    }
    private BandMeta band(String id, String text, int colSpan) {
        return BandMeta.builder().bandId(id).bandText(text)
                .colSpan(colSpan).rowSpan(1).bandOrder(1)
                .bgColor("#D9E1F2").textAlign("center").fontBold(true).build();
    }
    private Map<String, Object> row(String key, Object val) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put(key, val); return m;
    }
}
