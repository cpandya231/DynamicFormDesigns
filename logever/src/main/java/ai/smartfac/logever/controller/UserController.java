package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
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

    @PostMapping("/")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Optional<User> existingUser = userService.getUserByUsername(user.getUsername());
        User newUser;
        if(existingUser.isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            newUser = userService.saveUser(user);
        }
        else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"User already exists!");
        }
        return new ResponseEntity<>(newUser,HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        Optional<User> existingUser = userService.getUserByUsername(user.getUsername());
        User updatedUser;
        boolean userUpdated = false;
        if(!existingUser.isEmpty()) {
            if(user.getDepartment()!=null && !user.getDepartment().isEmpty() && !existingUser.get().getDepartment().equals(user.getDepartment())) {
                existingUser.get().setDepartment(user.getDepartment());
                userUpdated = true;
            }
            if(user.getEmail()!=null && !user.getEmail().isEmpty() && !existingUser.get().getEmail().equals(user.getEmail())) {
                existingUser.get().setEmail(user.getEmail());
                userUpdated = true;
            }
            if(user.getPassword()!=null && !user.getPassword().isEmpty()
                    && !bCryptPasswordEncoder.matches(user.getPassword(),existingUser.get().getPassword())) {
                existingUser.get().setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                existingUser.get().setLastPasswordChangedDate(new Date(System.currentTimeMillis()));
                userUpdated = true;
            }
            if(userUpdated)
                updatedUser = userService.saveUser(existingUser.get());
            else
                updatedUser = existingUser.get();
        }
        else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"User does not exist!");
        }
        return new ResponseEntity<>(updatedUser,HttpStatus.OK);
    }

    @PatchMapping("/")
    public ResponseEntity<?> toggleIsActive(@RequestBody User user) {
        Optional<User> existingUser = userService.getUserByUsername(user.getUsername());
        User updatedUser;
        if(!existingUser.isEmpty()) {
            existingUser.get().setIsActive(user.getIsActive());
            updatedUser = userService.saveUser(existingUser.get());
        }
        else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"User does not exist!");
        }
        return new ResponseEntity<>(updatedUser,HttpStatus.OK);
    }
}
