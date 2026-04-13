package com.example.nexacro.service;

import com.example.nexacro.dto.CodeMst;
import com.example.nexacro.excel.*;
import com.example.nexacro.mapper.ComboMapper;
import com.example.nexacro.mapper.SalesMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {

    @Mock SalesMapper salesMapper;
    @Mock ComboMapper comboMapper;
    @Mock NexacroGridExcelBuilder excelBuilder;
    @InjectMocks ExcelExportService service;

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
        when(excelBuilder.build(eq(columns), eq(bands), eq(Collections.singletonList(row)),
                                any(ComboResolver.class)))
            .thenReturn(new byte[]{1, 2, 3});

        byte[] result = service.exportExcel(columns, bands);

        assertThat(result).isEqualTo(new byte[]{1, 2, 3});
        verify(comboMapper).selectByGroupCds(Collections.singletonList("REGION"));
        verify(salesMapper).selectAll();
    }

    @Test
    void exportExcel_skips_combo_query_when_no_combo_columns() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(
            ColumnMeta.builder().colId("q1Sales").headerText("매출액")
                .colType("number").numberFormat("#,##0").textAlign("right")
                .colWidth(120).fontSize(10).borderStyle("thin").build()
        );
        when(salesMapper.selectAll()).thenReturn(Collections.emptyList());
        when(excelBuilder.build(any(), any(), any(), any())).thenReturn(new byte[]{});

        byte[] result = service.exportExcel(columns, Collections.emptyList());

        assertThat(result).isNotNull();
        verify(comboMapper, never()).selectByGroupCds(any());
    }
}
