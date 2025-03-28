package com.swiftiq.logever.kraftos;

import com.swiftiq.logever.kraftos.entity.Permission;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PermissionRepository extends CrudRepository<Permission,Integer> {
    Optional<Permission> findByPermission(String permission);
}
