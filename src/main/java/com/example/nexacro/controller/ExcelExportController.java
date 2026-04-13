package com.example.nexacro.controller;

import com.example.nexacro.excel.BandMeta;
import com.example.nexacro.excel.ColumnMeta;
import com.example.nexacro.service.ExcelExportService;
import com.nexacro.xapi.data.DataSet;
import com.nexacro.xapi.data.PlatformData;
import com.nexacro.xapi.tx.HttpPlatformRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/excel")
public class ExcelExportController {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportController.class);

    private final ExcelExportService excelExportService;

    public ExcelExportController(ExcelExportService excelExportService) {
        this.excelExportService = excelExportService;
    }

    @PostMapping("/export")
    public void exportExcel(HttpServletRequest request,
                            HttpServletResponse response) throws Exception {
        // 1. Parse PlatformData from request
        HttpPlatformRequest platformRequest = new HttpPlatformRequest(request);
        platformRequest.receiveData();
        PlatformData platformData = platformRequest.getData();

        DataSet dsGridMeta = platformData.getDataSet("ds_gridMeta");
        DataSet dsBandMeta = platformData.getDataSet("ds_bandMeta");

        if (dsGridMeta == null || dsGridMeta.getRowCount() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "ds_gridMeta is required and must have rows");
            return;
        }

        // 2. Parse DataSet -> DTO
        List<ColumnMeta> columns = parseColumnMeta(dsGridMeta);
        List<BandMeta> bands = dsBandMeta != null ? parseBandMeta(dsBandMeta) : new ArrayList<>();
        log.debug("Export: {} columns, {} bands", columns.size(), bands.size());

        // 3. Generate Excel
        byte[] excelBytes = excelExportService.exportExcel(columns, bands);

        // 4. Write binary response
        String filename = URLEncoder.encode(
            "sales_export_" + LocalDate.now() + ".xlsx",
            StandardCharsets.UTF_8);
        response.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
            "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(excelBytes.length);
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }

    private List<ColumnMeta> parseColumnMeta(DataSet ds) {
        List<ColumnMeta> list = new ArrayList<>();
        for (int r = 0; r < ds.getRowCount(); r++) {
            list.add(ColumnMeta.builder()
                .colId(ds.getString(r, "col_id"))
                .headerText(ds.getString(r, "header_text"))
                .colWidth(ds.getInt(r, "col_width"))
                .textAlign(ds.getString(r, "text_align"))
                .colType(ds.getString(r, "col_type"))
                .numberFormat(ds.getString(r, "number_format"))
                .dateFormat(ds.getString(r, "date_format"))
                .editType(ds.getString(r, "edit_type"))
                .comboGroupCd(ds.getString(r, "combo_group_cd"))
                .comboCodeCol(ds.getString(r, "combo_code_col"))
                .comboTextCol(ds.getString(r, "combo_text_col"))
                .bandId(ds.getString(r, "band_id"))
                .bgColor(ds.getString(r, "bg_color"))
                .fontBold("true".equalsIgnoreCase(ds.getString(r, "font_bold")))
                .fontSize(ds.getInt(r, "font_size"))
                .borderStyle(ds.getString(r, "border_style"))
                .build());
        }
        return list;
    }

    private List<BandMeta> parseBandMeta(DataSet ds) {
        List<BandMeta> list = new ArrayList<>();
        for (int r = 0; r < ds.getRowCount(); r++) {
            list.add(BandMeta.builder()
                .bandId(ds.getString(r, "band_id"))
                .bandText(ds.getString(r, "band_text"))
                .colSpan(ds.getInt(r, "col_span"))
                .rowSpan(ds.getInt(r, "row_span"))
                .bandOrder(ds.getInt(r, "band_order"))
                .bgColor(ds.getString(r, "bg_color"))
                .textAlign(ds.getString(r, "text_align"))
                .fontBold("true".equalsIgnoreCase(ds.getString(r, "font_bold")))
                .build());
        }
        return list;
    }
}
