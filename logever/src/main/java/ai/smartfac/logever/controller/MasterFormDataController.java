package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/master/entry")
public class MasterFormDataController {

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
    MasterFormDataService masterFormDataService;


    @GetMapping("/{formName}")
    public ResponseEntity<?> getLogEntries(@PathVariable(name = "formName") String formName,
                                           @RequestParam(name = "column", required = false) String column,
                                           @RequestParam(name = "columnValue", required = false) String columnValue) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        var dataQueried = masterFormDataService.getAllFor(existingForm.get(), column, columnValue, "");

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @PatchMapping("/{formName}")
    public ResponseEntity<?> updateMasterEntryState(@PathVariable(name = "formName") String formName,
                                                    @RequestParam(name = "id") String masterTableEntryId,
                                                    @RequestParam(name = "stateValue") String stateValue) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        masterFormDataService.updateEntryState(existingForm.get(), masterTableEntryId, stateValue);

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
