package ai.smartfac.logever.model;

import java.util.*;
import java.util.stream.Collectors;

public class Table {

    private String name;
    private ArrayList<ColumnDef> columnDefs;
    public static Map<String, String> typeToSqlMapping;
    static {
        typeToSqlMapping = new HashMap<>();
        typeToSqlMapping.put("text", "VARCHAR(255)");
        typeToSqlMapping.put("DATETIME", "DATETIME");
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

    public String showCreateTable() {
        String createStmt = "CREATE TABLE "+this.getName()+" (\n";
        List<String> defs = columnDefs.stream().map(columnDef -> {
            return "\t" + columnDef.getColumnName()+" "+typeToSqlMapping.getOrDefault(columnDef.getType(),"VARCHAR(255)")+" " + columnDef.getConstraints().toString();
        }).collect(Collectors.toList());
        System.out.println(createStmt + String.join(",",defs) + ")");
        return createStmt + String.join(",",defs) + ")";
    }
}
