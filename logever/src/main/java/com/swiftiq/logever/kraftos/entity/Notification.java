package com.swiftiq.logever.kraftos.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Map;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String app;

    private String user;

    private Integer role;

    private Integer department;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(nullable = false)
    private String primaryId;

    private String subPrimaryId;

    @Column(columnDefinition = "VARCHAR(4) DEFAULT 'LOW'")
    private String priority;

    private String createdBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getDepartment() {
        return department;
    }

    public void setDepartment(Integer department) {
        this.department = department;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSubPrimaryId() {
        return subPrimaryId;
    }

    public void setSubPrimaryId(String subPrimaryId) {
        this.subPrimaryId = subPrimaryId;
    }

    public Notification() {
    }

    public Notification(String app, String primaryId, String user, Integer role, Integer department,
                        Map<String,String> payload, String priority, String createdBy) {
        this.app = app;
        this.primaryId = primaryId;
        this.user = user;
        this.role = role;
        this.department = department;
        this.priority = priority;
        this.subPrimaryId = null;
        this.createdBy = createdBy;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String payloadStr = objectMapper.writeValueAsString(payload);
            this.payload = payloadStr;
        } catch(Exception ex) {
            this.payload = "";
        }
    }

    public Notification(String app, String primaryId, String subPrimaryId, String user, Integer role, Integer department,
                        Map<String,String> payload, String priority, String createdBy) {
        this.app = app;
        this.primaryId = primaryId;
        this.user = user;
        this.role = role;
        this.department = department;
        this.priority = priority;
        this.createdBy = createdBy;
        this.subPrimaryId = subPrimaryId;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String payloadStr = objectMapper.writeValueAsString(payload);
            this.payload = payloadStr;
        } catch(Exception ex) {
            this.payload = "";
        }
    }
}
