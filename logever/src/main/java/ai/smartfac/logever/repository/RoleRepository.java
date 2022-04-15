package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role,Integer> {
    Optional<Role> findByRole(String role);
}
