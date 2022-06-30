package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.*;
import ai.smartfac.logever.model.DataQuery;
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

import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping("/{formId}")
    public ResponseEntity<?> getLogEntries(@PathVariable(name="formId") int formId,
                                           @RequestParam(name="entryId",required = false,defaultValue = "-1") int entryId) {
        Optional<Form> existingForm = formService.getFormById(formId);
        List<State> accessibleStates = getAccessibleStates(existingForm);
        List<DataQuery> dataQueried = null;
        if(accessibleStates.size()>0)
            dataQueried = formDataService.getAllFor(existingForm.get(),accessibleStates,entryId);

        return new ResponseEntity<>(dataQueried,HttpStatus.OK);
    }

    private String checkAccess(Optional<Form> existingForm, LogEntry logEntry) {
        if(existingForm.isPresent()) {
            Optional<State> state = existingForm.get().getWorkflow().getStates().stream().filter(st->st.getName().equals(logEntry.getState())).findFirst();
            if(state.isPresent()) {
                List<State> accessibleStates = getAccessibleStates(existingForm);
                if(accessibleStates.contains(state.get())) {
                    return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
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

    private List<State> getAccessibleStates(Optional<Form> existingForm) {
        if(existingForm.isPresent()) {
            Set<State> formStates = existingForm.get().getWorkflow().getStates();
            return formStates.stream().filter(state-> {
                Set<Department> departments = state.getDepartments();
                Set<Role> roles = state.getRoles();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User loggedInUser = userService.getUserByUsername(auth.getPrincipal().toString()).get();
                if(departmentService.checkAccess(loggedInUser.getDepartment(),departments) ||
                        roleService.hasAccess(loggedInUser.getRoles(), roles)) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Form does not exist!");
        }
    }

}
