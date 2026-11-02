package com.xiaoya.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 注册成功后的响应数据，不包含密码或密码哈希。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    /** 新创建用户的数据库 ID。 */
    private Long id;

    /** 注册用户名。 */
    private String username;

    /** 用户账号创建时间。 */
    private LocalDateTime createdAt;
}
