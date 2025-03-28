package com.swiftiq.logever.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.swiftiq.logever.main",
        entityManagerFactoryRef = "logeverEntityManagerFactory",
        transactionManagerRef = "logeverTransactionManager"
)
public class LogeverDBConfig {
    @Primary
    @Bean(name = "logeverDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.logever")
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "logeverEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("logeverDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.swiftiq.logever.main")
                .persistenceUnit("logeverPU")
                .build();
    }

    @Bean(name = "logeverTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("logeverEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
