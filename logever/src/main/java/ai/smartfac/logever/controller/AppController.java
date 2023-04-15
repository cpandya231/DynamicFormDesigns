package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.App;
import ai.smartfac.logever.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apps")
@CrossOrigin("*")
public class AppController {

    @Autowired
    AppService appService;

    @GetMapping("/")
    public ResponseEntity<?> getAllApps() {
        return new ResponseEntity<>(appService.getAll(), HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveApp(@RequestBody App app) {
        return new ResponseEntity<>(appService.save(app),HttpStatus.OK);
    }
}
