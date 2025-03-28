package com.swiftiq.logever.kraftos;

import com.swiftiq.logever.kraftos.entity.Department;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DepartmentRepository extends CrudRepository<Department,Integer> {
    public Optional<Department> findByName(String name);
}
