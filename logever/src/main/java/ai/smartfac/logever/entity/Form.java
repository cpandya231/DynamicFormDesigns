package ai.smartfac.logever.entity;

import ai.smartfac.logever.model.ColumnConstraints;
import ai.smartfac.logever.model.ColumnDef;
import ai.smartfac.logever.model.FormTemplate;
import ai.smartfac.logever.model.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;

@EntityListeners(AuditingEntityListener.class)
@Entity
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String name;

    @Column(columnDefinition = "text")
    private String template;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @Column(name="create_dt")
    @CreationTimestamp
    private Timestamp createDt;

    @Column(name = "updated_by")
    @JsonIgnore
    @LastModifiedBy
    private String updatedBy;
    @JsonIgnore
    @Column(name="update_dt")
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

    public String makeCreateTableStmt() {
        Gson gson = new Gson();
        FormTemplate formTemplate = gson.fromJson(this.getTemplate(), FormTemplate.class);
        Table table = new Table();
        table.setName(this.getName());
        ArrayList<ColumnDef> columnDefs = new ArrayList<>();
        formTemplate.getComponents().get(0).getRows().forEach(row->{
            row.forEach(comps-> {
                comps.getComponents().forEach(comp -> {
                    columnDefs.add(new ColumnDef(comp.getKey(),comp.getType(),new ColumnConstraints(comp.getValidate().isRequired(),comp.getValidate().isUnique(),!comp.getDefaultValue().isBlank() || !comp.getDefaultValue().isEmpty(),"'"+comp.getDefaultValue()+"'")));
                });
            });
        });
        columnDefs.add(new ColumnDef("log_create_dt","DATETIME",new ColumnConstraints(true,false,true,"CURRENT_TIMESTAMP")));
        columnDefs.add(new ColumnDef("created_by","text",new ColumnConstraints(true,false,false,null)));
        columnDefs.add(new ColumnDef("log_update_dt","DATETIME",new ColumnConstraints(true,false,false,null)));
        columnDefs.add(new ColumnDef("updated_by","text",new ColumnConstraints(true,false,false,null)));
        table.setColumnDefs(columnDefs);
        return table.showCreateTable();
    }
}
