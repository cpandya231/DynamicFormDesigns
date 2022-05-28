package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Workflow;
import ai.smartfac.logever.repository.StateRepository;
import ai.smartfac.logever.repository.TransitionRepository;
import ai.smartfac.logever.repository.WorkflowRepository;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

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
