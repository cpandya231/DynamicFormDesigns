package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.RolePermission;
import ai.smartfac.logever.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionService {

    @Autowired
    RolePermissionRepository rolePermissionRepository;

    public Iterable<RolePermission> getRolePermissions(Integer roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    public Iterable<RolePermission> getAllRolePermissions() {
        return rolePermissionRepository.findAll();
    }

    public Iterable<RolePermission> saveAll(Iterable<RolePermission> rolePermissions) {
        return rolePermissionRepository.saveAll(rolePermissions);
    }
}
