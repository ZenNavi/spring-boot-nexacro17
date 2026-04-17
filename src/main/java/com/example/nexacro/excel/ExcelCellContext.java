package com.example.nexacro.excel;

import java.util.Map;

/**
 * 셀 렌더링 시점에 정책 객체로 전달되는 컨텍스트다.
 *
 * <p>정책 구현체는 이 객체를 통해 컬럼 메타, 원본 row 데이터, raw value,
 * row/column index, combo resolver 등을 함께 참조할 수 있다.</p>
 */
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
