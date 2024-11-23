package com.rsmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // 本地数据源
    @Bean(name = "localDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.local")
    public DataSource localDataSource() {
        return DataSourceBuilder.create().build();
    }

    // 远程数据源，平台
    @Bean(name = "remoteDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.remote")
    public DataSource remoteDataSource() {
        return DataSourceBuilder.create().build();
    }

    // 远程数据源，爬虫
    @Bean(name = "remoteBDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.remote-b")
    public DataSource remoteBDataSource() {
        return DataSourceBuilder.create().build();
    }
}