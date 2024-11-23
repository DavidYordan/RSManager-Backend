package com.rsmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // 排除默认数据源自动配置
@EnableScheduling  // 启用定时任务
public class RsmanagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RsmanagerApplication.class, args);
    }
}
