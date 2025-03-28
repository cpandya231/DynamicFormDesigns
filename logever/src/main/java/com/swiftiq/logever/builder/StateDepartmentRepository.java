package com.swiftiq.logever.builder;

import com.swiftiq.logever.builder.entity.State;
import com.swiftiq.logever.builder.entity.StateDepartment;
import com.swiftiq.logever.builder.entity.StateRole;
import com.swiftiq.logever.builder.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface StateDepartmentRepository extends JpaRepository<StateDepartment, Integer> {

    public List<StateDepartment> findAllByStateId(String stateId);
    public void deleteAllByStateId(String stateId);
}
