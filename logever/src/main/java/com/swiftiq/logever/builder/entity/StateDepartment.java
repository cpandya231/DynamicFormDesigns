package com.swiftiq.logever.builder.entity;

import javax.persistence.*;

@Entity(name = "state_department")
public class StateDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String stateId;

    @Column(nullable = false)
    private Integer departmentId;

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public StateDepartment() {
    }

    public StateDepartment(String stateId, Integer departmentId) {
        this.stateId = stateId;
        this.departmentId = departmentId;
    }
}
