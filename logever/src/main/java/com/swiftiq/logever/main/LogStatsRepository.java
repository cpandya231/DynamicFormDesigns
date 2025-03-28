package com.swiftiq.logever.main;

import com.swiftiq.logever.main.entity.LogStats;
import com.swiftiq.logever.main.entity.UploadHistory;
import org.springframework.data.repository.CrudRepository;

public interface LogStatsRepository extends CrudRepository<LogStats,Integer> {
    Iterable<LogStats> findAllByFormId(Integer formId);
}
