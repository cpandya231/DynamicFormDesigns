package com.swiftiq.logever.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.beans.ConstructorProperties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.swiftiq.logever.kraftos",
        entityManagerFactoryRef = "kraftOSEntityManagerFactory",
        transactionManagerRef = "kraftOSTransactionManager"
)
public class KraftOSDBConfig {
    @Bean(name = "kraftOSDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.kraftos")
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "kraftOSEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("kraftOSDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.swiftiq.logever.kraftos")
                .persistenceUnit("kraftOSPU")
                .build();
    }

    @Bean(name = "kraftOSTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("kraftOSEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
