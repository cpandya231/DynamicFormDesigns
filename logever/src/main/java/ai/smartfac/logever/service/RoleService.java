package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    public Iterable<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> findByRole(String role) { return roleRepository.findByRole(role); }

    public Role save(Role role) {
        return roleRepository.save(role);
    }
}
