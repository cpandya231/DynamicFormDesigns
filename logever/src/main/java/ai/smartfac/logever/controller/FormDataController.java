package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.*;
import ai.smartfac.logever.model.*;
import ai.smartfac.logever.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    PendingEntryService pendingEntryService;

    @PostMapping("/{formId}")
    public ResponseEntity<?> logEntry(@PathVariable(name = "formId") int formId, @RequestBody LogEntry logEntry) {
        Optional<Form> existingForm = formService.getFormById(formId);
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();
        Map<String, String> values = logEntry.getData();
        values.put("state", logEntry.getState());
        values.put("created_by", SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        values.put("endState", logEntry.isEndState() ? "true" : "false");
        Entry entry = formDataService.insertInto(existingForm.get(), values);
        formDataService.insertInto(existingForm.get(), logEntry.getGridData(), entry);
        if(!logEntry.isEndState() && !logEntry.getState().endsWith("-INPA")) {
            Form form = existingForm.get();
            State nextState = form.getWorkflow().getStates().stream().filter(st->st.getLabel().equals(logEntry.getState())).findFirst().get();
            List<PendingEntry> pendingEntries = new ArrayList<>();
            nextState.getRoles().forEach(r->{
                if(!r.getRole().equalsIgnoreCase("Initiator")) {
                    nextState.getDepartments().forEach(d-> {
                        if(d.getName().equalsIgnoreCase("Initiator Department")) {
                            departmentService.getAllUnder(user.getDepartment()).forEach(aD-> {
                                pendingEntries.add(new PendingEntry(form.getId(),entry.getEntryId(),null,r.getId(),aD.getId()));
                            });
                        } else {
                            departmentService.getAllUnder(d).forEach(aD-> {
                                pendingEntries.add(new PendingEntry(form.getId(),entry.getEntryId(),null,r.getId(),aD.getId()));
                            });
                        }
                    });
                } else {
                    pendingEntries.add(new PendingEntry(form.getId(),entry.getEntryId(),user.getUsername(),null,null));
                }
            });
            pendingEntryService.saveAll(pendingEntries);
        } else if(logEntry.getState().endsWith("-INPA")) {
            List<PendingEntry> pendingEntries = new ArrayList<>();
            pendingEntries.add(new PendingEntry(existingForm.get().getId(),entry.getEntryId(),user.getUsername(),null,null));
            pendingEntryService.saveAll(pendingEntries);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{formId}")
    public ResponseEntity<?> updateLogEntry(@PathVariable(name = "formId") int formId, @RequestBody LogEntry logEntry) {
        Optional<Form> existingForm = formService.getFormById(formId);
        String user = checkAccess(existingForm, logEntry);

        Map<String, String> values = logEntry.getData();
        values.put("state", logEntry.getState());
        values.put("updated_by", user);
        values.put("id", logEntry.getId() + "");
        values.put("log_entry_id", logEntry.getId() + "");
        values.put("endState", logEntry.isEndState() ? "true" : "false");
        values.remove("log_update_dt");
        Entry entry = formDataService.update(existingForm.get(), values);
        formDataService.update(existingForm.get(), logEntry.getGridData(),entry);
        pendingEntryService.removeAllFor(existingForm.get().getId(),logEntry.getId());
        if(!logEntry.isEndState() && !logEntry.getState().endsWith("-INPA")) {
            Form form = existingForm.get();
            State nextState = form.getWorkflow().getStates().stream().filter(st->st.getLabel().equals(logEntry.getState())).findFirst().get();
            List<PendingEntry> pendingEntries = new ArrayList<>();
            nextState.getRoles().forEach(r->{
                nextState.getDepartments().forEach(d-> {
                    departmentService.getAllUnder(d).forEach(aD-> {
                        pendingEntries.add(new PendingEntry(form.getId(),logEntry.getId(),null,r.getId(),aD.getId()));
                    });
                });
            });
            pendingEntryService.saveAll(pendingEntries);
        } else if(logEntry.getState().endsWith("-INPA")) {
            List<PendingEntry> pendingEntries = new ArrayList<>();
            pendingEntries.add(new PendingEntry(existingForm.get().getId(),entry.getEntryId(),user,null,null));
            pendingEntryService.saveAll(pendingEntries);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{formId}")
    public ResponseEntity<?> getLogEntries(@PathVariable(name = "formId") int formId,
                                           @RequestParam(name = "entryId", required = false, defaultValue = "-1") int entryId,
                                           @RequestParam(name = "filterByUsername", required = false, defaultValue = "false") boolean filterByUsername,
                                           @RequestParam(name = "filterByDepartment", required = false, defaultValue = "false") boolean filterByDepartment) {
        Optional<Form> existingForm = formService.getFormById(formId);

        List<DataQuery> dataQueried;

        dataQueried = formDataService.getAllFor(existingForm.get(), entryId, true, filterByDepartment);

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @GetMapping("/{formId}/pending")
    public ResponseEntity<?> getPendingLogEntries(@PathVariable(name = "formId") int formId) {
        Optional<Form> existingForm = formService.getFormById(formId);

        List<DataQuery> dataQueried;
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();
        dataQueried = formDataService.getAllPendingFor(existingForm.get(),user);

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @GetMapping("/all-pending")
    public ResponseEntity<?> getPendingForms() {
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();

        return new ResponseEntity<>(pendingEntryService.getAllForUser(user), HttpStatus.OK);
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

    private String checkAccess(Optional<Form> existingForm, LogEntry logEntry) {
//        if (existingForm.isPresent()) {
//            Optional<State> state = existingForm.get().getWorkflow().getStates().stream().filter(st -> st.getName().equals(logEntry.getState())).findFirst();
//            if (state.isPresent()) {
//                List<State> accessibleStates = getAccessibleWriteStates(existingForm, logEntry.getState());
//                if (accessibleStates.contains(state.get())) {
//                    return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//                } else {
//                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not have access to log this entry!");
//                }
//            } else {
//                throw new ResponseStatusException(HttpStatus.CONFLICT, "This state does not exist for given form!");
//            }
//        } else {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Form does not exist!");
//        }
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
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

    @GetMapping("/grid/{formId}/{logEntryId}")
    private ResponseEntity<?> getGridDataFor(@PathVariable(name = "formId") int formId,
                                               @PathVariable(name = "logEntryId") int logEntryId) {
        Optional<Form> existingForm = formService.getFormById(formId);
        return new ResponseEntity<>(formDataService.getGridsFor(existingForm.get(),logEntryId), HttpStatus.OK);
    }
}
