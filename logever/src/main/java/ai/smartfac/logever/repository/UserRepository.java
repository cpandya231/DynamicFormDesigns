package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User,Integer> {

    Optional<User> findByUsername(String username);
}
