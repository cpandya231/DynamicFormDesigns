package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Department;
import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class DepartmentService {

    @Autowired
    DepartmentRepository departmentRepository;

    public Optional<Department> getDepartmentById(Integer id) {
        return departmentRepository.findById(id);
    }

    public Iterable<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentByName(String name) {
        return departmentRepository.findByName(name);
    }

    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    public boolean underADepartment(Department parentDepartment, Department childDepartment) {
        Iterable<Department> departments = getDepartments();
        Map<Integer, Department> depts = new HashMap<>();
        departments.forEach(dept->depts.put(dept.getId(),dept));
        Department checkDept = childDepartment;
        while(checkDept.getParentId() != 0) {
            checkDept = depts.get(checkDept.getParentId());
            if(checkDept.getId() == parentDepartment.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean checkAccess(Department department, Set<Department> authorizedDepartments) {
        if(authorizedDepartments.contains(department)) {
            return true;
        } else {
            for (Department authorizedDepartment : authorizedDepartments) {
                if(underADepartment(authorizedDepartment,department)) {
                    return true;
                }
            }
        }
        return false;
    }
}
