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

    public String getMetadataTableName() {
        return this.getName() + "_history";
    }

    public String makeInsertValuesStmt(Map<String, String> values) {
        Table table = new Table();
        table.setName(this.getName());

        return table.buildInsertStatement(this.getColumns(), values);
    }

    public String makeInsertMetadataValuesStmt(Map<String, String> values) {
        Table table = new Table();
        table.setName(this.getMetadataTableName());

        return table.buildInsertMetadataStatement(this.getColumns(), values);
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
