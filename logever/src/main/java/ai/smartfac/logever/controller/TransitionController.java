package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.entity.Transition;
import ai.smartfac.logever.entity.Workflow;
import ai.smartfac.logever.model.StateTransitionRequest;
import ai.smartfac.logever.service.StateService;
import ai.smartfac.logever.service.TransitionService;
import ai.smartfac.logever.service.WorkflowService;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/transitions")
public class TransitionController {

    @Autowired
    TransitionService transitionService;

    @Autowired
    WorkflowService workflowService;

    @Autowired
    StateService stateService;

    @GetMapping("/{workflowId}/")
    public ResponseEntity<?> getWorkflowTransitions(@PathVariable(name = "workflowId") Integer workflowId) {
        return new ResponseEntity<>(transitionService.getWorkflowTransitions(workflowId), HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveAllTransitions(@RequestBody StateTransitionRequest stateTransitionRequest) {
        Optional<Workflow> workflow = workflowService.getWorkflowById(stateTransitionRequest.getWorkflowId());

        if(workflow.isPresent()) {
            Iterable<Transition> transitions = stateTransitionRequest.getStateTransitions();
            transitions.forEach(transition -> transition.setWorkflow(workflow.get()));
            List<Transition> transitionList = (StreamSupport.stream(transitions.spliterator(),false)).collect(Collectors.toList());

            Iterable<State> workflowStates = stateService.getWorkflowStates(stateTransitionRequest.getWorkflowId());
            List<Integer> workflowStateIds = (StreamSupport.stream(workflowStates.spliterator(),false)).map(state -> state.getId()).collect(Collectors.toList());

            List<Transition> validTransitions = transitionList.stream().filter(transition -> {
                if(workflowStateIds.contains(transition.getFromState().getId()))
                    return true;
                else
                    return false;
            }).collect(Collectors.toList());

            if(validTransitions.size() == transitionList.size()) {
                Iterable<Transition> savedTransitions = transitionService.saveAllTransitions(transitions);
                return new ResponseEntity<>(savedTransitions, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updateStates(@RequestBody StateTransitionRequest stateTransitionRequest) {
        Optional<Workflow> savedWorkflow = workflowService.getWorkflowById(stateTransitionRequest.getWorkflowId());

        if(savedWorkflow.isPresent()) {
            Iterable<Transition> transitions = stateTransitionRequest.getStateTransitions();
            transitions.forEach(transition -> transition.setWorkflow(savedWorkflow.get()));
            List<Transition> transitionList = (StreamSupport.stream(transitions.spliterator(),false)).collect(Collectors.toList());

            Iterable<State> workflowStates = stateService.getWorkflowStates(stateTransitionRequest.getWorkflowId());
            List<Integer> workflowStateIds = (StreamSupport.stream(workflowStates.spliterator(),false)).map(state -> state.getId()).collect(Collectors.toList());

            List<Transition> validTransitions = transitionList.stream().filter(transition -> {
                if(workflowStateIds.contains(transition.getFromState().getId()))
                    return true;
                else
                    return false;
            }).collect(Collectors.toList());

            if(validTransitions.size() == transitionList.size()) {
                transitionService.removeAllTransistionsForWorkflow(savedWorkflow.get());
                Iterable<Transition> savedTransitions = transitionService.saveAllTransitions(transitions);
                workflowService.saveWorkflow(savedWorkflow.get());
                return new ResponseEntity<>(savedTransitions, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
