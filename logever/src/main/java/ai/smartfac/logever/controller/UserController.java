package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/")
    public ResponseEntity<?> getUsers() {
        Iterable<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUsers(@PathVariable("username") String username) {
        Optional<User> user = userService.getUserByUsername(username);
        User resultUser = user.orElseThrow(()->new UsernameNotFoundException("User not found"));
        return new ResponseEntity<>(resultUser, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Optional<User> existingUser = userService.getUserByUsername(user.getUsername());
        User newUser;
        if(existingUser.isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            newUser = userService.saveUser(user);
        }
        else {
            existingUser.get().setDepartment(user.getDepartment());
            if(!bCryptPasswordEncoder.matches(user.getPassword(),existingUser.get().getPassword())) {
                existingUser.get().setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            }
            existingUser.get().setEmail(user.getEmail());
            existingUser.get().setFirst_name(user.getFirst_name());
            existingUser.get().setLast_name(user.getLast_name());
            existingUser.get().setRoles(user.getRoles());
            userService.saveUser(existingUser.get());
            newUser = existingUser.get();
        }
        return new ResponseEntity<>(newUser,HttpStatus.CREATED);
    }
}
