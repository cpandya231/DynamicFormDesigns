package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Permission;
import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.repository.PermissionRepository;
import ai.smartfac.logever.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionService {

    @Autowired
    PermissionRepository permissionRepository;

    public Optional<Permission> getPermissionById(Integer id) {
        return permissionRepository.findById(id);
    }

    public Iterable<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Optional<Permission> findByPermission(String permission) { return permissionRepository.findByPermission(permission); }

    public Permission save(Permission permission) {
        Optional<Permission> existingPermission = permissionRepository.findByPermission(permission.getPermission());
        if(existingPermission.isEmpty()) {
            return permissionRepository.save(permission);
        } else {
            existingPermission.get().setPermission(permission.getPermission());
            return permissionRepository.save(existingPermission.get());
        }
    }
}
