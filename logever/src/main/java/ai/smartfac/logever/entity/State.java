package ai.smartfac.logever.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "workflow_id" }) })
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    private boolean sendBackAvailable;

    @Column(columnDefinition = "text")
    private String visibleColumns;

    @Column(columnDefinition = "text")
    private String disabledColumns;

    @Column(columnDefinition = "text")
    private String mandatoryColumns;

    private boolean endState;
    private boolean firstState;

    @Column(columnDefinition = "text")
    private String stateCondition;
    private String label;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "state_role",joinColumns = @JoinColumn(name = "state_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "state_department",joinColumns = @JoinColumn(name = "state_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"))
    private Set<Department> departments = new HashSet<>();

    @Transient
    public String stateId;

    public State() {
    }

    public State(Integer id) {
        this.id = id;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSendBackAvailable() {
        return sendBackAvailable;
    }

    public void setSendBackAvailable(boolean sendBackAvailable) {
        this.sendBackAvailable = sendBackAvailable;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

    public String getVisibleColumns() {
        return visibleColumns;
    }

    public void setVisibleColumns(String visibleColumns) {
        this.visibleColumns = visibleColumns;
    }

    public String getDisabledColumns() {
        return disabledColumns;
    }

    public void setDisabledColumns(String disabledColumns) {
        this.disabledColumns = disabledColumns;
    }

    public String getMandatoryColumns() {
        return mandatoryColumns;
    }

    public void setMandatoryColumns(String mandatoryColumns) {
        this.mandatoryColumns = mandatoryColumns;
    }

    public boolean isEndState() {
        return endState;
    }

    public void setEndState(boolean endState) {
        this.endState = endState;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public boolean isFirstState() {
        return firstState;
    }

    public void setFirstState(boolean firstState) {
        this.firstState = firstState;
    }

    public String getStateCondition() {
        return stateCondition;
    }

    public void setStateCondition(String stateCondition) {
        this.stateCondition = stateCondition;
    }
}
