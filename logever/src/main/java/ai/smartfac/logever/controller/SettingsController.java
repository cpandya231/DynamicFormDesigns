package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.Settings;
import ai.smartfac.logever.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    SettingsService settingsService;

    @GetMapping("/")
    public ResponseEntity<?> getSettings() {
        Iterable<Settings> settings = settingsService.findAllSettings();
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @GetMapping("/{settingKey}/")
    public ResponseEntity<?> getSetting(@PathVariable(name = "settingKey") String settingKey) {
        Optional<Settings> queriedSetting = settingsService.findByKey(settingKey);
        Settings foundSettings = queriedSetting.orElseThrow(()->new RuntimeException("Setting not found"));
        return new ResponseEntity<>(foundSettings, HttpStatus.OK);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateSetting(@RequestBody Settings settings) {
        Optional<Settings> savedSetting = settingsService.update(settings);
        return new ResponseEntity<>(savedSetting, HttpStatus.OK);
    }

    @PutMapping("/all")
    public ResponseEntity<?> updateSetting(@RequestBody List<Settings> settings) {
        settingsService.updateAll(settings);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
