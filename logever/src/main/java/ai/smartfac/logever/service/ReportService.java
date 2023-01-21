package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Report;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public Iterable<DataQuery> getReportResults(int id, int page, int size) {
        System.out.println("Inside req "+page+" "+size);
        Report report = getById(id).get();
        String selectCols = report.getColumns();
        String selectStmt = report.getTemplate();
        if(page > 0 && size > 0) {
            selectStmt = selectStmt + " limit "+size+" offset "+((page-1)*size);
        }
        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }
}
