package com.swiftiq.logever.main;

import com.swiftiq.logever.main.entity.Report;
import org.springframework.data.repository.CrudRepository;

public interface ReportRepository extends CrudRepository<Report,Integer> {
    public Iterable<Report> findAllByIsActive(Boolean isActive);
}
