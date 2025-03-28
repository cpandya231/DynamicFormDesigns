package com.swiftiq.logever.builder;

import com.swiftiq.logever.builder.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FormRepository extends JpaRepository<Form,Integer> {

    public Optional<Form> findByName(String name);
    public Iterable<Form> findAllByAppId(Integer appId);
    public Optional<Form> findByWorkflowId(Integer workflowId);
}
