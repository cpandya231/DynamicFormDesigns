package com.swiftiq.logever.builder.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity(name = "state_role")
public class StateRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String stateId;

    @Column(nullable = false)
    private Integer roleId;

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public StateRole() {
    }

    public StateRole(String stateId, Integer roleId) {
        this.stateId = stateId;
        this.roleId = roleId;
    }
}
