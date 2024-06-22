package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.repository.RoleRepository;
import ai.smartfac.logever.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        String sqlStmt = "update user set last_login_dt='"+new Timestamp(System.currentTimeMillis()).toString()+"', updated_by='"+user.getUsername()+"'," +
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
}
