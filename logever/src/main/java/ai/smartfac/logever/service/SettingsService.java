package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Settings;
import ai.smartfac.logever.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SettingsService {

    @Autowired
    SettingsRepository settingsRepository;

    public Optional<Settings> findByKey(String key) {
        return settingsRepository.findByKey(key);
    }

    public Iterable<Settings> findAllSettingsByType(String type) {
        return settingsRepository.findAllSettingsByType(type);
    }

    public Iterable<Settings> findAllSettings() {
        return settingsRepository.findAll();
    }

    public Optional<Settings> update(Settings settings) {
        Optional<Settings> foundSetting = settingsRepository.findByKey(settings.getKey());
        if(foundSetting.isPresent()) {
            foundSetting.get().setValue(settings.getValue());
            settingsRepository.save(foundSetting.get());
        }
        return foundSetting;
    }
}
