package com.swiftiq.logever.controller;

import com.swiftiq.logever.builder.entity.Viz;
import com.swiftiq.logever.service.VizService;
import com.swiftiq.logever.service.WatchTowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@RestController
@RequestMapping("/watchtower")
@CrossOrigin(origins = "*")
public class WatchTowerController {

    @Autowired
    WatchTowerService watchTowerService;

    @Autowired
    VizService vizService;

    @GetMapping("/query/{query}")
    public ResponseEntity<?> getResults(@PathVariable("query") String query) {
        return new ResponseEntity<>(watchTowerService.getQueryResults(query), HttpStatus.OK);
    }

    @GetMapping("/tables")
    public ResponseEntity<?> getTables() {
        return new ResponseEntity<>(watchTowerService.getTableDetails(), HttpStatus.OK);
    }

    @GetMapping("/viz")
    public ResponseEntity<?> getVizs() {
        return new ResponseEntity<>(vizService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/viz/{id}")
    public ResponseEntity<?> getViz(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(vizService.get(id), HttpStatus.OK);
    }

    @PostMapping("/viz")
    public ResponseEntity<?> saveViz(@RequestBody Viz viz) {
        return new ResponseEntity<>(vizService.save(viz), HttpStatus.OK);
    }
}
