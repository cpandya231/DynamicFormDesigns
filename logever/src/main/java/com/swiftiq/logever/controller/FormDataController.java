package com.swiftiq.logever.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftiq.logever.builder.entity.EmailDetails;
import com.swiftiq.logever.builder.entity.Form;
import com.swiftiq.logever.builder.entity.State;
import com.swiftiq.logever.kraftos.entity.Department;
import com.swiftiq.logever.kraftos.entity.Notification;
import com.swiftiq.logever.kraftos.entity.Role;
import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.main.entity.PendingEntry;
import com.swiftiq.logever.model.DataQuery;
import com.swiftiq.logever.model.Entry;
import com.swiftiq.logever.model.Filter;
import com.swiftiq.logever.model.LogEntry;
import com.swiftiq.logever.service.*;
import com.unboundid.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    AppService appService;

//    @Autowired
//    PendingEntryService pendingEntryService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/{formId}")
    public ResponseEntity<?> logEntry(@PathVariable(name = "formId") int formId, @RequestPart("body") String logEntryStr,
                                      @RequestPart(name = "files", required = false) MultipartFile[] files) {
        try {
            LogEntry logEntry = new ObjectMapper().readValue(logEntryStr,LogEntry.class);
            Optional<Form> existingForm = formService.getFormById(formId);
            String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
            User user = userService.getUserByUsername(username).get();
            Map<String, String> values = logEntry.getData();
            values.put("state", logEntry.getState());
            values.put("created_by", username);
            values.put("endState", logEntry.isEndState() ? "true" : "false");
            Entry entry = formDataService.insertInto(existingForm.get(), values, files);
            formDataService.insertInto(existingForm.get(), logEntry.getGridData(), entry);
            Form form = existingForm.get();
            String appName = appService.getById(form.getAppId()).get().getName();
//            String appName = form.getType().equalsIgnoreCase("master") ? "TruData" : "Logever";
            Map<String, String> payload = new HashMap<>();
            payload.put("msg","REQ-"+entry.getEntryId()+" for "+form.getName() + " awaiting your action!");
//            State nextState = form.getWorkflow().getStates().stream().filter(st->st.getName().equals(logEntry.getState())).findFirst().get();
            List<Notification> notifications = new ArrayList<>();

            if (logEntry.getState().endsWith("-INPA")) {
                notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user.getUsername(), null, null, payload, "LOW", user.getUsername()));
                notificationService.saveAll(notifications);
            } else {
                State nextState = form.getWorkflow().getStates().stream().filter(st->st.getName().equals(logEntry.getState())).findFirst().get();
                System.out.println(nextState.getName());
                if (nextState.getUserAccessField() == null || nextState.getUserAccessField().trim().length() == 0) {
                    if (!logEntry.isEndState() && !logEntry.getState().endsWith("-INPA")) {
                        formService.getRolesForState(nextState).forEach(r -> {
                            System.out.println("Adding entries for role "+r.getRole());
                            if (!r.getRole().equalsIgnoreCase("Initiator")) {
                                if (!r.getRole().equalsIgnoreCase("Initiator Manager")) {
                                    if (!r.getRole().equalsIgnoreCase("Initiator HOD")) {
                                        formService.getDepartmentsForState(nextState).forEach(d -> {
                                            if (d.getName().equalsIgnoreCase("Initiator Department")) {
                                                departmentService.getAllUnder(user.getDepartment()).forEach(aD -> {
                                                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", null, r.getId(), aD.getId(), payload, "LOW", values.get("created_by")));
                                                });
                                            } else {
                                                departmentService.getAllUnder(d).forEach(aD -> {
                                                    System.out.println("Adding entries for all department under "+d.getName());
                                                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", null, r.getId(), aD.getId(), payload, "LOW", values.get("created_by")));
                                                });
                                            }
                                        });
                                    } else {
                                        if (username.equalsIgnoreCase(user.getDepartment().getHod())) {
                                            if (user.getDepartment().getParentId() > 0) {
                                                Department parentDept = departmentService.getDepartmentById(user.getDepartment().getParentId()).get();
                                                notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", parentDept.getHod(), null, null, payload, "LOW", values.get("created_by")));
                                            } else {
                                                if (user.getDepartment().getDesignee1() != null && !user.getDepartment().getDesignee1().isEmpty())
                                                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user.getDepartment().getDesignee1(), null, null, payload, "LOW", values.get("created_by")));
                                                if (user.getDepartment().getDesignee2() != null && !user.getDepartment().getDesignee2().isEmpty())
                                                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user.getDepartment().getDesignee2(), null, null, payload, "LOW", values.get("created_by")));
                                            }
                                        } else
                                            notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user.getDepartment().getHod(), null, null, payload, "LOW", values.get("created_by")));
                                        if (user.getDepartment().getDesignee1() != null && !user.getDepartment().getDesignee1().isEmpty())
                                            notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user.getDepartment().getDesignee1(), null, null, payload, "LOW", values.get("created_by")));
                                        if (user.getDepartment().getDesignee2() != null && !user.getDepartment().getDesignee2().isEmpty())
                                            notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user.getDepartment().getDesignee2(), null, null, payload, "LOW", values.get("created_by")));
                                    }
                                } else {
                                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user.getReporting_manager(), null, null, payload, "LOW", values.get("created_by")));
                                }
                            } else {
                                notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user.getUsername(), null, null, payload, "LOW", values.get("created_by")));
                            }
                        });
                        notificationService.saveAll(notifications);
                    }
                } else if (!logEntry.isEndState()) {notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", values.get(nextState.getUserAccessField()), null, null, payload, "LOW", values.get("created_by")));
                    notificationService.saveAll(notifications);
                }

                try {
                    EmailDetails details = new EmailDetails();
                    details.setSubject("Swift-IQ | " + appName + " : " + existingForm.get().getName() + " : New Request Created");
                    details.setRecipient(user.getEmail());
                    details.setMsgBody("Hello " + user.getFirst_name() + ",\n\nYour request has been submitted!\n\nThanks & Regards,\nTeam Digitedgy.");
                    emailService.sendSimpleMail(details);

                    if (nextState.isSendNotification()) {
                        notifications.forEach(pendingEntry -> {
                            if (pendingEntry.getUser() != null && !pendingEntry.getUser().equalsIgnoreCase("")) {
                                details.setSubject("Swift-IQ | "+appName+" : " + existingForm.get().getName() + " : Request for action");
                                User emailUser = userService.getUserByUsername(pendingEntry.getUser()).get();
                                details.setRecipient(emailUser.getEmail());
                                details.setMsgBody("Hello " + emailUser.getFirst_name() + ",\n\nThere is a pending request for you to act upon!\n\nThanks & Regards,\nTeam Digitedgy.");
                                emailService.sendSimpleMail(details);
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{formId}")
    public ResponseEntity<?> updateLogEntry(@PathVariable(name = "formId") int formId, @RequestPart("body") String logEntryStr,
                                            @RequestPart(name = "files", required = false) MultipartFile[] files) {
        try {
            LogEntry logEntry = new ObjectMapper().readValue(logEntryStr,LogEntry.class);
            Optional<Form> existingForm = formService.getFormById(formId);
            Form form = existingForm.get();
            String user = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
            User initiator = userService.getUserByUsername(logEntry.getInitiator()).get();
            Map<String, String> values = logEntry.getData();
            values.put("state", logEntry.getState());
            values.put("updated_by", user);
            values.put("id", logEntry.getId() + "");
            values.put("log_entry_id", logEntry.getId() + "");
            values.put("endState", logEntry.isEndState() ? "true" : "false");
            values.remove("log_update_dt");
            String appName = appService.getById(form.getAppId()).get().getName();
//            String appName = form.getType().equalsIgnoreCase("master") ? "TruData" : "Logever";
            Entry entry = formDataService.update(form, values, files);
            Map<String, String> payload = new HashMap<>();
            payload.put("msg","REQ-"+entry.getEntryId()+" for "+form.getName() + " awaiting your action!");
            formDataService.update(form, logEntry.getGridData(), entry);
            notificationService.deleteFormEntryNotification(appName, form.getId()+"", logEntry.getId()+"");

            List<Notification> notifications = new ArrayList<>();

            if (logEntry.getState().endsWith("-INPA")) {
                notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", user, null, null, payload, "LOW", initiator.getUsername()));
                notificationService.saveAll(notifications);
            } else {
                State nextState = form.getWorkflow().getStates().stream().filter(st -> st.getName().equals(logEntry.getState())).findFirst().get();
                if (nextState.getUserAccessField() == null || nextState.getUserAccessField().trim().length() == 0) {
                    if (!logEntry.isEndState() && !logEntry.getState().endsWith("-INPA")) {
                        formService.getRolesForState(nextState).forEach(r -> {
                            if (!r.getRole().equalsIgnoreCase("Initiator")) {
                                if (!r.getRole().equalsIgnoreCase("Initiator Manager")) {
                                    if (!r.getRole().equalsIgnoreCase("Initiator HOD")) {
                                        formService.getDepartmentsForState(nextState).forEach(d -> {
                                            departmentService.getAllUnder(d).forEach(aD -> {
                                                notifications.add(new Notification(appName, form.getId()+"", logEntry.getId()+"", null, r.getId(), aD.getId(), payload, "LOW", initiator.getUsername()));
                                            });
                                        });
                                    } else {
                                        if (initiator.getUsername().equalsIgnoreCase(initiator.getDepartment().getHod())) {
                                            if (initiator.getDepartment().getParentId() > 0) {
                                                Department parentDept = departmentService.getDepartmentById(initiator.getDepartment().getParentId()).get();
                                                notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", parentDept.getHod(), null, null, payload, "LOW", initiator.getUsername()));
                                            } else {
                                                if (initiator.getDepartment().getDesignee1() != null && !initiator.getDepartment().getDesignee1().isEmpty())
                                                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", initiator.getDepartment().getDesignee1(), null, null, payload, "LOW", initiator.getUsername()));
                                                if (initiator.getDepartment().getDesignee2() != null && !initiator.getDepartment().getDesignee2().isEmpty())
                                                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", initiator.getDepartment().getDesignee2(), null, null, payload, "LOW", initiator.getUsername()));
                                            }
                                        } else
                                            notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", initiator.getDepartment().getHod(), null, null, payload, "LOW", initiator.getUsername()));
                                        if (initiator.getDepartment().getDesignee1() != null && !initiator.getDepartment().getDesignee1().isEmpty())
                                            notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", initiator.getDepartment().getDesignee1(), null, null, payload, "LOW", initiator.getUsername()));
                                        if (initiator.getDepartment().getDesignee2() != null && !initiator.getDepartment().getDesignee2().isEmpty())
                                            notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", initiator.getDepartment().getDesignee2(), null, null, payload, "LOW", initiator.getUsername()));
                                    }
                                } else {
                                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", initiator.getReporting_manager(), null, null, payload, "LOW", initiator.getUsername()));
                                }
                            } else {
                                notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", initiator.getUsername(), null, null, payload, "LOW", initiator.getUsername()));
                            }
                        });
                        notificationService.saveAll(notifications);
                    }
                } else if (!logEntry.isEndState()) {
                    notifications.add(new Notification(appName, form.getId()+"", entry.getEntryId()+"", values.get(nextState.getUserAccessField()), null, null, payload, "LOW", initiator.getUsername()));
                    notificationService.saveAll(notifications);
                }

                if(nextState.isSendNotification()){
                    EmailDetails details = new EmailDetails();
                    notifications.forEach(pendingEntry -> {
                        if(pendingEntry.getUser() != null && !pendingEntry.getUser().equalsIgnoreCase("")) {
                            details.setSubject("DigitEdgy | Flux-Intelligent : " + existingForm.get().getName() + " : Request for action");
                            User emailUser = userService.getUserByUsername(pendingEntry.getUser()).get();
                            details.setRecipient(emailUser.getEmail());
                            details.setMsgBody("Hello " + emailUser.getFirst_name() + ",\n\nThere is a pending request for you to act upon!\n\nThanks & Regards,\nTeam Digitedgy.");
                            emailService.sendSimpleMail(details);
                        }
                    });
                }
            }

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/get/{formId}")
    public ResponseEntity<?> getLogEntries(@PathVariable(name = "formId") int formId,
                                           @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
                                           @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                           @RequestParam(name = "entryId", required = false, defaultValue = "-1") int entryId,
                                           @RequestParam(name = "filterByUsername", required = false, defaultValue = "false") boolean filterByUsername,
                                           @RequestParam(name = "filterByDepartment", required = false, defaultValue = "false") boolean filterByDepartment,
                                           @RequestBody List<Filter> filters) {
        Optional<Form> existingForm = formService.getFormById(formId);

        DataQuery dataQueried;

        dataQueried = formDataService.getAllFor(existingForm.get(), entryId, true,
                filterByDepartment, pageNumber, pageSize, filters);

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @PostMapping("/{formId}/last-state/")
    public ResponseEntity<?> getLastStateLogEntries(@PathVariable(name = "formId") int formId,
                                            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
                                            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                           @RequestParam(name = "entryId", required = false, defaultValue = "-1") int entryId,
                                           @RequestParam(name = "filterByUsername", required = false, defaultValue = "false") boolean filterByUsername,
                                           @RequestParam(name = "filterByDepartment", required = false, defaultValue = "false") boolean filterByDepartment,
                                                    @RequestBody List<Filter> filters) {
        Optional<Form> existingForm = formService.getFormById(formId);
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();
        if(StreamSupport.stream(formService.getLastStateAccessibleForms(user).spliterator(),false).filter(f->f.getName().equalsIgnoreCase(existingForm.get().getName())).count() > 0) {
            DataQuery dataQueried;
            dataQueried = formDataService.getAllFor(existingForm.get(), entryId, false, filterByDepartment,
                    pageNumber, pageSize, filters);
            return new ResponseEntity<>(dataQueried, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{formId}/pending")
    public ResponseEntity<?> getPendingLogEntries(@PathVariable(name = "formId") int formId) {
        Optional<Form> existingForm = formService.getFormById(formId);

        List<HashMap> dataQueried;
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();
        dataQueried = formDataService.getAllPendingFor(existingForm.get(),user);

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @GetMapping("/logever/all-pending")
    public ResponseEntity<?> getLogeverPendingForms() {
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();

        return new ResponseEntity<>(notificationService.findAllRelevantForApp(user, "Logever"), HttpStatus.OK);
    }

    @GetMapping("/trudata/all-pending")
    public ResponseEntity<?> getTruDataPendingForms() {
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();

        return new ResponseEntity<>(notificationService.findAllRelevantForApp(user, "TruData"), HttpStatus.OK);
    }

    @GetMapping("/metadata/{formId}/{entryId}")
    public ResponseEntity<?> getLogEntryMetadata(@PathVariable(name = "formId") int formId,
                                                 @PathVariable(name = "entryId") int entryId) {
        Optional<Form> existingForm = formService.getFormById(formId);

        var dataQueried = formDataService.getLogEntryMetadata(existingForm.get(), entryId);

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @GetMapping("/grid/metadata/{formId}/{entryId}")
    public ResponseEntity<?> getLogEntryGridMetadata(@PathVariable(name = "formId") int formId,
                                                 @PathVariable(name = "entryId") int entryId) {
        Optional<Form> existingForm = formService.getFormById(formId);

        var dataQueried = formDataService.getLogEntryGridMetadata(existingForm.get(), entryId);

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
                Set<Department> departments = formService.getDepartmentsForState(state);
                Set<Role> roles = formService.getRolesForState(state);
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
