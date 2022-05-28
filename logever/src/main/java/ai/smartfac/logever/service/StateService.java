package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Iterator;

@Service
public class StateService {

    @Autowired
    StateRepository stateRepository;

    public Iterable<State> saveAllStates(Iterable<State> states) {
        return stateRepository.saveAll(states);
    }

    public Iterable<State> getWorkflowStates(Integer workflowId) {
        return stateRepository.findAllByWorkflowId(workflowId);
    }

    @Transactional
    public void removeStatesForWorkflow(Integer workflowId) {
        stateRepository.deleteAllByWorkflowId(workflowId);
    }
}
