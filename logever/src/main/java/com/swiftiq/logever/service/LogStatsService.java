package com.swiftiq.logever.service;

import com.swiftiq.logever.main.LogStatsRepository;
import com.swiftiq.logever.main.UploadHistoryRepository;
import com.swiftiq.logever.main.entity.LogStats;
import com.swiftiq.logever.main.entity.UploadHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class LogStatsService {

    @Autowired
    LogStatsRepository logStatsRepository;

    public Iterable<LogStats> getAllFor(Integer formId) {
        return logStatsRepository.findAllByFormId(formId);
    }

    public Iterable<LogStats> getAll() {
        return logStatsRepository.findAll();
    }

    public Iterable<LogStats> getAppLogs() {
        return  logStatsRepository.findAllByFormId(-1);
    }
}
