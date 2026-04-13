package com.example.nexacro.service;

import com.example.nexacro.dto.CodeMst;
import com.example.nexacro.excel.*;
import com.example.nexacro.mapper.ComboMapper;
import com.example.nexacro.mapper.SalesMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExcelExportServiceTest {

    private SalesMapper salesMapper;
    private ComboMapper comboMapper;
    private ExcelBuilderTestDouble excelBuilder;
    private ExcelExportService service;

    @BeforeEach
    void setUp() {
        salesMapper = mock(SalesMapper.class);
        comboMapper = mock(ComboMapper.class);
        excelBuilder = new ExcelBuilderTestDouble();
        service = new ExcelExportService(salesMapper, comboMapper, excelBuilder);
    }

    @Test
    void exportExcel_queries_db_and_returns_bytes() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(
            ColumnMeta.builder().colId("regionCd").headerText("지역")
                .editType("combo").comboGroupCd("REGION")
                .colType("text").textAlign("left").colWidth(100)
                .fontSize(10).borderStyle("thin").build()
        );
        List<BandMeta> bands = Collections.emptyList();

        CodeMst code = new CodeMst();
        code.setGroupCd("REGION"); code.setCode("SEO"); code.setCodeNm("서울");

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("regionCd", "SEO");

        when(comboMapper.selectByGroupCds(Collections.singletonList("REGION")))
            .thenReturn(Collections.singletonList(code));
        when(salesMapper.selectAll())
            .thenReturn(Collections.singletonList(row));
        excelBuilder.setReturnValue(new byte[]{1, 2, 3});

        byte[] result = service.exportExcel(columns, bands);

        assertThat(result).isEqualTo(new byte[]{1, 2, 3});
        assertThat(excelBuilder.getLastCallColumns()).isEqualTo(columns);
        assertThat(excelBuilder.getLastCallBands()).isEqualTo(bands);
        assertThat(excelBuilder.getLastCallRows()).isEqualTo(Collections.singletonList(row));
    }

    @Test
    void exportExcel_skips_combo_query_when_no_combo_columns() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(
            ColumnMeta.builder().colId("q1Sales").headerText("매출액")
                .colType("number").numberFormat("#,##0").textAlign("right")
                .colWidth(120).fontSize(10).borderStyle("thin").build()
        );
        when(salesMapper.selectAll()).thenReturn(Collections.emptyList());
        excelBuilder.setReturnValue(new byte[]{});

        byte[] result = service.exportExcel(columns, Collections.emptyList());

        // comboMapper should NOT be called when no combo columns
        assertThat(result).isNotNull();
        verify(comboMapper, never())
            .selectByGroupCds(any());
    }

    static class ExcelBuilderTestDouble extends NexacroGridExcelBuilder {
        private byte[] returnValue = new byte[]{};
        private List<ColumnMeta> lastCallColumns;
        private List<BandMeta> lastCallBands;
        private List<Map<String, Object>> lastCallRows;
        private ComboResolver lastCallComboResolver;

        public void setReturnValue(byte[] value) {
            this.returnValue = value;
        }

        @Override
        public byte[] build(List<ColumnMeta> columns, List<BandMeta> bands,
                           List<Map<String, Object>> dataRows,
                           ComboResolver comboResolver) throws Exception {
            this.lastCallColumns = columns;
            this.lastCallBands = bands;
            this.lastCallRows = dataRows;
            this.lastCallComboResolver = comboResolver;
            return returnValue;
        }

        public List<ColumnMeta> getLastCallColumns() {
            return lastCallColumns;
        }

        public List<BandMeta> getLastCallBands() {
            return lastCallBands;
        }

        public List<Map<String, Object>> getLastCallRows() {
            return lastCallRows;
        }

        public ComboResolver getLastCallComboResolver() {
            return lastCallComboResolver;
        }
    }
}
