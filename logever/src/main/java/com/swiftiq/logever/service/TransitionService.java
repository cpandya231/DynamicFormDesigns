package com.swiftiq.logever.service;

import com.swiftiq.logever.builder.entity.Transition;
import com.swiftiq.logever.builder.entity.Workflow;
import com.swiftiq.logever.builder.TransitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransitionService {

    @Autowired
    TransitionRepository transitionRepository;

    public Iterable<Transition> getWorkflowTransitions(Integer workflowId) {
        return transitionRepository.findAllByWorkflowId(workflowId);
    }

    public Iterable<Transition> saveAllTransitions(Iterable<Transition> transitions) {
        return transitionRepository.saveAll(transitions);
    }

    @Transactional(transactionManager = "builderTransactionManager")
    public void removeAllTransistionsForWorkflow(Workflow workflow) {
        System.out.println("Removing transitions");
        transitionRepository.deleteByWorkflow(workflow);
    }
}
