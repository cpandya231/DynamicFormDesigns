package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Permission;
import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
