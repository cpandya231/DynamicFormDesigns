package com.swiftiq.logever.builder;

import com.swiftiq.logever.builder.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {
}
