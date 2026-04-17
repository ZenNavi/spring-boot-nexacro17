package com.example.nexacro.excel;

public interface ExcelRenderPolicy {

    ExcelRenderPolicy NO_OP = new ExcelRenderPolicy() {
    };

    default Object resolveCellValue(ExcelCellContext context) {
        return context.getRawValue();
    }

    default ExcelCellStyleSpec resolveCellStyle(ExcelCellContext context) {
        return ExcelCellStyleSpec.empty();
    }
}
