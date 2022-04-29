package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.Form;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FormRepository extends CrudRepository<Form,Integer> {

    public Optional<Form> findByName(String name);
}
