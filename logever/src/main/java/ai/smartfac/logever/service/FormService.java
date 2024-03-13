package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.*;
import ai.smartfac.logever.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FormService {

    @Autowired
    FormRepository formRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    RoleService roleService;

    public Optional<Form> getFormById(Integer id) {
        return formRepository.findById(id);
    }

    public Iterable<Form> getInitiatableForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        return StreamSupport.stream(forms.spliterator(),false).filter(form-> {
            State firstState = form.getWorkflow().getStates().stream().filter(st->st.isFirstState()).findFirst().get();
            Set<Department> authDepts = firstState.getDepartments();
            Set<Role> authRoles = firstState.getRoles();
            return (authDepts.size()==0 || departmentService.checkAccess(user.getDepartment(), authDepts)) && (authRoles.size()==0 || roleService.hasAccess(user
                    .getRoles(),authRoles));
        }).collect(Collectors.toList());
    }

    public Iterable<Form> getAccessibleForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        return StreamSupport.stream(forms.spliterator(),false).filter(form-> {
            return form.getWorkflow().getStates().stream().filter(st -> {
                Set<Department> authDepts = st.getDepartments();
                boolean initDeptCheck = authDepts.stream().filter(d->d.getName().equalsIgnoreCase("Initiator Department")).count() > 0;
                Set<Role> authRoles = st.getRoles();
                return (authDepts.size() == 0 || initDeptCheck || departmentService.checkAccess(user.getDepartment(), authDepts)) && (authRoles.size() == 0 || roleService.hasAccess(user
                        .getRoles(), authRoles));
            }).count() > 0;
        }).collect(Collectors.toList());
    }

    public Iterable<Form> getLastStateAccessibleForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        return StreamSupport.stream(forms.spliterator(),false).filter(form-> {
            return form.getWorkflow().getStates().stream().filter(st->st.isEndState()).filter(st -> {
                Set<Department> authDepts = st.getDepartments();
                boolean initDeptCheck = authDepts.stream().filter(d->d.getName().equalsIgnoreCase("Initiator Department")).count() > 0;
                Set<Role> authRoles = st.getRoles();
                return (authDepts.size() == 0 || initDeptCheck || departmentService.checkAccess(user.getDepartment(), authDepts)) && (authRoles.size() == 0 || roleService.hasAccess(user
                        .getRoles(), authRoles));
            }).count() > 0;
        }).collect(Collectors.toList());
    }

    public Iterable<Form> getForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        if (user.getAuthorities().stream().filter(auth -> auth.getAuthority().equals("ROLE_ADMIN")).count() > 0 || 1==1)
            return forms;
        else {
            return StreamSupport.stream(forms.spliterator(), false)
                    .filter(form ->
                            form.getWorkflow().getStates().stream()
                                    .anyMatch(state -> state.getDepartments().contains(user.getDepartment())
                                            || !Collections.disjoint(state.getRoles(), user.getRoles())))
                    .collect(Collectors.toList());
        }
    }

    public Optional<Form> getFormByName(String name) {
        return formRepository.findByName(name);
    }

    public Form save(Form form) {
//        if (form.getVersion() == 1 && form.getId() == null) {
        jdbcTemplate.execute(form.makeCreateTableStmt());
        jdbcTemplate.execute(form.makeCreateMetaDataTableStmt());
        form.makeCreateGridTableStmt().forEach(stmt-> {
            jdbcTemplate.execute(stmt);
        });
        form.makeCreateGridHistoryTableStmt().forEach(stmt-> {
            jdbcTemplate.execute(stmt);
        });
        if(form.getType().equalsIgnoreCase("master"))
            jdbcTemplate.execute(form.makeCreateMasterTableStmt());
        form.setColumns(form.getColumns());
        return formRepository.save(form);
//        }
    }

    public Iterable<Form> getFormsByApp(Integer appId) {
        return formRepository.findAllByAppId(appId);
    }

    public Form update(Form form, boolean rename) {
//        String alterStmt = form.makeAlterTableStmt(prevColumns);
//        String alterTableMetaDataStmt = form.makeAlterTableMetaDataStmt(prevColumns);
//        if (alterStmt.length() > 0) {
//            if(form.getType().equalsIgnoreCase("master")){
//                String alterMasterTableStmt = form.makeAlterMasterTableStmt(prevColumns);
//                jdbcTemplate.execute(alterMasterTableStmt);
//            }
//            jdbcTemplate.execute(alterStmt);
//            jdbcTemplate.execute(alterTableMetaDataStmt);
//
//        }
        if (rename) {
            jdbcTemplate.execute(form.renameTableStmt(form.getVersion() + ""));
            jdbcTemplate.execute(form.renameMetaDataTableStmt(form.getVersion() + ""));
            form.renameGridTableStmt(form.getVersion() + "").forEach(stmt -> {
                jdbcTemplate.execute(stmt);
            });
            form.renameGridHistoryTableStmt(form.getVersion() + "").forEach(stmt -> {
                jdbcTemplate.execute(stmt);
            });
            if (form.getType().equalsIgnoreCase("master"))
                jdbcTemplate.execute(form.renameMasterTableStmt(form.getVersion() + ""));

            return save(form);
        } else {
            return formRepository.save(form);
        }
    }

    public Optional<Form> getFormByWorkflowId(Integer workflowId) {
        return formRepository.findByWorkflowId(workflowId);
    }
}
