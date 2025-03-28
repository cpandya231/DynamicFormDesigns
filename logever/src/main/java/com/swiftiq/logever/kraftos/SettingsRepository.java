package com.swiftiq.logever.kraftos;

import com.swiftiq.logever.kraftos.entity.Settings;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SettingsRepository extends CrudRepository<Settings,Integer> {

    public Optional<Settings> findByKey(String key);

    public Iterable<Settings> findAllSettingsByType(String type);

}
