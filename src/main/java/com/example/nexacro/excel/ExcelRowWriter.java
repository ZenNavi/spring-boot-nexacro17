package com.example.nexacro.excel;

import java.util.List;
import java.util.Map;

public interface ExcelRowWriter {

    void writeRows(ExcelRowConsumer consumer) throws Exception;

    static ExcelRowWriter fromList(final List<Map<String, Object>> rows) {
        return new ExcelRowWriter() {
            @Override
            public void writeRows(ExcelRowConsumer consumer) throws Exception {
                for (Map<String, Object> row : rows) {
                    consumer.accept(row);
                }
            }
        };
    }
}
