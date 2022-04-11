package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.RolePermission;
import ai.smartfac.logever.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.Optional;

public interface RolePermissionRepository extends CrudRepository<RolePermission, Integer> {
    Iterable<RolePermission> findByRoleId(Integer roleId);
}
