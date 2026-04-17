package com.example.nexacro.excel;

import java.util.Map;

public interface ExcelRowConsumer {
    void accept(Map<String, Object> rowData) throws Exception;
}
