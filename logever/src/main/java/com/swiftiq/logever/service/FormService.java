package com.swiftiq.logever.service;

import com.swiftiq.logever.builder.StateDepartmentRepository;
import com.swiftiq.logever.builder.StateRoleRepository;
import com.swiftiq.logever.builder.entity.Form;
import com.swiftiq.logever.builder.entity.State;
import com.swiftiq.logever.builder.entity.StateDepartment;
import com.swiftiq.logever.kraftos.entity.Department;
import com.swiftiq.logever.kraftos.entity.Role;
import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.builder.FormRepository;
import com.swiftiq.logever.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FormService {

    @Autowired
    StateRoleRepository stateRoleRepository;

    @Autowired
    StateDepartmentRepository stateDepartmentRepository;

    @Autowired
    FormRepository formRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    RoleService roleService;

    public Set<Department> getDepartmentsForState(State state) {
        return StreamSupport.stream(stateDepartmentRepository.findAllByStateId(state.getId()+"")
                .spliterator(),false)
                .map(stateDepartment -> {
                    return departmentService.getDepartmentById(stateDepartment.getDepartmentId()).get();
                })
                .collect(Collectors.toSet());
    }

    public Set<Role> getRolesForState(State state) {
        return StreamSupport.stream(stateRoleRepository.findAllByStateId(state.getId()+"")
                        .spliterator(),false)
                .map(stateRole -> {
                    return roleService.getRoleById(stateRole.getRoleId()).get();
                })
                .collect(Collectors.toSet());
    }

    public Optional<Form> getFormById(Integer id) {
        return formRepository.findById(id).stream().map(form -> {
            form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                st.setRoles(roles);
                st.setDepartments(depts);
                return st;
            }).collect(Collectors.toSet()));
            return form;
        }).findFirst();
    }

    public Iterable<Form> getInitiatableForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        return StreamSupport.stream(forms.spliterator(),false)
                .filter(f->f.getStatus().equalsIgnoreCase("Published"))
                .filter(form-> {
            State firstState = form.getWorkflow().getStates().stream().filter(st->st.isFirstState()).findFirst().get();
            Set<Department> authDepts = getDepartmentsForState(firstState);
            Set<Role> authRoles = getRolesForState(firstState);
            return (authDepts.size() == 0 || departmentService.checkAccess(user.getDepartment(), authDepts)) && (authRoles.size()==0 || roleService.hasAccess(user
                    .getRoles(),authRoles));
        }).map(form -> {
            form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                st.setRoles(roles);
                st.setDepartments(depts);
                return st;
            }).collect(Collectors.toSet()));
            return form;
        }).collect(Collectors.toList());
    }

    public Iterable<Form> getAccessibleForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        return StreamSupport.stream(forms.spliterator(),false)
                .filter(f->f.getStatus().equalsIgnoreCase("Published")).filter(form-> {
            return form.getWorkflow().getStates().stream().filter(st -> {
                Set<Department> authDepts = getDepartmentsForState(st);
                boolean initDeptCheck = authDepts.stream().filter(d->d.getName().equalsIgnoreCase("Initiator Department")).count() > 0;
                Set<Role> authRoles = getRolesForState(st);
                System.out.println(departmentService.checkAccess(user.getDepartment(), authDepts));
                return (authDepts.size() == 0 || initDeptCheck || departmentService.checkAccess(user.getDepartment(), authDepts)) && (authRoles.size() == 0 || roleService.hasAccess(user
                        .getRoles(), authRoles));
            }).count() > 0;
        }).map(form -> {
            form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                st.setRoles(roles);
                st.setDepartments(depts);
                return st;
            }).collect(Collectors.toSet()));
            return form;
        }).collect(Collectors.toList());
    }

    public Iterable<Form> getLastStateAccessibleForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        return StreamSupport.stream(forms.spliterator(),false).filter(form-> {
            return form.getWorkflow().getStates().stream().filter(st->st.isEndState()).filter(st -> {
                Set<Department> authDepts = getDepartmentsForState(st);
                boolean initDeptCheck = authDepts.stream().filter(d->d.getName().equalsIgnoreCase("Initiator Department")).count() > 0;
                Set<Role> authRoles = getRolesForState(st);
                return (authDepts.size() == 0 || initDeptCheck || departmentService.checkAccess(user.getDepartment(), authDepts)) && (authRoles.size() == 0 || roleService.hasAccess(user
                        .getRoles(), authRoles));
            }).count() > 0;
        }).map(form -> {
            form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                st.setRoles(roles);
                st.setDepartments(depts);
                return st;
            }).collect(Collectors.toSet()));
            return form;
        }).collect(Collectors.toList());
    }

    public Iterable<Form> getForms(User user) {
        Iterable<Form> forms = formRepository.findAll();
        if (user.getAuthorities().stream().filter(auth -> auth.getAuthority().equals("OS_ADMIN")).count() > 0 || 1==1)
            return StreamSupport.stream(forms.spliterator(), false).map(form -> {
                form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                    Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                    Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                    st.setRoles(roles);
                    st.setDepartments(depts);
                    return st;
                }).collect(Collectors.toSet()));
                return form;
            }).collect(Collectors.toList());
        else {
            return StreamSupport.stream(forms.spliterator(), false)
                    .filter(form ->
                            form.getWorkflow().getStates().stream()
                                    .anyMatch(state -> getDepartmentsForState(state).contains(user.getDepartment())
                                            || !Collections.disjoint(getRolesForState(state), user.getRoles())))
                    .map(form -> {
                        form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                            Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                            Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                            st.setRoles(roles);
                            st.setDepartments(depts);
                            return st;
                        }).collect(Collectors.toSet()));
                        return form;
                    }).collect(Collectors.toList());
        }
    }

    public Optional<Form> getFormByName(String name) {

        return formRepository.findByName(name).stream().map(form -> {
            form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                st.setRoles(roles);
                st.setDepartments(depts);
                return st;
            }).collect(Collectors.toSet()));
            return form;
        }).findFirst();
    }

    public Form save(Form form) {
//        if (form.getVersion() == 1 && form.getId() == null) {
        if(form.getStatus().equalsIgnoreCase("Published")) {
            jdbcTemplate.execute(form.makeCreateTableStmt(form.getType().equalsIgnoreCase("master")));
            jdbcTemplate.execute(form.makeCreateMetaDataTableStmt());
            form.makeCreateGridTableStmt().forEach(stmt -> {
                jdbcTemplate.execute(stmt);
            });
            form.makeCreateGridHistoryTableStmt().forEach(stmt -> {
                jdbcTemplate.execute(stmt);
            });
            if (form.getType().equalsIgnoreCase("master"))
                jdbcTemplate.execute(form.makeCreateMasterTableStmt());
        }
        form.setColumns(form.getColumns());
        return formRepository.save(form);
//        }
//        return form;
    }

    public Iterable<Form> getFormsByApp(Integer appId) {
        return StreamSupport.stream(formRepository.findAllByAppId(appId).spliterator(), false).map(form -> {
            form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                st.setRoles(roles);
                st.setDepartments(depts);
                return st;
            }).collect(Collectors.toSet()));
            return form;
        }).collect(Collectors.toList());
    }

    public Form justSave(Form form) {
        return formRepository.save(form);
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "logeverTransactionManager")
    public Form saveAndHandleTransactionColumns(Form form, String prevTColumns, String newTColumns) {
        String dropColumns = Arrays.stream(prevTColumns.split(","))
                .filter(p-> Arrays.stream(newTColumns.split(","))
                        .filter(n-> {
                            System.out.println("Comparing "+n+" with "+p);
                            return n.equals(p) || p.isEmpty();
                        }).count() == 0)
                .map(c->"DROP COLUMN "+c)
                .collect(Collectors.joining(","));
        String addColumns = Arrays.stream(newTColumns.split(",")).filter(n-> Arrays.stream(prevTColumns.split(",")).filter(p-> p.equals(n) || n.isEmpty()).count() == 0)
                .map(n->"ADD COLUMN "+n+" text")
                .collect(Collectors.joining(","));

        Table table = new Table();
        table.setName(form.getMasterTableName());
        String alterStmt = "ALTER TABLE " + table.getName()+" "+ addColumns + "" +(addColumns.length() > 0 && dropColumns.length() > 0 ? "," : " ") + dropColumns;
        System.out.println(alterStmt);
        jdbcTemplate.execute(alterStmt);
        return formRepository.save(form);
    }

    public Form update(Form form, boolean rename) {
//        String alterStmt = form.makeAlterTableStmt(prevColumns);
//        String alterTableMetaDataStmt = form.makeAlterTableMetaDataStmt(prevColumns);
//        if (alterStmt.length() > 0) {
//            if (form.getType().equalsIgnoreCase("master")) {
//                String alterMasterTableStmt = form.makeAlterMasterTableStmt(prevColumns);
//                jdbcTemplate.execute(alterMasterTableStmt);
//            }
//            jdbcTemplate.execute(alterStmt);
//            jdbcTemplate.execute(alterTableMetaDataStmt);
//
//        }
        if(form.getStatus().equalsIgnoreCase("Published")) {
            if(rename) {
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
            }
            return save(form);
        } else {
            return formRepository.save(form);
        }
    }

    public Iterable<Form> getAll() {
        return formRepository.findAll();
    }

    public Optional<Form> getFormByWorkflowId(Integer workflowId) {
        return formRepository.findByWorkflowId(workflowId).stream().map(form -> {
            form.getWorkflow().setStates(form.getWorkflow().getStates().stream().map(st-> {
                Set<Role> roles = stateRoleRepository.findAllByStateId(st.getId()+"").stream().map(c->roleService.getRoleById(c.getRoleId()).get()).collect(Collectors.toSet());
                Set<Department> depts = stateDepartmentRepository.findAllByStateId(st.getId()+"").stream().map(c -> departmentService.getDepartmentById(c.getDepartmentId()).get()).collect(Collectors.toSet());
                st.setRoles(roles);
                st.setDepartments(depts);
                return st;
            }).collect(Collectors.toSet()));
            return form;
        }).findFirst();
    }
}
