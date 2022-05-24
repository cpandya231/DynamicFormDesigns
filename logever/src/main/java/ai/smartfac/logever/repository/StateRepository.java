package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.State;
import org.springframework.data.repository.CrudRepository;

public interface StateRepository extends CrudRepository<State, Integer> {

    public Iterable<State> findAllByWorkflowId(Integer workflowId);
    public void deleteAllByWorkflowId(Integer workflowId);
}
