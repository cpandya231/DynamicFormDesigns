package com.swiftiq.logever.main;

import com.swiftiq.logever.main.entity.AuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Timestamp;

public interface AuditTrailRepository extends CrudRepository<AuditTrail,Integer>, PagingAndSortingRepository<AuditTrail, Integer> {

    Page<AuditTrail> findByTypeAndAuditDtBetween(String type, Timestamp startDate, Timestamp endDate,Pageable sortedByDateDesc);
    Page<AuditTrail> findByUserNameAndAuditDtBetween(String username, Timestamp startDate, Timestamp endDate, Pageable sortedByDateDesc);
    Page<AuditTrail> findByAuditDtBetween(Timestamp startDate, Timestamp endDate, Pageable sortedByDateDesc);
    Page<AuditTrail> findAll(Pageable sortedByDateDesc);
}
