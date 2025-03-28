package com.swiftiq.logever.service;

import com.swiftiq.logever.builder.entity.Viz;
import com.swiftiq.logever.builder.VizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

//@Service
public class VizService {

//    @Autowired
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
