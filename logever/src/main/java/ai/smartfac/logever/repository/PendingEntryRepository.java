package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.PendingEntry;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface PendingEntryRepository extends CrudRepository<PendingEntry,Integer> {
    @Query(value = "Select p from PendingEntry p where (p.assignedRole=?1 and p.assignedDepartment=?2) or p.assignedUser=?3")
    public Iterable<PendingEntry> getAllForRoleAndDepartmentOrUser(Integer assignedRole,Integer assignedDepartment,String assignedUser);

    @Modifying
    @Transactional
    @Query(value="delete from PendingEntry p where p.formId = ?1 and p.entryId = ?2")
    public void deleteAllForFormAndEntry(Integer formId,Integer entryId);
}
