package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.Permission;
import ai.smartfac.logever.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PermissionRepository extends CrudRepository<Permission,Integer> {
    Optional<Permission> findByPermission(String permission);
}
