package ai.smartfac.logever.model;

import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.RolePermission;

import java.util.ArrayList;

public class RolePermissionModel
{
    Role role;
    ArrayList<RolePermission> rolePermissions;
    RolePermissionRequest rolePermissionRequest;

    public RolePermissionModel(RolePermissionRequest rolePermissionRequest) {
        this.rolePermissionRequest = rolePermissionRequest;
        role = new Role();
        role.setRole(rolePermissionRequest.getRole());
        role.setDescription(rolePermissionRequest.getDescription());
        rolePermissions = new ArrayList<>();
    }

    public Role getRole() {
        return role;
    }

    public ArrayList<RolePermission> getRolePermissions(Integer roleId) {
        rolePermissionRequest.getPermissions().stream().forEach(permission->rolePermissions.add(new RolePermission(roleId,permission)));
        return rolePermissions;
    }
}
