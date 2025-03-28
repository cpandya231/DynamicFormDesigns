package com.swiftiq.logever.main;

import com.swiftiq.logever.main.entity.UploadHistory;
import org.springframework.data.repository.CrudRepository;

public interface UploadHistoryRepository extends CrudRepository<UploadHistory,Integer> {
    public Iterable<UploadHistory> findAllByFormId(Integer formId);
}
