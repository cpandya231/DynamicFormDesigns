package com.swiftiq.logever.builder;

import com.swiftiq.logever.builder.entity.Transition;
import com.swiftiq.logever.builder.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(transactionManager = "builderTransactionManager")
public interface TransitionRepository extends JpaRepository<Transition, Integer> {

    public Iterable<Transition> findAllByWorkflowId(Integer workflowId);
    public void deleteAllByWorkflowId(Integer workflowId);
    @Modifying
    @Transactional
    @Query(value="delete from transition c where c.workflow = ?1")
    public void deleteByWorkflow(Workflow workflow);
}
