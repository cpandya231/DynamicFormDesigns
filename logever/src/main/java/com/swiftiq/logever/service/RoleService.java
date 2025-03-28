package com.swiftiq.logever.service;

import com.swiftiq.logever.kraftos.entity.Role;
import com.swiftiq.logever.kraftos.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    public Optional<Role> getRoleById(Integer id) {
        return roleRepository.findById(id);
    }

    public Iterable<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> findByRole(String role) { return roleRepository.findByRole(role); }

    public Role save(Role role) {
        //role.setPermissions(role.getPermissions());
        //role.getPermissions().stream().forEach(f->System.out.println(f.getPermission()));
        return roleRepository.save(role);
    }

    public boolean hasAccess(Set<Role> userRoles, Set<Role> authorizedRoles) {
        for (Role userRole : userRoles) {
            if(authorizedRoles.stream().filter(r->r.getRole().equalsIgnoreCase(userRole.getRole())).count() > 0) {
                return true;
            }
        }
        return false;
    }
}
