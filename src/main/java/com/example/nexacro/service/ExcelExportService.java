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

    public byte[] exportExcel(List<ColumnMeta> columns, List<BandMeta> bands,
                              ExcelRenderPolicy renderPolicy) throws Exception {
        ComboResolver comboResolver = buildComboResolver(columns);
        return excelBuilder.build(columns, bands, buildRowWriter(), comboResolver, renderPolicy);
    }

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
        // 1. Extract combo groupCd list (distinct)
        List<String> comboGroupCds = columns.stream()
            .filter(c -> "combo".equals(c.getEditType()) && c.getComboGroupCd() != null)
            .map(ColumnMeta::getComboGroupCd)
            .distinct()
            .collect(Collectors.toList());

        // 2. Load combo data from CODE_MST → build ComboResolver
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
