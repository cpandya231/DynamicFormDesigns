package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Report;
import ai.smartfac.logever.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    ReportRepository reportRepository;

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
}
