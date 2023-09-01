package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/custom")
@CrossOrigin(origins = "*")
public class CustomController {

    @Autowired
    UserService userService;

    @GetMapping("/user-id-names")
    public ResponseEntity<?> getUsers() {
        Iterable<User> users = userService.getAllUsers();
        return new ResponseEntity<>(StreamSupport.stream(users.spliterator(),false)
                .map(user->user.getId()+"|"+user.getFirst_name()+" "+user.getLast_name())
                .collect(Collectors.toList()), HttpStatus.OK);
    }
}
