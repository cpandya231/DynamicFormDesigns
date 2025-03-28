package com.swiftiq.logever.service;

import com.swiftiq.logever.builder.entity.Workflow;
import com.swiftiq.logever.builder.StateRepository;
import com.swiftiq.logever.builder.TransitionRepository;
import com.swiftiq.logever.builder.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorkflowService {

    @Autowired
    WorkflowRepository workflowRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    TransitionRepository transitionRepository;

    public Workflow saveWorkflow(Workflow workflow) {
        if(workflow.getStates()!=null) {
            //workflow.getStates().forEach(state -> state.setWorkflow(workflow));
        }
        return workflowRepository.save(workflow);
    }

    public Optional<Workflow> getWorkflowById(Integer id) {
        return workflowRepository.findById(id);
    }
}
