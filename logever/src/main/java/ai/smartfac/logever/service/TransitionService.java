package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Transition;
import ai.smartfac.logever.repository.TransitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

    @Transactional
    public void removeAllTransistionsForWorkflow(Integer workflowId) {
        transitionRepository.deleteAllByWorkflowId(workflowId);
    }
}
