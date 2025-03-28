package com.swiftiq.logever.service;

import com.swiftiq.logever.main.entity.Report;
import com.swiftiq.logever.model.DataQuery;
import com.swiftiq.logever.main.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Report save(Report report) {
        return reportRepository.save(report);
    }

    public Optional<Report> getById(int id) {
        return reportRepository.findById(id);
    }

    public Iterable<Report> getAll() {
        return reportRepository.findAll();
    }

    public Iterable<Report> getAllByIsActive(boolean isActive) {
        return reportRepository.findAllByIsActive(isActive);
    }

    public DataQuery getReportResults(int id, int page, int size) {
        System.out.println("Inside req "+page+" "+size);
        Report report = getById(id).get();
        String selectCols = report.getColumns();
        String selectStmt = report.getTemplate();
        if(page > 0 && size > 0) {
            selectStmt = selectStmt + " limit "+size+" offset "+((page-1)*size);
        }
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        return new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        return jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }
}
