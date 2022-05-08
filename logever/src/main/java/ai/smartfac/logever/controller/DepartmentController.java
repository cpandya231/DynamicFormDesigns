package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Department;
import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/departments/")
public class DepartmentController {

    @Autowired
    DepartmentService departmentService;
    @GetMapping("/")
    public ResponseEntity<?> getDepartments() {
        Iterable<Department> departments = departmentService.getDepartments();
        return new ResponseEntity<>(departments, HttpStatus.OK);
    }

    @GetMapping("/{department}/")
    public ResponseEntity<?> getDepartmentByName(@PathVariable(name = "department") String department) {
        Optional<Department> queriedDepartment = departmentService.getDepartmentByName(department);
        Department foundDepartment = queriedDepartment.orElseThrow(()->new RuntimeException("Department not found"));
        return new ResponseEntity<>(foundDepartment, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveDepartment(@RequestBody Department department) {
        Department savedDepartment = departmentService.save(department);
        return new ResponseEntity<>(savedDepartment, HttpStatus.OK);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateDepartment(@RequestBody Department department) {
        Optional<Department> existingDepartment = departmentService.getDepartmentById(department.getId());
        Department updatedDepartment = null;
        if(existingDepartment.isPresent()) {
            if(department.getName()!=null && !department.getName().equals(existingDepartment.get().getName()))
                existingDepartment.get().setName(department.getName());
            if(department.getCode()!=null && !department.getCode().equals(existingDepartment.get().getCode()))
                existingDepartment.get().setCode(department.getCode());
            if(department.getParentId()!=null && !department.getParentId().equals(existingDepartment.get().getParentId()))
                existingDepartment.get().setParentId(department.getParentId());
            updatedDepartment = departmentService.save(existingDepartment.get());
        }
        else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Department does not exist!");
        }
        return new ResponseEntity<>(updatedDepartment,HttpStatus.OK);
    }
}
