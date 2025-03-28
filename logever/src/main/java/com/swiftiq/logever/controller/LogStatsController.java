package com.swiftiq.logever.controller;

import com.swiftiq.logever.main.entity.LogStats;
import com.swiftiq.logever.main.entity.UploadHistory;
import com.swiftiq.logever.service.LogStatsService;
import com.swiftiq.logever.service.UploadHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class LogStatsController {

    @Autowired
    LogStatsService logStatsService;

    @GetMapping("/")
    public ResponseEntity<?> getAllStats() {
        Iterable<LogStats> logStats = logStatsService.getAll();
        return new ResponseEntity<>(logStats, HttpStatus.OK);
    }

    @GetMapping("/{formId}/")
    public ResponseEntity<?> getStats(@PathVariable(name = "formId") Integer formId) {
        Iterable<LogStats> logStats = logStatsService.getAllFor(formId);
        return new ResponseEntity<>(logStats, HttpStatus.OK);
    }

    @GetMapping("/sys")
    public ResponseEntity<?> getSystemStats() {
        Iterable<LogStats> logStats = logStatsService.getAllFor(-1);
        return new ResponseEntity<>(logStats, HttpStatus.OK);
    }
}
