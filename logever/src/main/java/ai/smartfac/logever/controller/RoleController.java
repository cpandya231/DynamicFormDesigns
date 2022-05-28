package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    RoleService roleService;

    @GetMapping("/")
    public ResponseEntity<?> getRoles() {
        Iterable<Role> roles = roleService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @GetMapping("/{role}/")
    public ResponseEntity<?> getRole(@PathVariable(name = "role") String role) {
        Optional<Role> queriedRole = roleService.findByRole(role);
        Role foundRole = queriedRole.orElseThrow(()->new RuntimeException("Role not found"));

        //rolePermissionService.getRolePermissions(foundRole.getId()).forEach(f->System.out.println(f.getPermission()));

        return new ResponseEntity<>(foundRole, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveRolePermission(@RequestBody Role role) {
        Role savedRole = roleService.save(role);
        return new ResponseEntity<>(savedRole, HttpStatus.OK);
    }

}
