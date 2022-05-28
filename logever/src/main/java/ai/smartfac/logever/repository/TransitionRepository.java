package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.Transition;
import org.springframework.data.repository.CrudRepository;

public interface TransitionRepository extends CrudRepository<Transition, Integer> {

    public Iterable<Transition> findAllByWorkflowId(Integer workflowId);
    public void deleteAllByWorkflowId(Integer workflowId);
}
