package ai.smartfac.logever.model;

import java.util.*;
import java.util.stream.Collectors;

public class Table {

    private String name;
    private ArrayList<ColumnDef> columnDefs;
    public static Map<String, String> typeToSqlMapping;
    private ArrayList<ColumnDef> alteredColumnDefs;
    static {
        typeToSqlMapping = new HashMap<>();
        typeToSqlMapping.put("text", "VARCHAR(255)");
        typeToSqlMapping.put("DATETIME", "DATETIME");
        typeToSqlMapping.put("INT","INT");
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = "f_"+String.join("_",name.trim().toLowerCase().split(" "))+"_lgs";
    }

    public ArrayList<ColumnDef> getColumnDefs() {
        return columnDefs;
    }

    public void setColumnDefs(ArrayList<ColumnDef> columnDefs) {
        this.columnDefs = columnDefs;
    }

    public ArrayList<ColumnDef> getAlteredColumnDefs() {
        return alteredColumnDefs;
    }

    public void setAlteredColumnDefs(ArrayList<ColumnDef> alteredColumnDefs) {
        this.alteredColumnDefs = alteredColumnDefs;
    }

    public String getDefaultColumns() {
        return "state,log_create_dt,created_by,log_update_dt,updated_by";
    }

    private String auditTableFor(Table table) {
        return table.getName() + "_audit";
    }

    public String showCreateTable() {
        String createStmt = "CREATE TABLE "+this.getName()+" (\n";
        List<String> defs = columnDefs.stream().map(columnDef -> {
            return "\t" + columnDef.getColumnName()+" "+typeToSqlMapping.getOrDefault(columnDef.getType(),"VARCHAR(255)")+" " + columnDef.getConstraints().toString();
        }).collect(Collectors.toList());
        System.out.println(createStmt + String.join(",",defs) + ")");
        return createStmt + String.join(",",defs) + ", PRIMARY KEY(id))";
    }

    public String showCreateAuditTable() {
        String createStmt = "CREATE TABLE "+this.auditTableFor(this)+" (\n";
        List<String> defs = columnDefs.stream().map(columnDef -> {
            return "\t" + columnDef.getColumnName()+" "+typeToSqlMapping.getOrDefault(columnDef.getType(),"VARCHAR(255)")+" " + columnDef.getConstraints().toString();
        }).collect(Collectors.toList());
        System.out.println(createStmt + String.join(",",defs) + ")");
        return createStmt + String.join(",",defs) + ", PRIMARY KEY(id)))";
    }

    public String showAlterTable() {
        String alterStmt = "ALTER TABLE "+this.getName()+"\n";
        List<String> defs = alteredColumnDefs.stream().map(columnDef -> {
            return "\t ADD " + columnDef.getColumnName()+" "+typeToSqlMapping.getOrDefault(columnDef.getType(),"VARCHAR(255)")+" " + columnDef.getConstraints().toString();
        }).collect(Collectors.toList());
        System.out.println(alterStmt + String.join(",",defs) + ")");
        return alterStmt + String.join(",",defs);
    }

    public String showAlterAuditTable() {
        String alterStmt = "ALTER TABLE "+this.auditTableFor(this)+"\n";
        List<String> defs = alteredColumnDefs.stream().map(columnDef -> {
            return "\t ADD " + columnDef.getColumnName()+" "+typeToSqlMapping.getOrDefault(columnDef.getType(),"VARCHAR(255)")+" " + columnDef.getConstraints().toString();
        }).collect(Collectors.toList());
        System.out.println(alterStmt + String.join(",",defs) + ")");
        return alterStmt + String.join(",",defs) + ")";
    }

    public String buildInsertStatement(String columns, Map<String,String> values) {
        String insertStmt = "INSERT INTO "+this.getName()+ "(";
        List<String> filledColumns = Arrays.stream((columns+","+getDefaultColumns()).split(",")).filter(column->values.containsKey(column)).collect(Collectors.toList());

        insertStmt = insertStmt + String.join(",",filledColumns);
        insertStmt = insertStmt + ") VALUES (" +
                String.join(",",filledColumns.stream().map(col->"'"+values.get(col)+"'").collect(Collectors.toList())) +
                ")";
        return insertStmt;
    }
}
