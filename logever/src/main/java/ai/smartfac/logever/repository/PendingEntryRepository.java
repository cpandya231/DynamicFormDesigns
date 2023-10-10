package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.PendingEntry;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface PendingEntryRepository extends CrudRepository<PendingEntry,Integer> {
    @Query(value = "Select p from PendingEntry p where (p.assignedRole IN (:assignedRoles) and p.assignedDepartment=:assignedDepartment) or p.assignedUser=:assignedUser")
    public Iterable<PendingEntry> getAllForRoleAndDepartmentOrUser(@Param("assignedRoles") List<Integer> assignedRoles, @Param("assignedDepartment") Integer assignedDepartment, @Param("assignedUser") String assignedUser);

    @Modifying
    @Transactional
    @Query(value="delete from PendingEntry p where p.formId = ?1 and p.entryId = ?2")
    public void deleteAllForFormAndEntry(Integer formId,Integer entryId);
}
