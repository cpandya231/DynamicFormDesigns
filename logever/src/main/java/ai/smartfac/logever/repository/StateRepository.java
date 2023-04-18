package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.entity.Workflow;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface StateRepository extends CrudRepository<State, Integer> {

    public Iterable<State> findAllByWorkflowId(Integer workflowId);
    public void deleteAllByWorkflowId(Integer workflowId);
    @Modifying
    @Transactional
    @Query(value="delete from State c where c.workflow = ?1")
    public void deleteByWorkflow(Workflow workflow);
}
