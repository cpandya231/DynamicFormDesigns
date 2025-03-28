package com.swiftiq.logever.service;

import com.swiftiq.logever.main.entity.UploadHistory;
import com.swiftiq.logever.main.UploadHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UploadHistoryService {

    @Autowired
    UploadHistoryRepository uploadHistoryRepository;

    public Optional<UploadHistory> getUploadHistoryById(Integer id) {
        return uploadHistoryRepository.findById(id);
    }

    public Iterable<UploadHistory> getAllUploadHistory() {
        return uploadHistoryRepository.findAll();
    }

    public UploadHistory save(UploadHistory uploadHistory) {
        return uploadHistoryRepository.save(uploadHistory);
    }

    public Optional<UploadHistory> findById(Integer id) { return uploadHistoryRepository.findById(id);}

    public Iterable<UploadHistory> findAllByFormId(Integer formId) {
        return uploadHistoryRepository.findAllByFormId(formId);
    }

    public void saveAll(ArrayList<UploadHistory> uploadHistories) {
        uploadHistoryRepository.saveAll(uploadHistories);
    }
}
