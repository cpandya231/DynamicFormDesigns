package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.RolePermission;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.model.RolePermissionModel;
import ai.smartfac.logever.model.RolePermissionRequest;
import ai.smartfac.logever.service.RolePermissionService;
import ai.smartfac.logever.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import javax.transaction.Transactional;
import java.util.Optional;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*")
public class RolePermissionsController {

    @Autowired
    RoleService roleService;

    @Autowired
    RolePermissionService rolePermissionService;

    @GetMapping("/")
    public ResponseEntity<?> getRoles() {
        Iterable<Role> roles = roleService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @GetMapping("/{role}/")
    public ResponseEntity<?> getRole(@PathVariable(name = "role") String role) {
        Optional<Role> queriedRole = roleService.findByRole(role);
        Role foundRole = queriedRole.orElseThrow(()->new RuntimeException("Role not found"));

        rolePermissionService.getRolePermissions(foundRole.getId()).forEach(f->System.out.println(f.getPermission()));

        return new ResponseEntity<>(foundRole, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/save")
    public ResponseEntity<?> saveRolePermission(@RequestBody RolePermissionRequest request) {
        RolePermissionModel model = new RolePermissionModel(request);
        Role role = roleService.save(model.getRole());
        Iterable<RolePermission> rolePermissions = null;
        if(role != null) {
            rolePermissions = rolePermissionService.saveAll(model.getRolePermissions(role.getId()));
        }
        if(rolePermissions != null) {
            return new ResponseEntity<>("Role and Permissions created",HttpStatus.CREATED);
        }
        return new ResponseEntity<>("Some error occurred while saving role and permissions",HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
