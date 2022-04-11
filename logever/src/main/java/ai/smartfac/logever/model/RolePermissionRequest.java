package ai.smartfac.logever.model;

import java.util.ArrayList;

public class RolePermissionRequest {
    String role;
    String description;
    ArrayList<String> permissions;

    public RolePermissionRequest(String role, String description, ArrayList<String> permissions) {
        this.role = role;
        this.permissions = permissions;
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ArrayList<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(ArrayList<String> permissions) {
        this.permissions = permissions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
