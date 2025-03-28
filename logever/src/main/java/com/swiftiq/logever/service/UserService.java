package com.swiftiq.logever.service;

import com.swiftiq.logever.kraftos.entity.Role;
import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.kraftos.RoleRepository;
import com.swiftiq.logever.kraftos.UserRepository;
import com.swiftiq.logever.projection.UserLeast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        System.out.println(event.getAuthentication().getName());
        String userName = event.getAuthentication().getName();
        User user = this.userRepository.findByUsername(userName).get();
        user.setLastLoginDt(new Timestamp(System.currentTimeMillis()));
        user.setUpdatedBy(user.getUsername());
        String sqlStmt = "update kraftos.user set last_login_dt='"+new Timestamp(System.currentTimeMillis()).toString()+"', updated_by='"+user.getUsername()+"'," +
                " update_dt='"+new Timestamp(System.currentTimeMillis()).toString()+"' where id="+user.getId();
        jdbcTemplate.execute(sqlStmt);
//        this.saveUser(user);
    }

    public void logout(User user) {
        String insertStmt = "INSERT INTO audit_trail(action,type,new_state,pk_value,username) VALUES('UPDATED','USER','" +
               user.getUsername() + " logged Out','"+user.getId()+"','"+user.getUsername()+"')";
        jdbcTemplate.execute(insertStmt);
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User newUser) {
        Set<Role> roles = new HashSet<>();
        newUser.getRoles().stream().forEach(role->roles.add(roleRepository.findById(role.getId()).get()));
        newUser.setRoles(roles);

        return userRepository.save(newUser);
    }

    public User getUserByEmployeeCode(String employeeCode) {
        return userRepository.findByEmployeeCode(employeeCode).get();
    }

    public Optional<User> getUserById(int employeeId) {
        return userRepository.findById(employeeId);
    }

    public Iterable<UserLeast> getAllUsersLeast() {
        return userRepository.findAllUsersBy();
    }
}
