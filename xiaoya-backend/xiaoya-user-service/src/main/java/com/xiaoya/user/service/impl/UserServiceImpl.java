package com.xiaoya.user.service.impl;


import com.xiaoya.user.dto.*;
import com.xiaoya.user.entity.User;
import com.xiaoya.user.exception.InvalidCredentialsException;
import com.xiaoya.user.exception.UserNotFoundException;
import com.xiaoya.user.exception.UsernameAlreadyExistsException;
import com.xiaoya.user.mapper.UserMapper;
import com.xiaoya.user.security.JwtTokenProvider;
import com.xiaoya.user.security.TokenRevocationService;
import com.xiaoya.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户业务层实现，负责协调用户校验、密码处理和数据库操作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRevocationService tokenRevocationService;

    /**
     * 创建用户账号。数据库唯一索引是用户名不重复的最终保障。
     *
     * @param request 前端提交的用户名和密码
     * @return 不包含密码信息的注册结果
     */
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 去掉用户名首尾空格，避免 "xiaoya" 和 " xiaoya " 被当成不同输入。
        String username = request.getUsername().trim();

        // 先进行友好查重；数据库唯一索引仍负责处理并发注册的情况。
        long existingUsers = userMapper.countByUsername(username);
        if (existingUsers > 0) {
            throw new UsernameAlreadyExistsException();
        }

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setUsername(username);

        // 数据库只保存 BCrypt 哈希，不能保存 request 中的明文密码。
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        try {
            userMapper.insertUser(user);
        } catch (DuplicateKeyException exception) {
            // 防止两个相同用户名的注册请求同时通过上面的查重。
            throw new UsernameAlreadyExistsException();
        }
        log.info("用户注册成功，userId={}", user.getId());
        // 只返回注册接口允许公开的字段，不返回密码哈希。
        return new RegisterResponse(user.getId(), user.getUsername(), user.getCreatedAt());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据用户名查询用户；不存在时与密码错误使用相同的异常。
        User user = userMapper.selectByUsername(request.getUsername());

        if (user == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // 密码验证通过后签发 JWT，并返回与令牌配置一致的有效期。
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功，userId={}", user.getId());
        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getExpirationSeconds(),
                new LoginResponse.UserInfo(user.getId(), user.getUsername())
        );
    }
    /**
     * 查询当前登录用户的公开资料。
     */
    @Override
    public CurrentUserResponse getCurrentUser(Long userId) {
        // userId 来自 Gateway 验证 Token 后设置的请求头。
        User user = userMapper.selectById(userId);

        // Token 有效但用户记录已不存在时，按公共约定返回 404。
        if (user == null) {
            throw new UserNotFoundException();
        }

        log.debug("当前用户资料查询完成，userId={}", userId);

        // 只组装接口允许公开的字段，不直接返回数据库实体。
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt()
        );
    }
    /**
     * 撤销当前访问令牌。
     *
     * @param accessToken 当前请求携带的 Token，不含 Bearer 前缀
     */
    @Override
    public void logout(String accessToken) {
        // 由安全组件计算摘要并写入 Redis，原始 Token 不会被保存或记录到日志。
        tokenRevocationService.revoke(accessToken);
    }
}
