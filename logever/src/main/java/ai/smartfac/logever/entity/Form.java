package ai.smartfac.logever.entity;

import ai.smartfac.logever.model.*;
import ai.smartfac.logever.model.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.gson.Gson;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EntityListeners(AuditingEntityListener.class)
@Entity
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String name;

    private String type;

    @Column(columnDefinition = "text")
    private String template;

    private int version;

    @Column(columnDefinition = "text")
    private String columns;

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "workflow_id", referencedColumnName = "id")
    private Workflow workflow;

//    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_id")
    private App app;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "create_dt")
    @CreationTimestamp
    private Timestamp createDt;

    @Column(name = "updated_by")
    @JsonIgnore
    @LastModifiedBy
    private String updatedBy;
    @JsonIgnore
    @Column(name = "update_dt")
    @UpdateTimestamp
    private Timestamp updateDt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Timestamp createDt) {
        this.createDt = createDt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Timestamp getUpdateDt() {
        return updateDt;
    }

    public void setUpdateDt(Timestamp updateDt) {
        this.updateDt = updateDt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private ArrayList<ColumnDef> parseFormTemplate() {
        Gson gson = new Gson();
        FormTemplate formTemplate = gson.fromJson(this.getTemplate(), FormTemplate.class);
        ArrayList<ColumnDef> columnDefs = new ArrayList<>();
        formTemplate.getControls().stream().forEach(controls-> {
            controls.stream().forEach(control-> {
                if(!control.getType().equalsIgnoreCase("grid"))
                    columnDefs.add(new ColumnDef(control.getKey(), control.getType(), new ColumnConstraints(control.isRequired(), false, false, null)));
            });
        });

        return columnDefs;
    }

    public ArrayList<String> getFormLabels() {
        Gson gson = new Gson();
        FormTemplate formTemplate = gson.fromJson(this.getTemplate(), FormTemplate.class);
        ArrayList<String> labels = new ArrayList<>();
        formTemplate.getControls().stream().forEach(controls-> {
            controls.stream().forEach(control-> {
                labels.add(control.getLabel());
            });
        });
        return labels;
    }
    public String makeCreateTableStmt() {
        Table table = new Table();

        table.setName(this.getName());

        ArrayList<ColumnDef> columnDefs = parseFormTemplate();

        columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT")));
        columnDefs.add(new ColumnDef("state", "VARCHAR2(50)", new ColumnConstraints(true, false, false, null)));
        columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP")));
        columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null)));
        columnDefs.add(new ColumnDef("log_update_dt", "DATETIME", new ColumnConstraints(false, false, true, "NULL ON UPDATE CURRENT_TIMESTAMP")));
        columnDefs.add(new ColumnDef("updated_by", "text", new ColumnConstraints(false, false, false, null)));
        table.setColumnDefs(columnDefs);
        System.out.println(table.showCreateTable());
        return table.showCreateTable();
    }

    public String makeCreateMasterTableStmt() {
        Table table = new Table();

        table.setName(getMasterTableName());

        ArrayList<ColumnDef> columnDefs = parseFormTemplate();

        columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT")));
        columnDefs.add(new ColumnDef("state", "VARCHAR2(50)", new ColumnConstraints(true, false, false, null)));
        columnDefs.add(new ColumnDef("entry_state", "VARCHAR2(50)", new ColumnConstraints(false, false, false, null)));
        columnDefs.add(new ColumnDef("metadata", "text", new ColumnConstraints(false, false, false, null)));
        columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP")));
        columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null)));
        columnDefs.add(new ColumnDef("log_update_dt", "DATETIME", new ColumnConstraints(false, false, true, "NULL ON UPDATE CURRENT_TIMESTAMP")));
        columnDefs.add(new ColumnDef("updated_by", "text", new ColumnConstraints(false, false, false, null)));
        table.setColumnDefs(columnDefs);
        return table.showCreateTable();
    }

    public ArrayList<String> makeCreateGridTableStmt() {
        List<ArrayList<Control>> gridControls = getGrids();
        ArrayList<String> createStmts = new ArrayList<>();
        if(gridControls.size() > 0) {
            gridControls.forEach(controls-> {
                Control gridControl = controls.get(0);
                Table table = new Table();
                table.setGridTableName(this.getName() +" "+ gridControl.getLabel());

                ArrayList<ColumnDef> columnDefs = new ArrayList<>();
                gridControl.getControls().stream().forEach(control-> {
                    columnDefs.add(new ColumnDef(control.getKey(), control.getType(), new ColumnConstraints(control.isRequired(), false, false, null)));
                });

                columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT")));
                columnDefs.add(new ColumnDef("log_entry_id", "INT", new ColumnConstraints(true, false, false, null)));
                columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP")));
                columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null)));
                columnDefs.add(new ColumnDef("log_update_dt", "DATETIME", new ColumnConstraints(false, false, true, "NULL ON UPDATE CURRENT_TIMESTAMP")));
                columnDefs.add(new ColumnDef("updated_by", "text", new ColumnConstraints(false, false, false, null)));
                table.setColumnDefs(columnDefs);
                createStmts.add(table.showCreateTable());
            });
        }
        return createStmts;
    }

    public ArrayList<String> makeCreateGridHistoryTableStmt() {
        List<ArrayList<Control>> gridControls = getGrids();
        ArrayList<String> createStmts = new ArrayList<>();
        if(gridControls.size() > 0) {
            gridControls.forEach(controls-> {
                Control gridControl = controls.get(0);
                Table table = new Table();
                table.setGridAuditTableName(this.getName() +" "+ gridControl.getLabel());

                ArrayList<ColumnDef> columnDefs = new ArrayList<>();
                gridControl.getControls().stream().forEach(control-> {
                    columnDefs.add(new ColumnDef(control.getKey(), control.getType(), new ColumnConstraints(control.isRequired(), false, false, null)));
                });

                columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT")));
                columnDefs.add(new ColumnDef("grid_entry_id", "INT", new ColumnConstraints(true, false, false, null)));
                columnDefs.add(new ColumnDef("history_log_entry_id", "INT", new ColumnConstraints(true, false, false, null)));
                columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP")));
                columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null)));
                columnDefs.add(new ColumnDef("comment", "LONGTEXT", new ColumnConstraints(false, false, false, null)));
                columnDefs.forEach(cd->cd.setConstraints(new ColumnConstraints(cd.getConstraints().isRequired(),false,cd.getConstraints().isDefaults(),cd.getConstraints().getDefaultValue())));
                table.setColumnDefs(columnDefs);
                createStmts.add(table.showCreateTable());
            });
        }
        return createStmts;
    }

    public String makeCreateMetaDataTableStmt() {
        Table table = new Table();
        table.setName(getMetadataTableName());

        ArrayList<ColumnDef> columnDefs = parseFormTemplate();

        columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT")));
        columnDefs.add(new ColumnDef("log_entry_id", "INT", new ColumnConstraints(true, false, false, null)));
        columnDefs.add(new ColumnDef("state", "VARCHAR2(50)", new ColumnConstraints(false, false, false, null)));
        columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP")));
        columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null)));
        columnDefs.add(new ColumnDef("comment", "LONGTEXT", new ColumnConstraints(false, false, false, null)));
        columnDefs.forEach(cd->cd.setConstraints(new ColumnConstraints(cd.getConstraints().isRequired(),false,cd.getConstraints().isDefaults(),cd.getConstraints().getDefaultValue())));
        table.setColumnDefs(columnDefs);
        return table.showCreateTable();
    }

    public String getMetadataTableName() {
        return this.getName() + "_history";
    }


    public String makeAlterTableStmt(String prevColumns) {
        return makeAlterStatement(prevColumns, this.getName());
    }

    public String makeAlterMasterTableStmt(String prevColumns) {
        return makeAlterStatement(prevColumns, this.getMasterTableName());
    }

    private String makeAlterStatement(String prevColumns, String tableName) {
        ArrayList<String> prevColList = new ArrayList<>(Arrays.asList(prevColumns.split(",")));
        ArrayList<String> newColList = new ArrayList<>(Arrays.asList(this.getColumns().split(",")));

        List<String> addCols = newColList.stream().filter(col -> !prevColList.contains(col)).collect(Collectors.toList());
        List<String> removeCols = prevColList.stream().filter(col -> !newColList.contains(col)).collect(Collectors.toList());

        prevColList.removeAll(removeCols);
        prevColList.addAll(addCols);

        ArrayList<ColumnDef> newColumns = parseFormTemplate();
        List<ColumnDef> columnsToBeAdded = newColumns.stream().filter(col -> addCols.contains(col.getColumnName())).collect(Collectors.toList());

        this.setColumns(String.join(",", prevColList));

        if (addCols.size() > 0) {
            Table table = new Table();
            table.setName(tableName);
            ArrayList<ColumnDef> alteredColumns = new ArrayList<>();
            alteredColumns.addAll(columnsToBeAdded);
            table.setAlteredColumnDefs(alteredColumns);

            this.setColumns(String.join(",", prevColList));
            return table.showAlterTable();
        }
        return "";
    }

    public String makeAlterTableMetaDataStmt(String prevColumns) {
        return makeAlterStatement(prevColumns, this.getMetadataTableName());
    }

    public String makeInsertValuesStmt(Map<String, String> values) {
        Table table = new Table();
        table.setName(this.getName());

        return table.buildInsertStatement(this.getColumns(), values);
    }

    public String makeGridInsertValuesStmt(String name,Map<String, String> values) {
        Table table = new Table();
        table.setGridTableName(this.getName()+" "+name);
        String columns = getGrids().stream().filter(controls->controls.get(0).getKey().equalsIgnoreCase(name)).findFirst().get()
                        .get(0).getControls().stream().map(ctrol->ctrol.getKey()).collect(Collectors.joining(","));
        System.out.println(table.buildGridInsertStatement(columns, values));
        return table.buildGridInsertStatement(columns, values);
    }

    public String makeGridDeleteValuesStmt(String name,int id) {
        Table table = new Table();
        table.setGridTableName(this.getName()+" "+name);
        System.out.println(table.buildGridDeleteStatement(id));
        return table.buildGridDeleteStatement(id);
    }

    public String makeInsertMetadataValuesStmt(Map<String, String> values) {
        Table table = new Table();
        table.setName(this.getMetadataTableName());

        return table.buildInsertMetadataStatement(this.getColumns(), values);
    }
    public String makeGridInsertMetadataValuesStmt(String name,Map<String, String> values) {
        Table table = new Table();
        table.setGridAuditTableName(this.getName()+" "+name);
        String columns = getGrids().stream().filter(controls->controls.get(0).getKey().equalsIgnoreCase(name)).findFirst().get()
                .get(0).getControls().stream().map(ctrol->ctrol.getKey()).collect(Collectors.joining(","));
        return table.buildGridInsertMetadataStatement(columns, values);
    }


    public String makeInsertMasterValuesStmt(Map<String, String> values) {
        Table table = new Table();
        table.setName(this.getMasterTableName());

        return table.buildInsertUpdateMasterStatement("id," + this.getColumns(), values);
    }

    public String makeUpdateStmt(Map<String, String> values) {
        Table table = new Table();
        table.setName(this.getName());

        return table.buildUpdateStatement(this.getColumns(), values);
    }

    public String makeGridUpdateStatement(String name, Map<String,String> values) {
        Table table = new Table();
        table.setGridTableName(this.getName()+" "+name);
        String columns = getGrids().stream().filter(controls->controls.get(0).getKey().equalsIgnoreCase(name)).findFirst().get()
                .get(0).getControls().stream().map(ctrol->ctrol.getKey()).collect(Collectors.joining(","));
        System.out.println(table.buildGridUpdateStatement(columns,values));
        return table.buildGridUpdateStatement(columns,values);
    }

    public String makeUpdateMasterStmt(Map<String, String> values) {
        Table table = new Table();
        table.setName(getMasterTableName());

        return table.buildInsertUpdateMasterStatement("id," + this.getColumns(), values);
    }

    public String makeUpdateMasterEntryStateStmt(Map<String, String> values) {
        Table table = new Table();
        table.setName(getMasterTableName());
        String columns = String.join(",", values.keySet());
        return table.makeUpdateMasterEntryStateStmt(columns, values);
    }

    public String getMasterTableName() {
        return "mstr_" + this.getName();
    }

    public List<ArrayList<Control>> getGrids() {
        Gson gson = new Gson();
        FormTemplate formTemplate = gson.fromJson(this.getTemplate(), FormTemplate.class);
        return formTemplate.getControls().stream().filter(controls->controls.get(0).getType().equalsIgnoreCase("grid")).collect(Collectors.toList());
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }
}
