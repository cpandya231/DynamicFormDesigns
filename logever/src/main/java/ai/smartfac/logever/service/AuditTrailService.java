package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.AuditTrail;
import ai.smartfac.logever.repository.AuditTrailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class AuditTrailService {

    @Autowired
    AuditTrailRepository auditTrailRepository;

    public Iterable<AuditTrail> findByTypeAndAuditDtBetween(String type, Timestamp startDate,Timestamp endDate) {
        return auditTrailRepository.findByTypeAndAuditDtBetween(type, startDate,endDate);
    }

    public Iterable<AuditTrail> findByUserNameAndAuditDtBetween(String username, Timestamp startDate,Timestamp endDate) {
        return auditTrailRepository.findByUserNameAndAuditDtBetween(username, startDate,endDate);
    }
}
