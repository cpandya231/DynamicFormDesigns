package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> getRole(@PathVariable(name = "form") String form) {
        Optional<Form> queriedForm = formService.getFormByName(form);
        Form foundForm = queriedForm.orElseThrow(()->new RuntimeException("Form not found"));
        return new ResponseEntity<>(foundForm, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveRolePermission(@RequestBody Form form) {
        Form savedForm = formService.save(form);
        return new ResponseEntity<>(savedForm, HttpStatus.OK);
    }
}
