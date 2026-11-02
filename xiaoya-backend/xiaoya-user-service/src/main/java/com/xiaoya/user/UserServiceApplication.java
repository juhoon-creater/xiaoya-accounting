package com.xiaoya.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户微服务启动入口；Spring Boot 从此包开始扫描组件与配置。
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        // 创建 Spring 容器并启动用户服务。
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
