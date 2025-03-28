package com.swiftiq.logever.builder;

import com.swiftiq.logever.builder.entity.State;
import com.swiftiq.logever.builder.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface StateRepository extends JpaRepository<State, Integer> {
    public Iterable<State> findAllByWorkflowId(Integer workflowId);
    public void deleteAllByWorkflowId(Integer workflowId);
    @Modifying
    @Transactional
    @Query(value="delete from state c where c.workflow = ?1")
    public void deleteByWorkflow(Workflow workflow);
}
