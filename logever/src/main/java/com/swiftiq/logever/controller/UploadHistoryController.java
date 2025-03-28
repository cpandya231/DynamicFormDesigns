package com.swiftiq.logever.controller;

import com.swiftiq.logever.kraftos.entity.Settings;
import com.swiftiq.logever.main.entity.UploadHistory;
import com.swiftiq.logever.service.SettingsService;
import com.swiftiq.logever.service.UploadHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/uploads")
public class UploadHistoryController {

    @Autowired
    UploadHistoryService uploadHistoryService;

    @GetMapping("/{formId}/")
    public ResponseEntity<?> getSetting(@PathVariable(name = "formId") Integer formId) {
        Iterable<UploadHistory> uploadHistories = uploadHistoryService.findAllByFormId(formId);
        return new ResponseEntity<>(uploadHistories, HttpStatus.OK);
    }
}
