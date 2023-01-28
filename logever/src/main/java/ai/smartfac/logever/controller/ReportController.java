package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Report;
import ai.smartfac.logever.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("")
    public ResponseEntity<?> getReports() {
        return new ResponseEntity<>(reportService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveReports() {
        return new ResponseEntity<>(reportService.getAllByIsActive(true), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable int id) {
        return new ResponseEntity<>(reportService.getById(id), HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveReport(@RequestBody Report report) {
        report.setVersion(1);
        report.setActive(true);
        return new ResponseEntity<>(reportService.save(report), HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateReport(@RequestBody Report report) {
        Report existingReport = reportService.getById(report.getId()).get();
        existingReport.setActive(false);
        existingReport.setReasonForChange(report.getReasonForChange());
        report.setId(null);
        report.setVersion(existingReport.getVersion()+1);
        report.setActive(true);
        reportService.save(existingReport);
        return new ResponseEntity<>(reportService.save(report), HttpStatus.OK);
    }

    @GetMapping("/run/{id}")
    public ResponseEntity<?> getReportResults(@PathVariable int id, @RequestParam(name = "page",required = false,defaultValue = "0") int page, @RequestParam(name="size", required = false,defaultValue = "0") int size) {
        return new ResponseEntity<>(reportService.getReportResults(id,page,size), HttpStatus.OK);
    }
}
