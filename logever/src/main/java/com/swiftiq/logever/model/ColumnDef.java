package com.swiftiq.logever.model;

public class ColumnDef {

    private String columnName;
    private String type;
    private ColumnConstraints constraints;
    private String columnLabel;

    public ColumnDef(String columnName, String type, ColumnConstraints constraints, String columnLabel) {
        this.columnName = columnName;
        this.type = type;
        this.constraints = constraints;
        this.columnLabel = columnLabel;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ColumnConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(ColumnConstraints constraints) {
        this.constraints = constraints;
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public void setColumnLabel(String columnLabel) {
        this.columnLabel = columnLabel;
    }
}
