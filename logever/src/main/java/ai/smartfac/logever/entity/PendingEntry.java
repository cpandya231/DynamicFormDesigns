package ai.smartfac.logever.entity;

import javax.persistence.*;

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

    public PendingEntry() {
    }

    public PendingEntry(Integer formId, Integer entryId, String assignedUser, Integer assignedRole, Integer assignedDepartment) {
        this.formId = formId;
        this.entryId = entryId;
        this.assignedUser = assignedUser;
        this.assignedRole = assignedRole;
        this.assignedDepartment = assignedDepartment;
    }
}
