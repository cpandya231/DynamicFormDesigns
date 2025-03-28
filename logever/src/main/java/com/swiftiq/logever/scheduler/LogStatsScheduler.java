package com.swiftiq.logever.scheduler;

import com.google.gson.Gson;
import com.swiftiq.logever.builder.entity.Form;
import com.swiftiq.logever.kraftos.RoleRepository;
import com.swiftiq.logever.kraftos.entity.Department;
import com.swiftiq.logever.kraftos.entity.Role;
import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.main.entity.LogStats;
import com.swiftiq.logever.model.FormSettings;
import com.swiftiq.logever.model.FormTemplate;
import com.swiftiq.logever.service.DepartmentService;
import com.swiftiq.logever.service.FormService;
import com.swiftiq.logever.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class LogStatsScheduler {

    @Autowired
    FormService formService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogStatsScheduler.class);

    @Scheduled(cron = "${stats.update.scheduled.interval}")
    public void runScheduledTask() {
        int masterCount = 0;
        int totalRecords = 0;
        int owners = 0;
        jdbcTemplate.execute("truncate table log_stats");
        List<Form> forms = StreamSupport.stream(formService.getAll().spliterator(),false)
                .filter(f->f.getStatus().equalsIgnoreCase("Published"))
                .collect(Collectors.toList());
        for(Form f: forms) {
            String tableName = "f_"+String.join("_",f.getName().trim().toLowerCase().split(" "))+"_lgs";
            if(f.getType().equalsIgnoreCase("master")) {
                masterCount++;
                totalRecords += jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
                Gson gson = new Gson();
                FormSettings formSettings = gson.fromJson(f.getSettings(), FormSettings.class);
                owners += formSettings.getOwners()!=null?formSettings.getOwners().split(",").length:0;
                jdbcTemplate.execute("insert into log_stats (form_id, state, count) select "+f.getId()+", state, count(*) from "+tableName+" group by state");
                jdbcTemplate.execute("insert into log_stats (form_id, state, count) select "+f.getId()+", 'Invalid', count(*) from "+tableName+" where is_invalid = 1");
                jdbcTemplate.execute("insert into log_stats (form_id, state, count) select "+f.getId()+", 'Valid', count(*) from "+tableName+" where is_invalid = 0");
            }
        }
        jdbcTemplate.execute("insert into log_stats (form_id, state, count) select -1, 'Users', count(distinct created_by) from upload_history");
        jdbcTemplate.execute("insert into log_stats (form_id, state, count) values (-1, 'Records',"+totalRecords+")");
        jdbcTemplate.execute("insert into log_stats (form_id, state, count) values (-1, 'Owners',"+owners+")");
        jdbcTemplate.execute("insert into log_stats (form_id, state, count) values (-1, 'Datasets',"+masterCount+")");
    }
}
