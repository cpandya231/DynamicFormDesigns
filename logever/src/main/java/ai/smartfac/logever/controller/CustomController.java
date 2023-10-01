package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.model.MultiSelectResponse;
import ai.smartfac.logever.service.CustomService;
import ai.smartfac.logever.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/custom")
@CrossOrigin(origins = "*")
public class CustomController {

    @Autowired
    UserService userService;

    @Autowired
    CustomService customService;

    @GetMapping("/user-id-names")
    public ResponseEntity<?> getUsers() {
        Iterable<User> users = userService.getAllUsers();
        return new ResponseEntity<>(StreamSupport.stream(users.spliterator(),false)
                .map(user->user.getId()+"|"+user.getFirst_name()+" "+user.getLast_name())
                .collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping("/required-roles/{applicationName}")
    public ResponseEntity<?> getRequiredRoles(@PathVariable(name = "applicationName") String applicationName) {
        ArrayList<String> all = new ArrayList<>();
        all.add("Vihit");
        all.add("Mohit");
        all.add("Chintan");
        all.add("Aakash");
        ArrayList<String> selected = new ArrayList<>();
        selected.add("Vihit");
        selected.add("Mohit");
        MultiSelectResponse multiSelectResponse = new MultiSelectResponse(all,selected);
        return new ResponseEntity<>(multiSelectResponse, HttpStatus.OK);
    }

    @GetMapping("/access_type/{applicationName}")
    public ResponseEntity<?> getRequiredAccessType(@PathVariable(name = "applicationName") String applicationName,
                                                   @RequestParam(name = "employeeID", required = false) String employeeID,
                                                   @RequestParam(name = "other_employee_id", required = false) String other_employee_id,
                                                   @RequestParam(name = "service_engineer_id", required = false) String service_engineer_id) {
        String idType = "";
        if (!employeeID.equalsIgnoreCase("undefined")) {
            idType = "employee_id";
            return new ResponseEntity<>(customService.getAllAccessTypes(employeeID,applicationName,idType), HttpStatus.OK);
        } else if(!other_employee_id.equalsIgnoreCase("undefined")) {
            idType = "other_employee_id";
            return new ResponseEntity<>(customService.getAllAccessTypes(other_employee_id,applicationName,idType), HttpStatus.OK);
        } else if(!service_engineer_id.equalsIgnoreCase("undefined")) {
            idType = service_engineer_id;
            return new ResponseEntity<>(customService.getAllAccessTypes(service_engineer_id,applicationName,idType), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/access_roles/{applicationName}")
    public ResponseEntity<?> getRequiredAccessRoles(@PathVariable(name = "applicationName") String applicationName,
                                                   @RequestParam(name = "employeeID", required = false) String employeeID,
                                                   @RequestParam(name = "other_employee_id", required = false) String other_employee_id,
                                                   @RequestParam(name = "service_engineer_id", required = false) String service_engineer_id) {
        String idType = "";
        if (!employeeID.equalsIgnoreCase("undefined")) {
            idType = "employee_id";
            return new ResponseEntity<>(customService.getAccessRoles(employeeID,applicationName,idType), HttpStatus.OK);
        } else if(!other_employee_id.equalsIgnoreCase("undefined")) {
            idType = "other_employee_id";
            return new ResponseEntity<>(customService.getAccessRoles(other_employee_id,applicationName,idType), HttpStatus.OK);
        } else if(!service_engineer_id.equalsIgnoreCase("undefined")) {
            idType = service_engineer_id;
            return new ResponseEntity<>(customService.getAccessRoles(service_engineer_id,applicationName,idType), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
