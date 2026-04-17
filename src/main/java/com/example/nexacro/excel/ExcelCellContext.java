package com.example.nexacro.excel;

import java.util.Map;

public class ExcelCellContext {
    private final ColumnMeta columnMeta;
    private final Map<String, Object> rowData;
    private final Object rawValue;
    private final int rowIndex;
    private final int columnIndex;
    private final ComboResolver comboResolver;

    public ExcelCellContext(ColumnMeta columnMeta, Map<String, Object> rowData, Object rawValue,
                            int rowIndex, int columnIndex, ComboResolver comboResolver) {
        this.columnMeta = columnMeta;
        this.rowData = rowData;
        this.rawValue = rawValue;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.comboResolver = comboResolver;
    }

    public ColumnMeta getColumnMeta() {
        return columnMeta;
    }

    public Map<String, Object> getRowData() {
        return rowData;
    }

    public Object getRawValue() {
        return rawValue;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public ComboResolver getComboResolver() {
        return comboResolver;
    }
}
