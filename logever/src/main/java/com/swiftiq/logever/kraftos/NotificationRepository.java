package com.swiftiq.logever.kraftos;

import com.swiftiq.logever.kraftos.entity.Notification;
import com.swiftiq.logever.kraftos.entity.Settings;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends CrudRepository<Notification,Integer> {

    @Query("select n from Notification n where n.user = :user OR (n.role IN :roles AND n.department = :department AND n.createdBy <> :user)")
    List<Notification> findByUserOrRoleInAndDepartment(@Param("user") String user,
                                                       @Param("roles") List<Integer> roles,
                                                       @Param("department") Integer department);

    @Query("select n from Notification n where (n.user = :user OR (n.role IN :roles AND n.department = :department AND n.createdBy <> :user)) AND app = :app")
    List<Notification> findAllRelevant(@Param("user") String user,
                                                       @Param("roles") List<Integer> roles,
                                                       @Param("department") Integer department,
                                                       @Param("app") String app);

    @Query("select n from Notification n where (n.user = :user OR (n.role IN :roles AND n.department = :department AND n.createdBy <> :user)) AND app IN (:app)")
    List<Notification> findAllRelevantforLogever(@Param("user") String user,
                                       @Param("roles") List<Integer> roles,
                                       @Param("department") Integer department,
                                       @Param("app") List<String> app);

    @Transactional(transactionManager = "kraftOSTransactionManager")
    void deleteAllByAppAndPrimaryId(String app, String primaryId);

    @Transactional(transactionManager = "kraftOSTransactionManager")
    void deleteAllByAppAndPrimaryIdAndSubPrimaryId(String app, String primaryId, String subPrimaryId);
}
