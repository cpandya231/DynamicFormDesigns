package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.AuditTrail;
import ai.smartfac.logever.repository.AuditTrailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
public class AuditTrailService {

    @Autowired
    AuditTrailRepository auditTrailRepository;

    public Iterable<AuditTrail> findByTypeAndAuditDtBetween(String type, Timestamp startDate, Timestamp endDate) {
        return auditTrailRepository.findByTypeAndAuditDtBetween(type, startDate, endDate, getPageable(0, 10));
    }

    public Iterable<AuditTrail> findByUserNameAndAuditDtBetween(String username, Timestamp startDate, Timestamp endDate) {
        return auditTrailRepository.findByUserNameAndAuditDtBetween(username, startDate, endDate, getPageable(0, 10));
    }

    public List<AuditTrail> findAuditTrail(String username,
                                           String type,
                                           String startDate,
                                           String endDate,
                                           Integer pageNumber,
                                           Integer pageSize) {


        Date date = Date.from(ZonedDateTime.now().minusMonths(1).toInstant());

        Timestamp startDateTimestamp = new Timestamp(date.getTime());
        Timestamp endDateTimestamp = Timestamp.from(Instant.now());
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            if (!ObjectUtils.isEmpty((startDate)) && !ObjectUtils.isEmpty(endDate)) {
                startDateTimestamp = new Timestamp(df.parse(startDate).getTime());
                endDateTimestamp = new Timestamp(df.parse(endDate).getTime());
            }
            Pageable sortedByDateDesc = getPageable(pageNumber, pageSize);
            if (!ObjectUtils.isEmpty((username))) {
                return auditTrailRepository.findByUserNameAndAuditDtBetween(username, startDateTimestamp, endDateTimestamp, sortedByDateDesc);
            } else if (!ObjectUtils.isEmpty(type)) {
                return auditTrailRepository.findByTypeAndAuditDtBetween(type, startDateTimestamp, endDateTimestamp, sortedByDateDesc);
            } else {
                return auditTrailRepository.findByAuditDtBetween(startDateTimestamp, endDateTimestamp, sortedByDateDesc);
            }

        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parsing error");
        }

    }

    private Pageable getPageable(Integer pageNumber, Integer pageSize) {
        return PageRequest.of(pageNumber, pageSize, Sort.by("auditDt").descending().and(Sort.by("id").descending()));
    }
}
