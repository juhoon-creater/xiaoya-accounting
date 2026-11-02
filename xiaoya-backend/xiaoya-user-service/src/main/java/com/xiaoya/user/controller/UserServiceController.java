package com.xiaoya.user.controller;

import com.xiaoya.user.common.ApiResponse;
import com.xiaoya.user.dto.*;
import com.xiaoya.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务的 HTTP 请求接收层，负责接收请求并调用业务层。
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserServiceController {

    private final UserService userService;


    /**
     * 接收注册 JSON，启用请求字段校验，再调用用户业务层。
     *
     * @param request 前端提交的注册信息
     * @return HTTP 201 和新建用户的公开信息
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
    /**
     * 用户登录。
     *
     * @param request 登录请求参数
     * @return 用户信息和访问令牌
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    /**
     * 查询当前登录用户的公开资料。
     *
     * @param userId Gateway 验证 Token 后传入的当前用户 ID
     * @return HTTP 200 和当前用户资料
     */
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> getCurrentUser(
            @RequestHeader("X-User-Id") Long userId) {

        CurrentUserResponse response = userService.getCurrentUser(userId);
        return ApiResponse.success(response);
    }
    /**
     * 退出当前账号，并撤销请求所携带的访问令牌。
     *
     * @param authorization Authorization 请求头，格式为 Bearer {accessToken}
     * @return HTTP 200，成功响应中的 data 为 null
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authorization) {

        // Gateway 已验证 Bearer 格式和 JWT；这里取出实际 Token 传给业务层。
        String accessToken = authorization.substring("Bearer ".length()).trim();

        userService.logout(accessToken);
        return ApiResponse.success(null);
    }
}