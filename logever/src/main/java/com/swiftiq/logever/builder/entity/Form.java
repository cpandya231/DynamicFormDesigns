package com.swiftiq.logever.builder.entity;

import com.swiftiq.logever.model.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.gson.Gson;
import com.swiftiq.logever.model.Table;
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
@Entity(name = "form")
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String name;

    private String type;

    @Column(columnDefinition = "text")
    private String template;

    @Column(columnDefinition = "text")
    private String settings;

    private int version;

    @Column(columnDefinition = "text")
    private String columns;

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "workflow_id", referencedColumnName = "id")
    private Workflow workflow;

    @Column(columnDefinition = "INT NOT NULL")
    private Integer appId;

    private String status;

    private String qaApprover;
    private String qaApprovalStatus;
    private Timestamp qaApprovalDt;
    @Column(columnDefinition = "text")
    private String qaApproverComments;

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

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

//    public String getQaReviewer() {
//        return qaReviewer;
//    }
//
//    public void setQaReviewer(String qaReviewer) {
//        this.qaReviewer = qaReviewer;
//    }
//
//    public String getQaReviewStatus() {
//        return qaReviewStatus;
//    }
//
//    public void setQaReviewStatus(String qaReviewStatus) {
//        this.qaReviewStatus = qaReviewStatus;
//    }
//
//    public Timestamp getQaReviewDt() {
//        return qaReviewDt;
//    }
//
//    public void setQaReviewDt(Timestamp qaReviewDt) {
//        this.qaReviewDt = qaReviewDt;
//    }
//
//    public String getQaReviewerComments() {
//        return qaReviewerComments;
//    }
//
//    public void setQaReviewerComments(String qaReviewerComments) {
//        this.qaReviewerComments = qaReviewerComments;
//    }

    public String getQaApprover() {
        return qaApprover;
    }

    public void setQaApprover(String qaApprover) {
        this.qaApprover = qaApprover;
    }

    public String getQaApprovalStatus() {
        return qaApprovalStatus;
    }

    public void setQaApprovalStatus(String qaApprovalStatus) {
        this.qaApprovalStatus = qaApprovalStatus;
    }

    public Timestamp getQaApprovalDt() {
        return qaApprovalDt;
    }

    public void setQaApprovalDt(Timestamp qaApprovalDt) {
        this.qaApprovalDt = qaApprovalDt;
    }

    public String getQaApproverComments() {
        return qaApproverComments;
    }

    public void setQaApproverComments(String qaApproverComments) {
        this.qaApproverComments = qaApproverComments;
    }

    public ArrayList<ColumnDef> parseFormTemplate() {
        Gson gson = new Gson();
        FormTemplate formTemplate = gson.fromJson(this.getTemplate(), FormTemplate.class);
        ArrayList<ColumnDef> columnDefs = new ArrayList<>();
        formTemplate.getControls().stream().forEach(controls-> {
            controls.stream().forEach(control-> {
                if(!control.getType().equalsIgnoreCase("grid") && !control.getType().equalsIgnoreCase("section-heading"))
                    columnDefs.add(new ColumnDef(control.getKey(), control.getType(), new ColumnConstraints(control.isRequired(), false, false, null), control.getLabel()));
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

    public ArrayList<String> getFormKeys() {
        Gson gson = new Gson();
        FormTemplate formTemplate = gson.fromJson(this.getTemplate(), FormTemplate.class);
        ArrayList<String> keys = new ArrayList<>();
        formTemplate.getControls().stream().forEach(controls-> {
            controls.stream().forEach(control-> {
                keys.add(control.getKey());
            });
        });
        return keys;
    }

    public String makeCreateTableStmt(boolean isMaster) {
        Table table = new Table();

        table.setName(this.getName());

        ArrayList<ColumnDef> columnDefs = parseFormTemplate();

        columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT"), "ID"));
        columnDefs.add(new ColumnDef("state", "VARCHAR2(50)", new ColumnConstraints(true, false, false, null), "State"));
        if(isMaster)
            columnDefs.add(new ColumnDef("is_invalid", "INT", new ColumnConstraints(true, false, true, "0"), "Is Invalid?"));

        columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP"), "Log Create Date"));
        columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null), "Created By"));
        columnDefs.add(new ColumnDef("log_update_dt", "DATETIME", new ColumnConstraints(false, false, true, "NULL ON UPDATE CURRENT_TIMESTAMP"), "Log Update Date"));
        columnDefs.add(new ColumnDef("updated_by", "text", new ColumnConstraints(false, false, false, null), "Updated By"));
        table.setColumnDefs(columnDefs);
        System.out.println(table.showCreateTable());
        return table.showCreateTable();
    }

    public String makeCreateMasterTableStmt() {
        Table table = new Table();

        table.setName(getMasterTableName());

        ArrayList<ColumnDef> columnDefs = parseFormTemplate();

        columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT"), "ID"));
        columnDefs.add(new ColumnDef("state", "VARCHAR2(50)", new ColumnConstraints(true, false, false, null), "State"));
        columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP"), "Log Create Date"));
        columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null), "Created By"));
        columnDefs.add(new ColumnDef("log_update_dt", "DATETIME", new ColumnConstraints(false, false, true, "NULL ON UPDATE CURRENT_TIMESTAMP"), "Log Update Date"));
        columnDefs.add(new ColumnDef("updated_by", "text", new ColumnConstraints(false, false, false, null), "Updated By"));
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
                table.setGridTableName(this.getName() +"_"+ gridControl.getKey());

                ArrayList<ColumnDef> columnDefs = new ArrayList<>();
                gridControl.getControls().stream().forEach(control-> {
                    columnDefs.add(new ColumnDef(control.getKey(), control.getType(), new ColumnConstraints(control.isRequired(), false, false, null), control.getLabel()));
                });

                columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT"), "ID"));
                columnDefs.add(new ColumnDef("log_entry_id", "INT", new ColumnConstraints(true, false, false, null), "Log Entry ID"));
                columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP"), "Log Create Date"));
                columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null), "Created By"));
                columnDefs.add(new ColumnDef("log_update_dt", "DATETIME", new ColumnConstraints(false, false, true, "NULL ON UPDATE CURRENT_TIMESTAMP"), "Log Update Date"));
                columnDefs.add(new ColumnDef("updated_by", "text", new ColumnConstraints(false, false, false, null), "Updated By"));
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
                table.setGridAuditTableName(this.getName() +"_"+ gridControl.getKey());

                ArrayList<ColumnDef> columnDefs = new ArrayList<>();
                gridControl.getControls().stream().forEach(control-> {
                    columnDefs.add(new ColumnDef(control.getKey(), control.getType(), new ColumnConstraints(control.isRequired(), false, false, null), control.getLabel()));
                });

                columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT"), "ID"));
                columnDefs.add(new ColumnDef("grid_entry_id", "INT", new ColumnConstraints(true, false, false, null), "Grid Entry ID"));
                columnDefs.add(new ColumnDef("history_log_entry_id", "INT", new ColumnConstraints(true, false, false, null), "History Log Entry ID"));
                columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP"), "Log Create Date"));
                columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null), "Created By"));
                columnDefs.add(new ColumnDef("comment", "LONGTEXT", new ColumnConstraints(false, false, false, null), "Comment"));
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

        columnDefs.add(new ColumnDef("id", "INT", new ColumnConstraints(true, false, true, "AUTO_INCREMENT"), "ID"));
        columnDefs.add(new ColumnDef("log_entry_id", "INT", new ColumnConstraints(true, false, false, null), "Log Entry ID"));
        columnDefs.add(new ColumnDef("state", "VARCHAR2(50)", new ColumnConstraints(false, false, false, null), "State"));
        columnDefs.add(new ColumnDef("log_create_dt", "DATETIME", new ColumnConstraints(true, false, true, "CURRENT_TIMESTAMP"), "Log Create Date"));
        columnDefs.add(new ColumnDef("created_by", "text", new ColumnConstraints(true, false, false, null), "Created By"));
        columnDefs.add(new ColumnDef("comment", "LONGTEXT", new ColumnConstraints(false, false, false, null), "Comment"));
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

    public String makeDeleteMasterDataStmt(Map<String,String> values) {
        Table table = new Table();
        table.setName(this.getMasterTableName());

        return table.buildDeleteMasterStatement(this.getColumns(),values);
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

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String makeUpdateMasterEntryStateStmtWithId(Map<String, String> values, String idCol) {
        Table table = new Table();
        table.setName(getMasterTableName());
        String columns = String.join(",", values.keySet());
        return table.makeUpdateMasterEntryStateStmtWithId(columns, values, idCol);
    }

    public String renameMetaDataTableStmt(String version) {
        Table table = new Table();
        table.setName(getMetadataTableName());

        return "ALTER TABLE "+table.getName()+" rename "+table.getName()+"_"+version;
    }

    public ArrayList<String> renameGridHistoryTableStmt(String version) {
        List<ArrayList<Control>> gridControls = getGrids();
        ArrayList<String> renameStmts = new ArrayList<>();
        if(gridControls.size() > 0) {
            gridControls.forEach(controls-> {
                Control gridControl = controls.get(0);
                Table table = new Table();
                table.setGridAuditTableName(this.getName() +" "+ gridControl.getKey());
                renameStmts.add("ALTER TABLE "+table.getName()+" rename "+table.getName()+"_"+version);
            });
        }
        return renameStmts;
    }

    public ArrayList<String> renameGridTableStmt(String version) {
        List<ArrayList<Control>> gridControls = getGrids();
        ArrayList<String> renameStmts = new ArrayList<>();
        if(gridControls.size() > 0) {
            gridControls.forEach(controls-> {
                Control gridControl = controls.get(0);
                Table table = new Table();
                table.setGridTableName(this.getName() +" "+ gridControl.getKey());
                renameStmts.add("ALTER TABLE "+table.getName()+" rename "+table.getName()+"_"+version);
            });
        }
        return renameStmts;
    }

    public String renameTableStmt(String version) {
        Table table = new Table();
        table.setName(this.getName());
        return "ALTER TABLE "+table.getName()+" rename "+table.getName()+"_"+version;
    }

    public String renameMasterTableStmt(String version) {
        Table table = new Table();
        table.setName(getMasterTableName());
        return "ALTER TABLE "+table.getName()+" rename "+table.getName()+"_"+version;
    }
}
