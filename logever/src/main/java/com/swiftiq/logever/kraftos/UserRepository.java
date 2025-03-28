package com.swiftiq.logever.kraftos;

import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.projection.UserLeast;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User,Integer> {

    @Query("select u from User u join fetch u.roles where u.username = ?1")
    Optional<User> findByUsername(String username);
    Optional<User> findByEmployeeCode(String employee_code);
    @Query("SELECT u.id as id, u.username as username, CONCAT(coalesce(u.first_name,''), ' ', coalesce(u.last_name,'')) AS fullName FROM User u")
    Iterable<UserLeast> findAllUsersBy();
}
