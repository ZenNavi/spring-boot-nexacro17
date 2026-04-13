package com.nexacro.xapi.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stub for com.nexacro.xapi.data.PlatformData.
 * Replace with real nexacro-xapi17.jar when available.
 */
public class PlatformData {

    private final Map<String, DataSet> dataSets = new LinkedHashMap<>();

    public void addDataSet(DataSet ds) {
        dataSets.put(ds.getId(), ds);
    }

    public DataSet getDataSet(String id) {
        return dataSets.get(id);
    }

    public int getDataSetCount() { return dataSets.size(); }
}
