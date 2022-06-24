package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FormService {

    @Autowired
    FormRepository formRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Optional<Form> getFormById(Integer id) {
        return formRepository.findById(id);
    }

    public Iterable<Form> getForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        if(user.getAuthorities().stream().filter(auth->auth.getAuthority().equals("ROLE_ADMIN")).count()>0)
            return forms;
        else {
            return StreamSupport.stream(forms.spliterator(),false)
                    .filter(form->{
                        return form.getWorkflow().getStates().stream().filter(state-> {
                            return state.getDepartments().contains(user.getDepartment()) || state.getRoles().contains(user.getRoles().toArray()[0]);
                        }).count() > 0;
                    })
                    .collect(Collectors.toList());
        }
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
