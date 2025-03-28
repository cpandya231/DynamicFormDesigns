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
        basePackages = "com.swiftiq.logever.builder",
        entityManagerFactoryRef = "builderEntityManagerFactory",
        transactionManagerRef = "builderTransactionManager"
)
public class BuilderDBConfig {
    @Bean(name = "builderDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.builder")
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "builderEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("builderDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.swiftiq.logever.builder")
                .persistenceUnit("builderPU")
                .build();
    }

//    @Primary
    @Bean(name = "builderTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("builderEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
