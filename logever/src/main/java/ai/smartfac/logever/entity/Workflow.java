package ai.smartfac.logever.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "workflow",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<State> states;

    @OneToMany(mappedBy = "workflow",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Transition> transitions;

    @JsonBackReference
    @OneToOne(mappedBy = "workflow")
    private Form form;

    @JsonIgnore
    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "create_dt")
    private Timestamp createDt;

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @JsonIgnore
    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private Timestamp lastUpdatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<State> getStates() {
        return states;
    }

    public void setStates(Set<State> states) {
        this.states = states;
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

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Timestamp getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Timestamp lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public void addState(State state) {
        states.add(state);
        //state.setWorkflow(this);
    }

    public Set<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Set<Transition> transitions) {
        this.transitions = transitions;
    }

    public Workflow(Integer id) {
        this.id = id;
    }

    public Workflow() {
    }
}
