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

    public ExcelExportService(SalesMapper salesMapper, ComboMapper comboMapper,
                              NexacroGridExcelBuilder excelBuilder) {
        this.salesMapper = salesMapper;
        this.comboMapper = comboMapper;
        this.excelBuilder = excelBuilder;
    }

    public byte[] exportExcel(List<ColumnMeta> columns, List<BandMeta> bands) throws Exception {
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

        // 3. Load ALL data from SALES_DATA (no pagination)
        List<Map<String, Object>> dataRows = salesMapper.selectAll();
        log.debug("Data rows fetched: {}", dataRows.size());

        // 4. Build Excel
        return excelBuilder.build(columns, bands, dataRows, comboResolver);
    }
}
