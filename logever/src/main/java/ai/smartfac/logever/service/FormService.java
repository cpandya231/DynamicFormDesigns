package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FormService {

    @Autowired
    FormRepository formRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Iterable<Form> getForms() {
        return formRepository.findAll();
    }

    public Optional<Form> getFormByName(String name) {
        return formRepository.findByName(name);
    }

    public Form save(Form form) {
        jdbcTemplate.execute(form.makeCreateTableStmt());
        return formRepository.save(form);
    }
}
