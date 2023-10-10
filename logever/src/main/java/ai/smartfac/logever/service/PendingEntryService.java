package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.PendingEntry;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.repository.PendingEntryRepository;
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
