package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.App;
import ai.smartfac.logever.repository.AppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppService {

    @Autowired
    AppRepository appRepository;

    public Iterable<App> getAll() {
        return appRepository.findAll();
    }

    public App save(App app) {
        return appRepository.save(app);
    }
}
