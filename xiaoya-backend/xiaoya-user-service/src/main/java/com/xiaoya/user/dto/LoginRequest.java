package com.xiaoya.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录接口接收的请求参数。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    /** 登录用户名，不能为空。 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 登录密码，不能为空。 */
    @NotBlank(message = "密码不能为空")
    private String password;
}