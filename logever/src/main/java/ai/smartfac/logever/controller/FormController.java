package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.service.FormService;
import ai.smartfac.logever.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/forms")
public class FormController {

    @Autowired
    FormService formService;

    @Autowired
    UserService userService;

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
        form.setVersion(1);
        Form savedForm = formService.save(form);
        return new ResponseEntity<>(savedForm, HttpStatus.OK);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateForm(@RequestBody Form form) {
        Optional<Form> existingForm = formService.getFormById(form.getId());
        Form updatedForm = null;
        if (existingForm.isPresent()) {
            String prevColumns = existingForm.get().getColumns();
            existingForm.get().setVersion(existingForm.get().getVersion() + 1);
            if (form.getName() != null && !form.getName().equals(existingForm.get().getName()))
                existingForm.get().setName(form.getName());
            if (form.getTemplate() != null && !form.getTemplate().equals(existingForm.get().getTemplate()))
                existingForm.get().setTemplate(form.getTemplate());

            updatedForm = formService.update(existingForm.get(), prevColumns);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Form does not exist!");
        }
        return new ResponseEntity<>(updatedForm, HttpStatus.OK);
    }
}
