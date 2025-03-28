package com.swiftiq.logever.service;

import com.swiftiq.logever.builder.StateDepartmentRepository;
import com.swiftiq.logever.builder.StateRoleRepository;
import com.swiftiq.logever.builder.entity.State;
import com.swiftiq.logever.builder.entity.StateDepartment;
import com.swiftiq.logever.builder.entity.StateRole;
import com.swiftiq.logever.builder.entity.Workflow;
import com.swiftiq.logever.builder.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class StateService {

    @Autowired
    StateRepository stateRepository;

    @Autowired
    StateDepartmentRepository stateDepartmentRepository;

    @Autowired
    StateRoleRepository stateRoleRepository;

    public Iterable<State> saveAllStates(Iterable<State> states, Workflow workflow) {
        System.out.println("Saving new states");
        System.out.println("Workflow id is "+workflow.getId());
        System.out.println("Workflow id is "+workflow.getStates().size());
        Iterable<State> savedStates = stateRepository.saveAll(states);
//        savedStates.forEach(state -> System.out.println(state.));
        states.forEach(state -> {
            String stateId = StreamSupport.stream(savedStates.spliterator(), false).filter(st->st.getName().equalsIgnoreCase(state.getName())).findFirst().get().getId() + "";
            System.out.println("Saved state id is "+stateId);
            stateRoleRepository.saveAll(state.getRoles().stream().map(r-> new StateRole(stateId, r.getId())).collect(Collectors.toList()));
            stateDepartmentRepository.saveAll(state.getDepartments().stream().map(r-> new StateDepartment(stateId, r.getId())).collect(Collectors.toList()));
        });
        return savedStates;
    }

    public Iterable<State> getWorkflowStates(Integer workflowId) {
        return stateRepository.findAllByWorkflowId(workflowId);
    }

    @Transactional(transactionManager = "builderTransactionManager")
    public void removeStatesForWorkflow(Workflow workflow) {
        System.out.println("Removing states for workflowId "+workflow.getId());
        stateRepository.deleteByWorkflow(workflow);
    }

    public void removeRolesAndDepartments(Workflow workflow) {
        workflow.getStates().forEach(state -> {
            stateDepartmentRepository.deleteAllByStateId(state.getStateId());
            stateRoleRepository.deleteAllByStateId(state.getStateId());
        });
    }
}
