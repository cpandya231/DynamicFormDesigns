package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Department;
import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.model.FormTemplate;
import ai.smartfac.logever.service.FormService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/forms")
public class FormController {

    @Autowired
    FormService formService;

    @GetMapping("/")
    public ResponseEntity<?> getForms() {
        Iterable<Form> forms = formService.getForms();
        return new ResponseEntity<>(forms, HttpStatus.OK);
    }

    @GetMapping("/{form}/")
    public ResponseEntity<?> getFormByName(@PathVariable(name = "form") String form) {
        Optional<Form> queriedForm = formService.getFormByName(form);
        Form foundForm = queriedForm.orElseThrow(()->new RuntimeException("Form not found"));
        foundForm.makeCreateTableStmt();
        return new ResponseEntity<>(foundForm, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveForm(@RequestBody Form form) {
        Form savedForm = formService.save(form);
        return new ResponseEntity<>(savedForm, HttpStatus.OK);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateForm(@RequestBody Form form) {
        Optional<Form> existingForm = formService.getFormById(form.getId());
        Form updatedForm = null;
        if(existingForm.isPresent()) {
            if(form.getName()!=null && !form.getName().equals(existingForm.get().getName()))
                existingForm.get().setName(form.getName());
            if(form.getTemplate()!=null && !form.getTemplate().equals(existingForm.get().getTemplate()))
                existingForm.get().setTemplate(form.getTemplate());

            updatedForm = formService.save(existingForm.get());
        }
        else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Form does not exist!");
        }
        return new ResponseEntity<>(updatedForm,HttpStatus.OK);
    }
}
