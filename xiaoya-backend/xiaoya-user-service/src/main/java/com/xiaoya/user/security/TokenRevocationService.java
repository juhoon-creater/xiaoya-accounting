package com.xiaoya.user.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * 将已退出的 Token 标记写入 Redis，直到该 Token 自然过期。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    /** Redis 撤销记录的统一前缀，Gateway 也会使用相同前缀。 */
    private static final String KEY_PREFIX = "xiaoya:auth:revoked:";

    /** Redis 字符串操作模板，由 Spring 自动注入。 */
    private final StringRedisTemplate redisTemplate;

    /** 用于读取 Token 的剩余有效时间。 */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 撤销 Token，并让 Redis 记录在 Token 自身过期时自动删除。
     *
     * @param token 当前访问令牌
     */
    public void revoke(String token) {
        long remainingMillis =
                jwtTokenProvider.getRemainingValidityMillis(token);

        // Token 已经过期时不需要留下撤销记录。
        if (remainingMillis <= 0) {
            return;
        }

        String redisKey = KEY_PREFIX + sha256(token);

        // Redis 只保存 Token 摘要，不保存原始登录凭证。
        redisTemplate.opsForValue().set(
                redisKey,
                "1",
                Duration.ofMillis(remainingMillis)
        );

        log.info("访问令牌撤销标记已写入 Redis");
    }

    /**
     * 将原始 Token 转为固定长度的 SHA-256 十六进制摘要。
     */
    private String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 是 Java 标准算法；若不可用，属于运行环境异常。
            throw new IllegalStateException("无法计算 Token 摘要", exception);
        }
    }
}
