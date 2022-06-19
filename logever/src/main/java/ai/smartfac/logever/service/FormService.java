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

    public Optional<Form> getFormById(Integer id) {
        return formRepository.findById(id);
    }

    public Iterable<Form> getForms() {
        return formRepository.findAll();
    }

    public Optional<Form> getFormByName(String name) {
        return formRepository.findByName(name);
    }

    public Form save(Form form) {
        if(form.getVersion() == 1 && form.getId() == null) {
            jdbcTemplate.execute(form.makeCreateTableStmt());
            form.setColumns(form.getColumns());
            return formRepository.save(form);
        }
        return form;
    }

    public Form update(Form form, String prevColumns) {
        Form existingForm = getFormById(form.getId()).get();
        String alterStmt = form.makeAlterTableStmt(prevColumns);
        if(alterStmt.length() > 0)
            jdbcTemplate.execute(alterStmt);
        return formRepository.save(form);
    }
}
