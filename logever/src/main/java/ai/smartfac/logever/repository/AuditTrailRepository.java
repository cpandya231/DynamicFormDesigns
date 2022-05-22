package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.AuditTrail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Timestamp;
import java.util.List;

public interface AuditTrailRepository extends CrudRepository<AuditTrail,Integer>, PagingAndSortingRepository<AuditTrail, Integer> {

    List<AuditTrail> findByTypeAndAuditDtBetween(String type, Timestamp startDate, Timestamp endDate,Pageable sortedByDateDesc);

    List<AuditTrail> findByUserNameAndAuditDtBetween(String username, Timestamp startDate, Timestamp endDate, Pageable sortedByDateDesc);
    List<AuditTrail> findByAuditDtBetween(Timestamp startDate, Timestamp endDate, Pageable sortedByDateDesc);
}
