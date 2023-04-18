package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.entity.Transition;
import ai.smartfac.logever.entity.Workflow;
import ai.smartfac.logever.model.StateTransitionRequest;
import ai.smartfac.logever.service.FormService;
import ai.smartfac.logever.service.StateService;
import ai.smartfac.logever.service.TransitionService;
import ai.smartfac.logever.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/states")
public class StateController {

    @Autowired
    StateService stateService;

    @Autowired
    WorkflowService workflowService;

    @Autowired
    TransitionService transitionService;

    @Autowired
    FormService formService;

    @PostMapping("/")
    public ResponseEntity<?> saveAllStates(@RequestBody StateTransitionRequest stateTransitionRequest) {
        Optional<Workflow> workflow = workflowService.getWorkflowById(stateTransitionRequest.getWorkflowId());

        if(workflow.isPresent()) {
            Iterable<State> states = stateTransitionRequest.getStates();
            states.forEach(state -> state.setWorkflow(workflow.get()));
            Iterable<State> savedStates = stateService.saveAllStates(states);
            return new ResponseEntity<>(savedStates, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updateStates(@RequestBody StateTransitionRequest stateTransitionRequest) {
        Optional<Workflow> savedWorkflow = workflowService.getWorkflowById(stateTransitionRequest.getWorkflowId());

        if(savedWorkflow.isPresent()) {
            System.out.println("Workflow present");
            Iterable<State> newStates = stateTransitionRequest.getStates();
            newStates.forEach(state -> state.setWorkflow(savedWorkflow.get()));
            transitionService.removeAllTransistionsForWorkflow(savedWorkflow.get());
            stateService.removeStatesForWorkflow(savedWorkflow.get());

            Iterable<State> savedStates = stateService.saveAllStates(newStates);
            workflowService.saveWorkflow(savedWorkflow.get());
            stateTransitionRequest.setStates(savedStates);
            Iterable<Transition> savedTransitions = transitionService.saveAllTransitions(stateTransitionRequest.getStateTransitions());

            Workflow workflow = workflowService.getWorkflowById(stateTransitionRequest.getWorkflowId()).get();
            workflow.setStates(StreamSupport.stream(savedStates.spliterator(),false).collect(Collectors.toSet()));
            workflow.setTransitions(StreamSupport.stream(savedTransitions.spliterator(),false).collect(Collectors.toSet()));
            return new ResponseEntity<>(workflow, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
