package com.xiaoya.user.service;

import com.xiaoya.user.dto.*;

/**
 * 用户业务层接口，定义用户相关业务操作。
 */
public interface UserService {

    /**
     * 注册新用户。
     *
     * @param request 前端提交的注册信息
     * @return 注册成功后允许返回给前端的用户信息
     */
    RegisterResponse register(RegisterRequest request);

// 在 UserService 接口中添加
    /**
     * 根据用户名和密码完成登录，并返回登录凭证。
     *
     * @param request 登录请求参数
     * @return 登录成功后的用户信息和访问令牌
     */
    LoginResponse login(LoginRequest request);
    /**
     * 查询当前登录用户的公开信息。
     *
     * @param userId Gateway 验证 Token 后传入的当前用户 ID
     * @return 当前用户资料，不包含密码信息
     */
    CurrentUserResponse getCurrentUser(Long userId);
    /**
     * 撤销当前访问令牌，使其不能继续访问受保护接口。
     *
     * @param accessToken 当前请求携带的访问令牌
     */
    void logout(String accessToken);
}