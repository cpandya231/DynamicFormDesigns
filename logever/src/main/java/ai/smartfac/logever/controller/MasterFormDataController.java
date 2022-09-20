package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

        var dataQueried = masterFormDataService.getAllFor(existingForm.get(), column, columnValue, "entry_state,metadata");

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @GetMapping("/{formName}/new/")
    public ResponseEntity<?> getFilteredLogEntries(@PathVariable(name = "formName") String formName,
                                           @RequestParam(name = "filters") String filters) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        var dataQueried = masterFormDataService.getAllFor(existingForm.get(), Arrays.stream(filters.split(";")).collect(Collectors.toMap(cond->cond.split(":")[0],cond->cond.split(":")[1])), "entry_state,metadata");

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @PutMapping("/{formName}")
    public ResponseEntity<?> updateMasterEntryState(@PathVariable(name = "formName") String formName,
                                                    @RequestParam(name = "id") String masterTableEntryId,
                                                    @RequestParam(name = "stateValue") String stateValue,
                                                    @RequestBody Map<String, String> value ) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        masterFormDataService.updateEntryState(existingForm.get(), masterTableEntryId, stateValue,value);

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
