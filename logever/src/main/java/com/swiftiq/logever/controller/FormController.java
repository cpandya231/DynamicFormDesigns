package com.swiftiq.logever.controller;

import com.google.gson.Gson;
import com.swiftiq.logever.builder.entity.Form;
import com.swiftiq.logever.kraftos.entity.Notification;
import com.swiftiq.logever.model.FormReviewRequest;
import com.swiftiq.logever.model.FormSettings;
import com.swiftiq.logever.service.FormService;
import com.swiftiq.logever.service.NotificationService;
import com.swiftiq.logever.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/forms")
public class FormController {

    @Value("${qa.role.id}") private Integer qaRole;

    @Autowired
    FormService formService;

    @Autowired
    UserService userService;

    @Autowired
    NotificationService notificationService;

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getFormById(@PathVariable(name = "id") Integer id) {
        Optional<Form> form = formService.getFormById(id);
        return new ResponseEntity<>(form, HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<?> getForms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Iterable<Form> forms = formService.getForms(userService.getUserByUsername(authentication.getPrincipal().toString()).get());
        return new ResponseEntity<>(forms, HttpStatus.OK);
    }

    @GetMapping("/init-forms/")
    public ResponseEntity<?> getInitiatableForms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Iterable<Form> forms = formService.getInitiatableForms(userService.getUserByUsername(authentication.getPrincipal().toString()).get());
        return new ResponseEntity<>(forms, HttpStatus.OK);
    }

    @GetMapping("/accessible-forms/")
    public ResponseEntity<?> getAccessibleForms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Iterable<Form> forms = formService.getAccessibleForms(userService.getUserByUsername(authentication.getPrincipal().toString()).get());
        return new ResponseEntity<>(forms, HttpStatus.OK);
    }

    @GetMapping("/last-state-accessible-forms/")
    public ResponseEntity<?> getLastStateAccessibleForms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Iterable<Form> forms = formService.getLastStateAccessibleForms(userService.getUserByUsername(authentication.getPrincipal().toString()).get());
        return new ResponseEntity<>(forms, HttpStatus.OK);
    }

    @GetMapping("/{form}/")
    public ResponseEntity<?> getFormByName(@PathVariable(name = "form") String form) {
        Optional<Form> queriedForm = formService.getFormByName(form);
        Form foundForm = queriedForm.orElseThrow(() -> new RuntimeException("Form not found"));
//        foundForm.makeCreateTableStmt();
        return new ResponseEntity<>(foundForm, HttpStatus.OK);
    }

    @GetMapping("/{appId}")
    public ResponseEntity<?> getFormByName(@PathVariable(name = "appId") Integer appId) {
        Iterable<Form> queriedForms = formService.getFormsByApp(appId);
        return new ResponseEntity<>(queriedForms, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveForm(@RequestBody Form form) {
        Optional<Form> queriedForm = formService.getFormByName(form.getName());
        if(queriedForm.isPresent()){
            throw new RuntimeException("Form already present");
        }
        form.setQaApprovalStatus("TBD");
        form.setVersion(1);
        Form savedForm = formService.save(form);
        if(form.getStatus().equalsIgnoreCase("In Review")) {
            Map<String, String> payload = new HashMap<>();
            payload.put("appId",form.getAppId()+"");
            payload.put("formId", savedForm.getId()+"");
            payload.put("msg",form.getName() + " log ready to be reviewed!");
            payload.put("type","LogReviewReq");
            notificationService.save(new Notification("Builder", savedForm.getId()+"",savedForm.getAppId()+"", null, qaRole, 1, payload, "LOW", savedForm.getCreatedBy()));
        }
        return new ResponseEntity<>(savedForm, HttpStatus.OK);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateForm(@RequestBody Form form) {
        Optional<Form> existingForm = formService.getFormById(form.getId());
        Form updatedForm = null;
        if (existingForm.isPresent()) {
            String prevColumns = existingForm.get().getColumns();
            String status = existingForm.get().getStatus();
            if(status.equalsIgnoreCase("Published") && !form.getStatus().equalsIgnoreCase("Published")) {
                form.setVersion(form.getVersion()+1);
                if(form.getName().equalsIgnoreCase(existingForm.get().getName()))
                    form.setName(form.getName()+" v"+form.getVersion());
                form.setId(null);
                updatedForm = formService.save(form);
                if(form.getStatus().equalsIgnoreCase("In Review")) {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("appId",form.getAppId()+"");
                    payload.put("formId", updatedForm.getId()+"");
                    payload.put("msg",form.getName() + " log ready to be reviewed!");
                    payload.put("type","LogReviewReq");
                    notificationService.save(new Notification("Builder", updatedForm.getId()+"",updatedForm.getAppId()+"", null, qaRole, 1, payload, "LOW", updatedForm.getCreatedBy()));
                }
            } else {
                if (form.getStatus() != null && !form.getStatus().equals(existingForm.get().getStatus())) {
                    existingForm.get().setStatus(form.getStatus());
                }if (form.getName() != null && !form.getName().equals(existingForm.get().getName()))
                    existingForm.get().setName(form.getName());
                if (form.getTemplate() != null && !form.getTemplate().equals(existingForm.get().getTemplate()))
                    existingForm.get().setTemplate(form.getTemplate());
                if (form.getAppId() != null && !form.getAppId().equals(existingForm.get().getAppId()))
                    existingForm.get().setAppId(form.getAppId());
                if(prevColumns.equalsIgnoreCase(form.getColumns())) {
                    updatedForm = formService.update(existingForm.get(), false);
                } else {
                    existingForm.get().setColumns(form.getColumns());
                    updatedForm = formService.update(existingForm.get(), true);
                }
                if(form.getStatus().equalsIgnoreCase("Published")) {
                    notificationService.deleteAllByAppAndId("Builder", updatedForm.getId()+"");
                } else if(form.getStatus().equalsIgnoreCase("In Review")) {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("appId",form.getAppId()+"");
                    payload.put("formId", updatedForm.getId()+"");
                    payload.put("msg",form.getName() + " log ready to be reviewed!");
                    payload.put("type","LogReviewReq");
                    notificationService.save(new Notification("Builder", updatedForm.getId()+"", updatedForm.getAppId()+"", null, qaRole, 1, payload, "LOW", updatedForm.getCreatedBy()));
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Form does not exist!");
        }
        return new ResponseEntity<>(updatedForm, HttpStatus.OK);
    }

    @PutMapping("/review/")
    public ResponseEntity<?> review(@RequestBody FormReviewRequest formReviewRequest) {
        Optional<Form> existingForm = formService.getFormById(formReviewRequest.getFormId());
        if(existingForm.isPresent()) {
            Form form = existingForm.get();
            if(formReviewRequest.getProcess().equalsIgnoreCase("QaApproval")) {
                form.setQaApprovalStatus(formReviewRequest.isApproved() ? "Approved" : "Rejected");
                form.setQaApprovalDt(new Timestamp((new Date()).getTime()));
                form.setQaApprover(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
                form.setQaApproverComments(formReviewRequest.getComments());
                form.setStatus("Approved");
                Form savedForm = formService.save(form);
                if(form.getStatus().equalsIgnoreCase("Approved")) {
                    notificationService.deleteAllByAppAndId("Builder", savedForm.getId()+"");
                    Map<String, String> payload = new HashMap<>();
                    payload.put("appId",form.getAppId()+"");
                    payload.put("formId", savedForm.getId()+"");
                    payload.put("msg",form.getName() + " log approved and ready to be published!");
                    payload.put("type","LogReviewPositive");
                    notificationService.save(new Notification("Builder", savedForm.getId()+"",savedForm.getAppId()+"", form.getCreatedBy(), null, null, payload, "LOW", savedForm.getUpdatedBy()));
                } else {
                    notificationService.deleteAllByAppAndId("Builder", savedForm.getId()+"");
                    Map<String, String> payload = new HashMap<>();
                    payload.put("appId",form.getAppId()+"");
                    payload.put("formId", savedForm.getId()+"");
                    payload.put("msg",form.getName() + " log rejected. See review comments!");
                    payload.put("type","LogReviewNegative");
                    notificationService.save(new Notification("Builder", savedForm.getId()+"",savedForm.getAppId()+"", form.getCreatedBy(), null, null, payload, "LOW", savedForm.getUpdatedBy()));
                }
                return new ResponseEntity<>(savedForm, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateFormSettings(@RequestBody Form form) {
        Optional<Form> existingForm = formService.getFormById(form.getId());
        Form updatedForm = null;
        if (existingForm.isPresent()) {
            Gson gson = new Gson();
            FormSettings formSettings = gson.fromJson(existingForm.get().getSettings(), FormSettings.class);
            String prevTransactionColumns = formSettings == null ? "": formSettings.getTransactionColumns() == null ? "" : formSettings.getTransactionColumns();
            FormSettings updatedSettings = gson.fromJson(form.getSettings(), FormSettings.class);
            String transactionColumns = updatedSettings.getTransactionColumns() == null ? "" : updatedSettings.getTransactionColumns();
            if(prevTransactionColumns.equalsIgnoreCase(transactionColumns)) {
                existingForm.get().setSettings(form.getSettings());
                updatedForm = formService.justSave(existingForm.get());
            } else {
                existingForm.get().setSettings(form.getSettings());
                updatedForm = formService.saveAndHandleTransactionColumns(existingForm.get(), prevTransactionColumns, transactionColumns);
            }

        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Form does not exist!");
        }
        return new ResponseEntity<>(updatedForm, HttpStatus.OK);
    }

}
