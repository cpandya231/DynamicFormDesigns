package com.swiftiq.logever.controller;

import com.swiftiq.logever.kraftos.entity.App;
import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.service.AppService;
import com.swiftiq.logever.service.NotificationService;
import com.swiftiq.logever.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@CrossOrigin("*")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserService userService;

    @GetMapping("/")
    public ResponseEntity<?> getNotifications() {
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();
        return new ResponseEntity<>(notificationService.findAllRelevantForApp(user, "Builder"), HttpStatus.OK);
    }

    @GetMapping("/all/")
    public ResponseEntity<?> getAllNotifications() {
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get();
        return new ResponseEntity<>(notificationService.findAllFor(user), HttpStatus.OK);
    }

}
