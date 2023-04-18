package ai.smartfac.logever.model;

import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.entity.Transition;
import ai.smartfac.logever.entity.Workflow;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StateTransitionRequest {
    private Integer workflowId;
    private Iterable<State> states;
    private Iterable<TransitionModel> transitions;

    public StateTransitionRequest() {
    }

    public StateTransitionRequest(Integer workflowId, Iterable<State> states, Iterable<TransitionModel> transitions) {
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

    public Iterable<TransitionModel> getTransitions() {
        return transitions;
    }

    public Iterable<Transition> getStateTransitions() {
        states.forEach(s->System.out.println(s.getStateId()+"--"+s.getId()));
        transitions.forEach(s->System.out.println(s.fromState));
        return StreamSupport.stream(transitions.spliterator(),false).map(t->{
            Integer fromState = StreamSupport.stream(states.spliterator(),false).filter(s->s.getStateId().equals(t.fromState)).findFirst().get().getId();
            Integer toState = StreamSupport.stream(states.spliterator(),false).filter(s->s.getStateId().equals(t.toState)).findFirst().get().getId();
            return new Transition(null,new Workflow(workflowId),new State(fromState),new State(toState),false);
        }).collect(Collectors.toList());
    }

    public void setTransitions(Iterable<TransitionModel> transitions) {
        this.transitions = transitions;
    }
}
