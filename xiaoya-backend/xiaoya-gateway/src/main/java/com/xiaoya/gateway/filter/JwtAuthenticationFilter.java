package com.xiaoya.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 在请求转发前验证 JWT；注册和登录接口允许匿名访问。
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SecretKey secretKey;
    /** 响应式 Redis 客户端，用于查询 Token 是否已撤销。 */
    private final ReactiveStringRedisTemplate redisTemplate;

    public JwtAuthenticationFilter(
            @Value("${xiaoya.jwt.secret}") String secret,
            ReactiveStringRedisTemplate redisTemplate) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        // HS256 要求密钥至少为 32 个字节。
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT 密钥长度不能少于32个字节");
        }

        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 注册和登录需要在没有令牌时也能访问。
        if ("/api/users/register".equals(path)
                || "/api/users/login".equals(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authorization.substring(7).trim();

        try {
            // 验签并解析声明；解析过程也会检查令牌是否过期。
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 登录时把用户 ID 放在 JWT 的 subject 中；签名验证通过后再读取它。
            String userId = claims.getSubject();
            if (userId == null || userId.isBlank()) {
                return unauthorized(exchange);
            }

// 覆盖客户端传来的同名请求头，避免客户端伪造用户 ID。
            // 用户服务写入 Redis 时使用的前缀必须完全一致。
            String redisKey = "xiaoya:auth:revoked:" + sha256(token);

            // 重复调用退出接口要保持幂等，所以允许已撤销 Token 再次到达 logout。
            boolean isLogoutRequest = HttpMethod.POST.matches(
                    exchange.getRequest().getMethod().name()
            ) && "/api/users/logout".equals(path);

            return redisTemplate.hasKey(redisKey)
                    .flatMap(revoked -> {
                        if (Boolean.TRUE.equals(revoked) && !isLogoutRequest) {
                            return unauthorized(exchange);
                        }

                        // 覆盖客户端传来的同名请求头，避免伪造用户 ID。
                        ServerWebExchange authenticatedExchange =
                                exchange.mutate()
                                        .request(exchange.getRequest().mutate()
                                                .headers(headers ->
                                                        headers.set("X-User-Id", userId))
                                                .build())
                                        .build();

                        return chain.filter(authenticatedExchange);
                    });
        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(exchange);
        }
    }

    /**
     * 返回统一格式的未授权响应。
     */
    /**
     * 计算 Token 的 SHA-256 摘要，必须与用户服务使用相同的算法。
     */
    private String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("无法计算 Token 摘要", exception);
        }
    }
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        log.warn("请求身份认证失败，path={}", path);
        String body = """
                {"code":0,"msg":"登录凭证无效或已过期","errorCode":"UNAUTHORIZED","data":null}
                """;

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }

    @Override
    public int getOrder() {
        // 在路由转发前尽早校验请求。
        return -100;
    }
}