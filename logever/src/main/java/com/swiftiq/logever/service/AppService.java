package com.swiftiq.logever.service;

import com.swiftiq.logever.kraftos.AppRepository;
import com.swiftiq.logever.kraftos.entity.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppService {

    private final AppRepository appRepository;

    @Autowired
    public AppService(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public List<App> getAll() {
        return appRepository.findAll();
    }

    public App save(App app) {
        return appRepository.save(app);
    }

    public Optional<App> getById(Integer appId) {
        return  appRepository.findById(appId);
    }
}
