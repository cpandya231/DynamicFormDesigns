package com.swiftiq.logever.response;

import com.swiftiq.logever.main.entity.AuditTrail;

import java.util.List;

public class AuditTrailResponse {

   private int totalPages;

   private List<AuditTrail> auditTrails;

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<AuditTrail> getAuditTrails() {
        return auditTrails;
    }

    public void setAuditTrails(List<AuditTrail> auditTrails) {
        this.auditTrails = auditTrails;
    }
}
