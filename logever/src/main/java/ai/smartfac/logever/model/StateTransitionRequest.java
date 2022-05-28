package ai.smartfac.logever.model;

import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.entity.Transition;

import java.util.Set;

public class StateTransitionRequest {
    private Integer workflowId;
    private Iterable<State> states;
    private Iterable<Transition> transitions;

    public StateTransitionRequest() {
    }

    public StateTransitionRequest(Integer workflowId, Iterable<State> states, Iterable<Transition> transitions) {
        this.workflowId = workflowId;
        this.states = states;
        this.transitions = transitions;
    }

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    public Iterable<State> getStates() {
        return states;
    }

    public void setStates(Iterable<State> states) {
        this.states = states;
    }

    public Iterable<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Iterable<Transition> transitions) {
        this.transitions = transitions;
    }
}
