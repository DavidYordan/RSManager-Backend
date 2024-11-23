package com.rsmanager.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.rsmanager.repository.remoteB",  // 远程数据库 B 的 Repository 包路径
    entityManagerFactoryRef = "remoteBEntityManagerFactory",
    transactionManagerRef = "remoteBTransactionManager"
)
public class RemoteBJpaConfig {

    @Bean(name = "remoteBEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean remoteBEntityManagerFactory(
            @Qualifier("remoteBDataSource") DataSource remoteBDataSource,
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        return builder
                .dataSource(remoteBDataSource)
                .packages("com.rsmanager.model")  // 实体类的包路径
                .persistenceUnit("remoteB")
                .build();
    }

    @Bean(name = "remoteBTransactionManager")
    public PlatformTransactionManager remoteBTransactionManager(
            @Qualifier("remoteBEntityManagerFactory") EntityManagerFactory remoteBEntityManagerFactory) {
        return new JpaTransactionManager(remoteBEntityManagerFactory);
    }
}