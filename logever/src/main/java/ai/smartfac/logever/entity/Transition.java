package ai.smartfac.logever.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class Transition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonBackReference
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "from_state_id")
    private State fromState;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "to_state_id")
    private State toState;

    private boolean sendBackTransition;

    public Transition(Integer id, Workflow workflow, State fromState, State toState, boolean sendBackTransition) {
        this.id = id;
        this.workflow = workflow;
        this.fromState = fromState;
        this.toState = toState;
        this.sendBackTransition = sendBackTransition;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public State getFromState() {
        return fromState;
    }

    public void setFromState(State fromState) {
        this.fromState = fromState;
    }

    public State getToState() {
        return toState;
    }

    public void setToState(State toState) {
        this.toState = toState;
    }

    public boolean isSendBackTransition() {
        return sendBackTransition;
    }

    public void setSendBackTransition(boolean sendBackTransition) {
        this.sendBackTransition = sendBackTransition;
    }

    public Transition() {
    }
}
