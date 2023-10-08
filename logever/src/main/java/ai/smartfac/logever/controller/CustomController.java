package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.model.MultiSelectResponse;
import ai.smartfac.logever.model.TextResponseModel;
import ai.smartfac.logever.service.CustomService;
import ai.smartfac.logever.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @GetMapping("/emp_dtl/{detail}")
    public ResponseEntity<?> getUserDetail(@PathVariable(name = "detail") String detail,
                                           @RequestParam(name = "employee_id", required = false) String employee_id) throws JsonProcessingException {
        User user = userService.getUserByEmployeeCode(employee_id);
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        JsonNode userNode = objectMapper.readTree(userString);
        System.out.println(userString);
        String response = userNode.at("/"+detail.replaceAll("\\.","\\/")).asText();
        TextResponseModel textResponse = new TextResponseModel(response);
        return new ResponseEntity<>(textResponse, HttpStatus.OK);
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

    @GetMapping("/existing_id/{applicationName}")
    public ResponseEntity<?> getExistingID(@PathVariable(name = "applicationName") String applicationName,
                                                    @RequestParam(name = "employeeID", required = false) String employeeID,
                                                    @RequestParam(name = "other_employee_id", required = false) String other_employee_id,
                                                    @RequestParam(name = "service_engineer_id", required = false) String service_engineer_id) {
        String idType = "";
        if (!employeeID.equalsIgnoreCase("undefined")) {
            return new ResponseEntity<>(customService.getExistingID(employeeID,applicationName), HttpStatus.OK);
        } else if(!other_employee_id.equalsIgnoreCase("undefined")) {
            return new ResponseEntity<>(customService.getExistingID(other_employee_id,applicationName), HttpStatus.OK);
        } else if(!service_engineer_id.equalsIgnoreCase("undefined")) {
            return new ResponseEntity<>(customService.getExistingID(service_engineer_id,applicationName), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}
