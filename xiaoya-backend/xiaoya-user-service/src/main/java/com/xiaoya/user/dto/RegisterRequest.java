package com.xiaoya.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册接口的请求体，只接收客户端允许提交的用户名和密码。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    /** 用户名不能为空，长度限制与用户表及接口约定保持一致。 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须是3到50个字符")
    private String username;

    /** 密码只在请求处理中短暂存在，保存前必须转换为 BCrypt 哈希。 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须是8到72个字符")
    private String password;
}
