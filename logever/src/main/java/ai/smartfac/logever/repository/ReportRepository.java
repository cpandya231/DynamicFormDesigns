package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.Report;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ReportRepository extends CrudRepository<Report,Integer> {
    public Iterable<Report> findAllByIsActive(Boolean isActive);
}
