package com.swiftiq.logever.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LogRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    //public
}
