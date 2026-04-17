package com.example.nexacro.service;

import com.example.nexacro.dto.CodeMst;
import com.example.nexacro.excel.*;
import com.example.nexacro.mapper.ComboMapper;
import com.example.nexacro.mapper.SalesMapper;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
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
    @Mock ExcelRenderPolicy defaultRenderPolicy;
    @InjectMocks ExcelExportService service;

    @Test
    void exportExcel_queries_db_with_row_handler_and_returns_bytes() throws Exception {
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
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            ResultHandler<Map<String, Object>> handler = invocation.getArgument(0);
            handler.handleResult(new SimpleResultContext<Map<String, Object>>(row));
            return null;
        }).when(salesMapper).selectAllWithHandler(any());
        when(excelBuilder.build(eq(columns), eq(bands), any(ExcelRowWriter.class),
                                any(ComboResolver.class), eq(defaultRenderPolicy)))
            .thenAnswer(invocation -> {
                ExcelRowWriter writer = invocation.getArgument(2);
                writer.writeRows(new ExcelRowConsumer() {
                    @Override
                    public void accept(Map<String, Object> rowData) {
                    }
                });
                return new byte[]{1, 2, 3};
            });

        byte[] result = service.exportExcel(columns, bands);

        assertThat(result).isEqualTo(new byte[]{1, 2, 3});
        verify(comboMapper).selectByGroupCds(Collections.singletonList("REGION"));
        verify(salesMapper).selectAllWithHandler(any());
    }

    @Test
    void exportExcel_skips_combo_query_when_no_combo_columns() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(
            ColumnMeta.builder().colId("q1Sales").headerText("매출액")
                .colType("number").numberFormat("#,##0").textAlign("right")
                .colWidth(120).fontSize(10).borderStyle("thin").build()
        );
        when(excelBuilder.build(any(), any(), any(ExcelRowWriter.class), any(), eq(defaultRenderPolicy)))
                .thenReturn(new byte[]{});

        byte[] result = service.exportExcel(columns, Collections.emptyList());

        assertThat(result).isNotNull();
        verify(comboMapper, never()).selectByGroupCds(any());
    }

    @Test
    void exportExcel_accepts_direct_list_and_custom_policy() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(
                ColumnMeta.builder().colId("regDate").headerText("등록일")
                        .colType("text").dateFormat("yyyy/MM/dd")
                        .colWidth(120).fontSize(10).borderStyle("thin").build()
        );
        List<Map<String, Object>> rows = Collections.singletonList(new LinkedHashMap<String, Object>() {{
            put("regDate", "20260227");
        }});
        ExcelRenderPolicy customPolicy = mock(ExcelRenderPolicy.class);

        when(excelBuilder.build(eq(columns), eq(Collections.<BandMeta>emptyList()), eq(rows), any(ComboResolver.class), eq(customPolicy)))
                .thenReturn(new byte[]{9});

        byte[] result = service.exportExcel(columns, Collections.<BandMeta>emptyList(), rows, customPolicy);

        assertThat(result).isEqualTo(new byte[]{9});
        verify(salesMapper, never()).selectAllWithHandler(any());
    }

    private static class SimpleResultContext<T> implements ResultContext<T> {
        private final T resultObject;

        private SimpleResultContext(T resultObject) {
            this.resultObject = resultObject;
        }

        @Override
        public T getResultObject() {
            return resultObject;
        }

        @Override
        public int getResultCount() {
            return 1;
        }

        @Override
        public boolean isStopped() {
            return false;
        }

        @Override
        public void stop() {
        }
    }
}
