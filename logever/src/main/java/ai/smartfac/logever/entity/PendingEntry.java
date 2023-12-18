package ai.smartfac.logever.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class PendingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer formId;

    private Integer entryId;

    private String assignedUser;

    private Integer assignedRole;

    private Integer assignedDepartment;

    private String entryCreatedBy;

    private String pendingHod;

    @Column(name = "create_dt")
    @CreationTimestamp
    private Timestamp createDt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFormId() {
        return formId;
    }

    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    public Integer getAssignedRole() {
        return assignedRole;
    }

    public void setAssignedRole(Integer assignedRole) {
        this.assignedRole = assignedRole;
    }

    public Integer getAssignedDepartment() {
        return assignedDepartment;
    }

    public void setAssignedDepartment(Integer assignedDepartment) {
        this.assignedDepartment = assignedDepartment;
    }

    public Integer getEntryId() {
        return entryId;
    }

    public void setEntryId(Integer entryId) {
        this.entryId = entryId;
    }

    public String getEntryCreatedBy() {
        return entryCreatedBy;
    }

    public void setEntryCreatedBy(String entryCreatedBy) {
        this.entryCreatedBy = entryCreatedBy;
    }

    public Timestamp getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Timestamp createDt) {
        this.createDt = createDt;
    }

    public String getPendingHod() {
        return pendingHod;
    }

    public void setPendingHod(String pendingHod) {
        this.pendingHod = pendingHod;
    }

    public PendingEntry() {
    }

    public PendingEntry(Integer formId, Integer entryId, String assignedUser, Integer assignedRole, Integer assignedDepartment,
                        String entryCreatedBy, String pendingHod) {
        this.formId = formId;
        this.entryId = entryId;
        this.assignedUser = assignedUser;
        this.assignedRole = assignedRole;
        this.assignedDepartment = assignedDepartment;
        this.entryCreatedBy = entryCreatedBy;
        this.pendingHod = pendingHod;
    }
}
