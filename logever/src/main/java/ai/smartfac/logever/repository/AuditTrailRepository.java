package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.AuditTrail;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;

public interface AuditTrailRepository extends CrudRepository<AuditTrail,Integer> {

    public Iterable<AuditTrail> findByTypeAndAuditDtBetween(String type, Timestamp startDate, Timestamp endDate);
    public Iterable<AuditTrail> findByUserNameAndAuditDtBetween(String username, Timestamp startDate, Timestamp endDate);
}
