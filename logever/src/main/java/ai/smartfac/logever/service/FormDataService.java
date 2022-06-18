package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insertInto(Form form, Map<String,String> values) {
        Map<String, String> data = values;
        jdbcTemplate.execute(form.makeInsertValuesStmt(values));
    }
}
