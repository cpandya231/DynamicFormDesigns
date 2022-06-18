package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.*;
import ai.smartfac.logever.model.LogEntry;
import ai.smartfac.logever.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/entry")
public class FormDataController {

    @Autowired
    FormService formService;

    @Autowired
    UserService userService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    RoleService roleService;

    @Autowired
    FormDataService formDataService;

    @PostMapping("/{form}")
    public ResponseEntity<?> logEntry(@PathVariable(name = "form") String form,@RequestBody LogEntry logEntry) {
        Optional<Form> existingForm = formService.getFormByName(form);
        if(existingForm.isPresent()) {
            existingForm.get().getWorkflow().getStates().forEach(state->System.out.println((state.getName())));
            Optional<State> state = existingForm.get().getWorkflow().getStates().stream().filter(st->st.getName().equals(logEntry.getState())).findFirst();
            if(state.isPresent()) {
                Set<Department> departments = state.get().getDepartments();
                Set<Role> roles = state.get().getRoles();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User loggedInUser = userService.getUserByUsername(auth.getPrincipal().toString()).get();
                if(departmentService.checkAccess(loggedInUser.getDepartment(),departments) ||
                roleService.hasAccess(loggedInUser.getRoles(), roles)) {
                    Map<String, String> values = logEntry.getData();
                    values.put("state",logEntry.getState());
                    values.put("created_by",loggedInUser.getUsername());
                    formDataService.insertInto(existingForm.get(),values);
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not have access to log this entry!");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT,"This state does not exist for given form!");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Form does not exist!");
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{form}")
    public ResponseEntity<?> updateLogEntry(@PathVariable(name = "form") String form,@RequestBody LogEntry logEntry) {
        Optional<Form> existingForm = formService.getFormByName(form);
        if(existingForm.isPresent()) {
            existingForm.get().getWorkflow().getStates().forEach(state->System.out.println((state.getName())));
            System.out.println(logEntry.getState());
            Optional<State> state = existingForm.get().getWorkflow().getStates().stream().filter(st->st.getName().equals(logEntry.getState())).findFirst();
            if(state.isPresent()) {
                Set<Department> departments = state.get().getDepartments();
                Set<Role> roles = state.get().getRoles();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User loggedInUser = userService.getUserByUsername(auth.getPrincipal().toString()).get();
                if(departmentService.checkAccess(loggedInUser.getDepartment(),departments) ||
                        roleService.hasAccess(loggedInUser.getRoles(), roles)) {
                    Map<String, String> values = logEntry.getData();
                    values.put("state",logEntry.getState());
                    values.put("created_by",loggedInUser.getUsername());
                    System.out.println(existingForm.get().makeInsertValuesStmt(values));
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not have access to log this entry!");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT,"This state does not exist for given form!");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Form does not exist!");
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
