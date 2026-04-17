package com.example.nexacro.service;

import com.example.nexacro.dto.CodeMst;
import com.example.nexacro.excel.*;
import com.example.nexacro.mapper.ComboMapper;
import com.example.nexacro.mapper.SalesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

    private final SalesMapper salesMapper;
    private final ComboMapper comboMapper;
    private final NexacroGridExcelBuilder excelBuilder;
    private final ExcelRenderPolicy defaultRenderPolicy;

    public ExcelExportService(SalesMapper salesMapper, ComboMapper comboMapper,
                              NexacroGridExcelBuilder excelBuilder,
                              ExcelRenderPolicy defaultRenderPolicy) {
        this.salesMapper = salesMapper;
        this.comboMapper = comboMapper;
        this.excelBuilder = excelBuilder;
        this.defaultRenderPolicy = defaultRenderPolicy;
    }

    public byte[] exportExcel(List<ColumnMeta> columns, List<BandMeta> bands) throws Exception {
        return exportExcel(columns, bands, defaultRenderPolicy);
    }

    /**
     * DB에서 row를 직접 읽어 Excel을 생성한다.
     *
     * <p>대량 데이터 처리 시 메모리 적재를 줄이기 위해 mapper의 row handler 기반
     * writer를 builder에 전달한다.</p>
     */
    public byte[] exportExcel(List<ColumnMeta> columns, List<BandMeta> bands,
                              ExcelRenderPolicy renderPolicy) throws Exception {
        ComboResolver comboResolver = buildComboResolver(columns);
        return excelBuilder.build(columns, bands, buildRowWriter(), comboResolver, renderPolicy);
    }

    /**
     * 이미 준비된 List 데이터를 바로 Excel로 변환한다.
     *
     * <p>서비스 외부에서 데이터를 조합한 경우나 테스트 코드에서 사용하기 좋다.</p>
     */
    public byte[] exportExcel(List<ColumnMeta> columns, List<BandMeta> bands,
                              List<Map<String, Object>> dataRows) throws Exception {
        return exportExcel(columns, bands, dataRows, defaultRenderPolicy);
    }

    public byte[] exportExcel(List<ColumnMeta> columns, List<BandMeta> bands,
                              List<Map<String, Object>> dataRows,
                              ExcelRenderPolicy renderPolicy) throws Exception {
        ComboResolver comboResolver = buildComboResolver(columns);
        log.debug("Data rows supplied directly: {}", dataRows.size());
        return excelBuilder.build(columns, bands, dataRows, comboResolver, renderPolicy);
    }

    private ComboResolver buildComboResolver(List<ColumnMeta> columns) {
        // combo 컬럼에서 필요한 그룹 코드만 추출해 코드 마스터를 최소 조회한다.
        List<String> comboGroupCds = columns.stream()
            .filter(c -> "combo".equals(c.getEditType()) && c.getComboGroupCd() != null)
            .map(ColumnMeta::getComboGroupCd)
            .distinct()
            .collect(Collectors.toList());

        // 조회한 코드 목록을 resolver로 감싸 builder/정책에서 재사용한다.
        List<CodeMst> codeList = comboGroupCds.isEmpty()
            ? Collections.emptyList()
            : comboMapper.selectByGroupCds(comboGroupCds);
        ComboResolver comboResolver = new ComboResolver(codeList);
        log.debug("Combo groups: {}, codes loaded: {}", comboGroupCds.size(), codeList.size());
        return comboResolver;
    }

    private ExcelRowWriter buildRowWriter() {
        return new ExcelRowWriter() {
            @Override
            public void writeRows(final ExcelRowConsumer consumer) throws Exception {
                // MyBatis ResultHandler를 통해 row를 한 건씩 builder로 전달한다.
                salesMapper.selectAllWithHandler(context -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> row = (Map<String, Object>) context.getResultObject();
                    try {
                        consumer.accept(row);
                    } catch (Exception ex) {
                        throw new IllegalStateException("Failed to consume row data", ex);
                    }
                });
            }
        };
    }
}
