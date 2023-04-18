package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.Transition;
import ai.smartfac.logever.entity.Workflow;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface TransitionRepository extends CrudRepository<Transition, Integer> {

    public Iterable<Transition> findAllByWorkflowId(Integer workflowId);
    public void deleteAllByWorkflowId(Integer workflowId);
    @Modifying
    @Transactional
    @Query(value="delete from Transition c where c.workflow = ?1")
    public void deleteByWorkflow(Workflow workflow);
}
