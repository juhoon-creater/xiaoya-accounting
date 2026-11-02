package com.xiaoya.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询当前用户接口的响应数据，不包含密码信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {

    /** 当前用户 ID。 */
    private Long id;

    /** 当前用户名。 */
    private String username;

    /** 用户注册时间。 */
    private LocalDateTime createdAt;
}