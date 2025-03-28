package com.swiftiq.logever.service;

import com.swiftiq.logever.main.entity.PendingEntry;
import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.main.PendingEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PendingEntryService {

    @Autowired
    PendingEntryRepository pendingEntryRepository;

    public void save(PendingEntry pendingEntry) {
        pendingEntryRepository.save(pendingEntry);
    }

    public Iterable<PendingEntry> getAllForUser(User user) {
        return pendingEntryRepository.getAllForRoleAndDepartmentOrUser(user.getRoles().stream().map(role->role.getId()).collect(Collectors.toList()),
                user.getDepartment().getId(),user.getUsername());
    }

    public void saveAll(List<PendingEntry> entries) {
        pendingEntryRepository.saveAll(entries);
    }

    public void removeAllFor(Integer formId,Integer entryId) {
        pendingEntryRepository.deleteAllForFormAndEntry(formId,entryId);
    }
}
