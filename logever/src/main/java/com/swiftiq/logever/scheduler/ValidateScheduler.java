package com.swiftiq.logever.scheduler;

import com.google.gson.Gson;
import com.swiftiq.logever.builder.entity.Form;
import com.swiftiq.logever.model.FormSettings;
import com.swiftiq.logever.model.FormTemplate;
import com.swiftiq.logever.service.FormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ValidateScheduler {

    @Autowired
    FormService formService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateScheduler.class);

    @Scheduled(cron = "${log.validate.scheduled.interval}")
    public void runScheduledTask() {
        List<Form> forms = StreamSupport.stream(formService.getAll().spliterator(),false)
                .filter(f->f.getStatus().equalsIgnoreCase("Published"))
                .collect(Collectors.toList());
        for(Form f: forms) {
            String tableName = "f_"+String.join("_",f.getName().trim().toLowerCase().split(" "))+"_lgs";
            String tempTableName = tableName+"_tmp";
            Gson gson = new Gson();
            FormSettings settings = gson.fromJson(f.getSettings(), FormSettings.class);
            if(settings.getValidation() != null && !settings.getValidation().getPostIngestCode().isEmpty()) {
                String query = settings.getValidation().getPostIngestCode();
                jdbcTemplate.execute("DROP TEMPORARY TABLE IF EXISTS temp_table");
                String tempTableQuery = "CREATE TEMPORARY TABLE temp_table AS "+query;
                jdbcTemplate.execute(tempTableQuery);
                String updateStmt = "update "+tableName+" set is_invalid = 1 where id not in (select id from temp_table)";
                jdbcTemplate.execute(updateStmt);
            }
        }
        String dropStmt = "DROP TEMPORARY TABLE IF EXISTS temp_table";
        jdbcTemplate.execute(dropStmt);
    }
}
