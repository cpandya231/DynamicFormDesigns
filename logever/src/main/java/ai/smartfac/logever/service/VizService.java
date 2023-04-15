package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Viz;
import ai.smartfac.logever.repository.VizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VizService {

    @Autowired
    VizRepository vizRepository;

    public Viz save(Viz viz) {
        return vizRepository.save(viz);
    }

    public Iterable<Viz> getAll() {
        return vizRepository.findAll();
    }

    public Optional<Viz> get(Integer id) {
        return vizRepository.findById(id);
    }
}
