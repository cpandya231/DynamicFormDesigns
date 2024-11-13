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

}
