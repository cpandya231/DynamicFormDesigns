package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.AuditTrail;
import ai.smartfac.logever.model.DateRange;
import ai.smartfac.logever.service.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
public class AuditTrailController {

    @Autowired
    AuditTrailService auditTrailService;


    @GetMapping("/")
    public ResponseEntity<?> getAuditTrail(
            @RequestParam(required = false,defaultValue = "") String user,
            @RequestParam(required = false,defaultValue = "") String type,
            @RequestParam(required = false,defaultValue = "")  String startDate,
            @RequestParam(required = false,defaultValue = "") String endDate,
            @RequestParam(required = false,defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false,defaultValue = "10") Integer pageSize) {
        Iterable<AuditTrail> auditTrail = auditTrailService.findAuditTrail(user,type, startDate,endDate,pageNumber,pageSize);
        return new ResponseEntity<>(auditTrail, HttpStatus.OK);
    }
    @GetMapping("/{type}")
    public ResponseEntity<?> getAuditTrailByType(@PathVariable String type, @RequestBody DateRange dateRange) {
        Iterable<AuditTrail> auditTrail = auditTrailService.findByTypeAndAuditDtBetween(type, dateRange.getStartDate(),dateRange.getEndDate());
        return new ResponseEntity<>(auditTrail, HttpStatus.OK);
    }

    @GetMapping("/user/{user}")
    public ResponseEntity<?> getAuditTrailByUser(@PathVariable String user, @RequestBody DateRange dateRange) {
        Iterable<AuditTrail> auditTrail = auditTrailService.findByUserNameAndAuditDtBetween(user, dateRange.getStartDate(),dateRange.getEndDate());
        return new ResponseEntity<>(auditTrail, HttpStatus.OK);
    }
}
