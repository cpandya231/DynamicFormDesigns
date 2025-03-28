package com.swiftiq.logever.kraftos;

import com.swiftiq.logever.kraftos.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRepository extends JpaRepository<App,Integer> {
}
