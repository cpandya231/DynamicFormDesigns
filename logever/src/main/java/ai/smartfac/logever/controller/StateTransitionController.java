package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.entity.Transition;
import ai.smartfac.logever.entity.Workflow;
import ai.smartfac.logever.model.StateTransitionRequest;
import ai.smartfac.logever.model.TransitionModel;
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
@RequestMapping("/state_transitions")
public class StateTransitionController {

    @Autowired
    WorkflowService workflowService;

    @Autowired
    StateService stateService;

    @Autowired
    TransitionService transitionService;

    @GetMapping("/{workflowId}/")
    public ResponseEntity<?> getWorkflowStatesAndTransitions(@PathVariable(name = "workflowId") Integer workflowId) {
        Optional<Workflow> workflow = workflowService.getWorkflowById(workflowId);

        if(workflow.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        StateTransitionRequest stateTransitionRequest  = new StateTransitionRequest();
        stateTransitionRequest.setWorkflowId(workflowId);
        stateTransitionRequest.setStates(stateService.getWorkflowStates(workflowId));
        stateTransitionRequest.setTransitions(StreamSupport.stream(transitionService.getWorkflowTransitions(workflowId).spliterator(),false).map(t->
             new TransitionModel(t.getFromState().getId()+"",t.getToState().getId()+"")
        ).collect(Collectors.toList()));
        return new ResponseEntity<>(stateTransitionRequest, HttpStatus.OK);
    }

}
