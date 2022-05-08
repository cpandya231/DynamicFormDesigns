package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.Department;
import ai.smartfac.logever.entity.Form;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DepartmentRepository extends CrudRepository<Department,Integer> {
    public Optional<Department> findByName(String name);
}
