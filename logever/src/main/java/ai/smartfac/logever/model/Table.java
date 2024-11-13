package ai.smartfac.logever.model;

import java.util.*;
import java.util.stream.Collectors;

public class Table {

    private String name;
    public static Map<String, String> typeToSqlMapping;
    static {
        typeToSqlMapping = new HashMap<>();
        typeToSqlMapping.put("text", "text");
        typeToSqlMapping.put("DATETIME", "DATETIME");
        typeToSqlMapping.put("datetime", "DATETIME");
        typeToSqlMapping.put("INT","INT");
        typeToSqlMapping.put("grid", "INT");
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = "f_"+String.join("_",name.trim().toLowerCase().split(" "))+"_lgs";
    }

    public String getDefaultColumns() {
        return "state,log_create_dt,created_by,log_update_dt,updated_by";
    }

    public String getDefaultMetadataColumns() {
        return "state,log_entry_id,comment,log_create_dt,created_by";
    }

    private String auditTableFor(Table table) {
        return table.getName() + "_audit";
    }

    public String buildInsertStatement(String columns, Map<String,String> values) {
        String insertStmt = "INSERT INTO "+this.getName()+ "(";
        List<String> filledColumns = Arrays.stream((columns+","+getDefaultColumns()).split(",")).filter(column->values.containsKey(column)).collect(Collectors.toList());

        insertStmt = insertStmt + String.join(",",filledColumns);
        insertStmt = insertStmt + ") VALUES (" +
                String.join(",",filledColumns.stream().map(col->"'"+values.get(col).replaceAll("'","\\\\'")+"'").collect(Collectors.toList())) +
                ")";
        return insertStmt;
    }

    public String buildInsertMetadataStatement(String columns, Map<String,String> values) {
        String insertStmt = "INSERT INTO "+this.getName()+ "(";
        List<String> filledColumns = Arrays.stream((columns+","+getDefaultMetadataColumns()).split(",")).filter(column->values.containsKey(column)).collect(Collectors.toList());

        insertStmt = insertStmt + String.join(",",filledColumns);
        insertStmt = insertStmt + ") VALUES (" +
                String.join(",",filledColumns.stream().map(col->"'"+values.get(col).replaceAll("'","\\\\'")+"'").collect(Collectors.toList())) +
                ")";
        return insertStmt;
    }

    public String buildUpdateStatement(String columns, Map<String,String> values) {
        String updateStmt = "UPDATE "+this.getName()+" SET ";
        List<String> filledColumns = Arrays.stream((columns+","+getDefaultColumns()).split(",")).filter(column->values.containsKey(column))
                .map(col->col + "="+"'"+values.get(col).replaceAll("'","\\\\'")+"'").collect(Collectors.toList());
        updateStmt = updateStmt + String.join(",",filledColumns);
        updateStmt = updateStmt + " WHERE id='"+Integer.parseInt(values.get("id"))+"'";
        System.out.println(updateStmt);
        return updateStmt;
    }

    public String buildInsertUpdateMasterStatement(String columns, Map<String,String> values) {
        String upsertStmt = "INSERT INTO "+this.getName()+"(";
        List<String> filledColumns = Arrays.stream((columns+","+getDefaultMetadataColumns()).split(",")).filter(column->values.containsKey(column)).collect(Collectors.toList());

        upsertStmt = upsertStmt + String.join(",",filledColumns);
        upsertStmt = upsertStmt + ") VALUES (" +
                String.join(",",filledColumns.stream().map(col->"'"+values.get(col).replaceAll("'","\\\\'")+"'").collect(Collectors.toList())) +
                ") ON DUPLICATE KEY UPDATE "+String.join(",",filledColumns.stream().map(col->col+"="+"'"+values.get(col).replaceAll("'","\\\\'")+"'").collect(Collectors.toList()));

        return upsertStmt;
    }

    public String buildDeleteMasterStatement(String columns, Map<String,String> values) {
        String deleteStmt = "DELETE FROM "+this.getName()+" WHERE ";
        String whereClause = Arrays.stream(columns.split(",")).filter(column->values.containsKey(column))
                .map(column->column+" = '"+values.get(column).replaceAll("'","\\\\'")+"'").collect(Collectors.joining(" AND "));

        deleteStmt = deleteStmt + whereClause;
        System.out.println(deleteStmt);
        return deleteStmt;
    }

    public String makeUpdateMasterEntryStateStmt(String columns, Map<String,String> values) {
        String updateStmt = "UPDATE "+this.getName()+" SET ";
        List<String> filledColumns = Arrays.stream((columns+","+getDefaultColumns()).split(",")).filter(values::containsKey)
                .map(col->col + "="+"'"+values.get(col).replaceAll("'","\\\\'")+"'").collect(Collectors.toList());
        updateStmt = updateStmt + String.join(",",filledColumns);
        updateStmt = updateStmt + " WHERE id='"+Integer.parseInt(values.get("id"))+"'";
        return updateStmt;
    }
}
