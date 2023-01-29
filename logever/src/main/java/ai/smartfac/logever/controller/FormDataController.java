package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.*;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.model.LogEntry;
import ai.smartfac.logever.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    TransitionService transitionService;

    @Autowired
    FormDataService formDataService;

    @Value("${logever.initiator.department}")
    private String initiatorDept;

    @Value("${logever.initiator.manager.role}")
    private String initiatorManagerRole;

    @Value("${logever.initiator.role}")
    private String initiatorRole;

    @PostMapping("/{formId}")
    public ResponseEntity<?> logEntry(@PathVariable(name = "formId") int formId, @RequestBody LogEntry logEntry) {
        Optional<Form> existingForm = formService.getFormById(formId);
        String user = checkAccess(existingForm, logEntry);

        User currUser = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();

        State state = existingForm.get().getWorkflow().getStates().stream().filter(st->st.getName().equalsIgnoreCase(logEntry.getState())).findFirst().get();
        String assignedUser = "";
        String assignedRoles = state.getRoles().stream().map(r->r.getId()+"").collect(Collectors.joining(","));
        String assignedDepartments = state.getDepartments().stream().map(dpt->dpt.getId()+"").collect(Collectors.joining(","));
        if(state.getRoles().stream().anyMatch(role -> role.getRole().equalsIgnoreCase(initiatorRole))){
            assignedUser = user;
        }
        if(state.getRoles().stream().filter(role->role.getRole().equalsIgnoreCase(initiatorRole)).count() > 0) {
            assignedRoles += ","+currUser.getRoles().stream().map(role -> role.getId()+"").collect(Collectors.joining(","));
        }
        if(state.getDepartments().stream().filter(dept->dept.getName().equalsIgnoreCase(initiatorDept)).count() > 0) {
            assignedDepartments += ","+currUser.getDepartment().getId();
        }

        Map<String, String> values = logEntry.getData();
        values.put("state", logEntry.getState());
        values.put("assigned_user",assignedUser);
        values.put("assigned_role",assignedRoles);
        values.put("assigned_dept",assignedDepartments);
        values.put("created_by", user);
        values.put("endState", logEntry.isEndState() ? "true" : "false");
        formDataService.insertInto(existingForm.get(), values);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{formId}")
    public ResponseEntity<?> updateLogEntry(@PathVariable(name = "formId") int formId, @RequestBody LogEntry logEntry) {
        Optional<Form> existingForm = formService.getFormById(formId);
        DataQuery dataQueried = formDataService.getAllForWithAssignments(existingForm.get(), logEntry.getId(), false, false).get(0);
        String user = checkNewAccess(existingForm, dataQueried);
        User currUser = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();
        State state = existingForm.get().getWorkflow().getStates().stream().filter(st->st.getName().equalsIgnoreCase(logEntry.getState())).findFirst().get();
        String assignedUser = "";
        String assignedRoles = state.getRoles().stream().map(r->r.getId()+"").collect(Collectors.joining(","));
        String assignedDepartments = state.getDepartments().stream().map(dpt->dpt.getId()+"").collect(Collectors.joining(","));
        if(state.getRoles().stream().anyMatch(role -> role.getRole().equalsIgnoreCase(initiatorRole))){
            assignedUser = dataQueried.getData().get("created_by");
        }
        if(state.getRoles().stream().filter(role->role.getRole().equalsIgnoreCase(initiatorRole)).count() > 0) {
            assignedRoles += ","+currUser.getRoles().stream().map(role -> role.getId()+"").collect(Collectors.joining(","));
        }
        if(state.getDepartments().stream().filter(dept->dept.getName().equalsIgnoreCase(initiatorDept)).count() > 0) {
            assignedDepartments += ","+currUser.getDepartment().getId();
        }

        Map<String, String> values = logEntry.getData();
        values.put("state", logEntry.getState());
        values.put("updated_by", user);
        values.put("id", logEntry.getId() + "");
        values.put("log_entry_id", logEntry.getId() + "");
        values.put("assigned_user",assignedUser);
        values.put("assigned_role",assignedRoles);
        values.put("assigned_dept",assignedDepartments);
        values.put("endState", logEntry.isEndState() ? "true" : "false");
        formDataService.update(existingForm.get(), values);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{formId}")
    public ResponseEntity<?> getLogEntries(@PathVariable(name = "formId") int formId,
                                           @RequestParam(name = "entryId", required = false, defaultValue = "-1") int entryId,
                                           @RequestParam(name = "filterByUsername", required = false, defaultValue = "false") boolean filterByUsername,
                                           @RequestParam(name = "filterByDepartment", required = false, defaultValue = "false") boolean filterByDepartment) {
        Optional<Form> existingForm = formService.getFormById(formId);

        List<DataQuery> dataQueried;
        String user = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User currUser = userService.getUserByUsername(user).get();
        dataQueried = formDataService.getAllForUser(existingForm.get(), currUser);

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @GetMapping("/{formId}/{entryId}")
    public ResponseEntity<?> getLogEntriesById(@PathVariable(name = "formId") int formId,
                                               @PathVariable(name = "entryId") int entryId) {
        Optional<Form> existingForm = formService.getFormById(formId);

        List<DataQuery> dataQueried;
        dataQueried = formDataService.getAllFor(existingForm.get(), entryId,false,false);

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }
    @GetMapping("/metadata/{formId}/{entryId}")
    public ResponseEntity<?> getLogEntryMetadata(@PathVariable(name = "formId") int formId,
                                                 @PathVariable(name = "entryId") int entryId) {
        Optional<Form> existingForm = formService.getFormById(formId);

        var dataQueried = formDataService.getLogEntryMetadata(existingForm.get(), entryId);

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @PostMapping("/metadata/{formId}/{entryId}")
    public ResponseEntity<?> saveLogEntryMetadata(@PathVariable(name = "formId") int formId,
                                                  @PathVariable(name = "entryId") int entryId,
                                                  @RequestBody Map<String, String> metaDataValues) {
        Optional<Form> existingForm = formService.getFormById(formId);
        String user = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        metaDataValues.put("log_entry_id", entryId + "");
        metaDataValues.put("created_by", user);
        formDataService.saveLogEntryMetadata(existingForm.get(), metaDataValues);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{formId}/filtered/")
    public ResponseEntity<?> getFilteredLogEntries(@PathVariable(name = "formId") int formId,
                                                   @RequestParam(name = "filters") String filters) {
        Optional<Form> existingForm = formService.getFormById(formId);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formId);
        }

        var dataQueried = formDataService.getAllFor(existingForm.get(), Arrays.stream(filters.split(";")).collect(Collectors.toMap(cond->cond.split(":")[0],cond->cond.split(":")[1])));

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    private String checkNewAccess(Optional<Form> existingForm, DataQuery dataQueried) {
        String user = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User currUser = userService.getUserByUsername(user).get();

        String assignedToUser = dataQueried.getData().getOrDefault("assigned_user","");
        String assignedToRoles = dataQueried.getData().getOrDefault("assigned_role","");
        String assignedToDepartments = dataQueried.getData().getOrDefault("assigned_dept","");

        boolean deptCheck = Arrays.stream(assignedToDepartments.split(",")).filter(dpt->dpt.equalsIgnoreCase(currUser.getDepartment().getId()+"")).count() > 0 || assignedToDepartments.length() == 0;
        boolean roleCheck = Arrays.stream(assignedToRoles.split(",")).filter(role-> {return currUser.getRoles().stream().filter(r->r.getId().toString().equalsIgnoreCase(role)).count()>0;}).count() > 0 || assignedToRoles.length() == 0;

        if(assignedToUser.equalsIgnoreCase(user)) {
            return user;
        } else if(deptCheck && roleCheck) {
            return user;
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not have access to log this entry!");
        }
    }

    private String checkAccess(Optional<Form> existingForm, LogEntry logEntry) {
        if (existingForm.isPresent()) {
            Optional<State> state = existingForm.get().getWorkflow().getStates().stream().filter(st -> st.getName().equals(logEntry.getState())).findFirst();
            if (state.isPresent()) {
                List<State> accessibleStates = getAccessibleWriteStates(existingForm, logEntry.getState());
                if (accessibleStates.contains(state.get())) {
                    return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not have access to log this entry!");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This state does not exist for given form!");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Form does not exist!");
        }
    }

    private List<State> getAccessibleWriteStates(Optional<Form> existingForm, String state) {
        Map<State, State> transitions = new HashMap<>();
        List<State> accessibleStates = getAccessibleReadStates(existingForm);
        transitionService.getWorkflowTransitions(existingForm.get().getWorkflow().getId())
                .forEach(transition -> transitions.put(transition.getToState(), transition.getFromState()));
        List<State> matchingState = transitions.entrySet().stream().filter(kv -> kv.getKey().getName().equals(state) && accessibleStates.contains(kv.getValue()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        return matchingState;
    }

    private List<State> getAccessibleReadStates(Optional<Form> existingForm) {
        if (existingForm.isPresent()) {
            Set<State> formStates = existingForm.get().getWorkflow().getStates();
            return formStates.stream().filter(state -> {
                Set<Department> departments = state.getDepartments();
                Set<Role> roles = state.getRoles();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User loggedInUser = userService.getUserByUsername(auth.getPrincipal().toString()).get();
                if (departmentService.checkAccess(loggedInUser.getDepartment(), departments) ||
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
