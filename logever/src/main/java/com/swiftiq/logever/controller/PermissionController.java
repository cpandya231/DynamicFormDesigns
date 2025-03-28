package com.swiftiq.logever.controller;

import com.swiftiq.logever.kraftos.entity.Permission;
import com.swiftiq.logever.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {

    @Autowired
    PermissionService permissionService;

    @GetMapping("/")
    public ResponseEntity<?> getRoles() {
        Iterable<Permission> permissions = permissionService.getAllPermissions();
        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

    @GetMapping("/{permission}/")
    public ResponseEntity<?> getRole(@PathVariable(name = "permission") String permission) {
        Optional<Permission> queriedPermission = permissionService.findByPermission(permission);
        Permission foundPermission = queriedPermission.orElseThrow(()->new RuntimeException("Permission not found"));

        return new ResponseEntity<>(foundPermission, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> savePermission(@RequestBody Permission permission) {
        Permission savedPermission = permissionService.save(permission);
        return new ResponseEntity<>(savedPermission, HttpStatus.OK);
    }

}
