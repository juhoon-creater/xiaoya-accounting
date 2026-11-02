package com.xiaoya.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户服务的密码编码配置。
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 将 BCrypt 编码器注册为 Spring Bean，供注册和登录业务注入使用。
     * BCrypt 会自动加盐；校验密码时应使用 matches，而非比较哈希字符串。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
