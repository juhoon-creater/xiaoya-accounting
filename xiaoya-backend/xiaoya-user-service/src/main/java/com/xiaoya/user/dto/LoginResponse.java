package com.xiaoya.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录成功后的响应数据。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    /** 后续受保护接口使用的访问令牌。 */
    private String accessToken;

    /** 令牌类型，固定为 Bearer。 */
    private String tokenType;

    /** 令牌有效时间，单位为秒。 */
    private Long expiresIn;

    /** 当前登录用户的公开信息。 */
    private UserInfo user;

    /**
     * 登录响应中的用户公开信息，不包含密码相关数据。
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {

        /** 用户 ID。 */
        private Long id;

        /** 用户名。 */
        private String username;
    }
}