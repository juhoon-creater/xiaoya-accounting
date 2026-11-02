package com.xiaoya.user.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * 负责为登录用户签发 JWT 访问令牌。
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationSeconds;
    /**
     * 获取令牌有效期，单位为秒。
     */
    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public JwtTokenProvider(
            @Value("${xiaoya.jwt.secret}") String secret,
            @Value("${xiaoya.jwt.expiration-seconds}") long expirationSeconds) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        // HS256 至少需要 256 位密钥，也就是 32 字节。
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT 密钥长度不能少于32个字节");
        }

        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * 根据用户 ID 和用户名签发带有效期的令牌。
     */
    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
    /**
     * 读取 JWT 剩余有效时间，单位为毫秒。
     *
     * @param token 已通过 Gateway 校验的访问令牌
     * @return Token 过期前剩余的毫秒数
     */
    public long getRemainingValidityMillis(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .getTime() - System.currentTimeMillis();
    }
}