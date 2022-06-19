package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.*;
import ai.smartfac.logever.model.LogEntry;
import ai.smartfac.logever.service.*;
import org.apache.juli.logging.Log;
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

    @PostMapping("/{formId}")
    public ResponseEntity<?> logEntry(@PathVariable(name = "formId") int formId,@RequestBody LogEntry logEntry) {
        Optional<Form> existingForm = formService.getFormById(formId);
        String user = checkAccess(existingForm,logEntry);
        Map<String, String> values = logEntry.getData();
        values.put("state",logEntry.getState());
        values.put("created_by",user);
        formDataService.insertInto(existingForm.get(),values);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{formId}")
    public ResponseEntity<?> updateLogEntry(@PathVariable(name = "formId") int formId,@RequestBody LogEntry logEntry) {
        Optional<Form> existingForm = formService.getFormById(formId);
        String user = checkAccess(existingForm, logEntry);

        Map<String, String> values = logEntry.getData();
        values.put("state", logEntry.getState());
        values.put("updated_by", user);
        values.put("id", logEntry.getId() + "");
        formDataService.update(existingForm.get(), values);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private String checkAccess(Optional<Form> existingForm, LogEntry logEntry) {
        if(existingForm.isPresent()) {
            Optional<State> state = existingForm.get().getWorkflow().getStates().stream().filter(st->st.getName().equals(logEntry.getState())).findFirst();
            if(state.isPresent()) {
                Set<Department> departments = state.get().getDepartments();
                Set<Role> roles = state.get().getRoles();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User loggedInUser = userService.getUserByUsername(auth.getPrincipal().toString()).get();
                if(departmentService.checkAccess(loggedInUser.getDepartment(),departments) ||
                        roleService.hasAccess(loggedInUser.getRoles(), roles)) {
                    return loggedInUser.getUsername();
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not have access to log this entry!");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT,"This state does not exist for given form!");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Form does not exist!");
        }
    }

}
