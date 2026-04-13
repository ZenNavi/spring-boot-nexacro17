package com.nexacro.xapi.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stub for com.nexacro.xapi.data.DataSet.
 * Replace with real nexacro-xapi17.jar when available.
 */
public class DataSet {

    private String id;
    private final List<String> columns = new ArrayList<>();
    private final List<Map<String, String>> rows = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getRowCount() { return rows.size(); }

    public void addColumn(String name, String type, int size) { columns.add(name); }
    public void addColumn(String name, String type) { columns.add(name); }

    public int addRow() {
        rows.add(new LinkedHashMap<>());
        return rows.size() - 1;
    }

    public void setColumn(int rowIdx, String colName, Object value) {
        rows.get(rowIdx).put(colName, value == null ? null : value.toString());
    }

    public String getString(int rowIdx, String colName) {
        Map<String, String> row = rows.get(rowIdx);
        return row.get(colName);
    }

    public int getInt(int rowIdx, String colName) {
        String val = getString(rowIdx, colName);
        if (val == null || val.isEmpty()) return 0;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return 0; }
    }
}
