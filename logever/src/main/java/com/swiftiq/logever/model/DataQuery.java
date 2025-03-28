package com.swiftiq.logever.model;

import java.sql.ResultSet;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataQuery {

    private int totalRows;
    private List<Map<String, Object>> data = new ArrayList<>();

    public DataQuery(List<Map<String, Object>> data, int totalRows) {
        this.data = data;
        this.totalRows = totalRows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
