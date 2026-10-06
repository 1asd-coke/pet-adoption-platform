package com.clawpet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 - 令牌生成、解析和校验
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey key;
    private final long expiration;

    /**
     * ⚠️ 这里只能有这一个构造器。
     *
     * <p>曾经还有一个包级私有的无参构造器（给单元测试用固定密钥）。Spring 在
     * 「没有 @Autowired 注解 + 存在无参构造器」时会优先用无参构造器，
     * 结果 `jwt.secret` 配了也不生效，所有环境都偷偷用着写死在代码里的测试密钥 ——
     * 而这个密钥是公开在仓库里的，等于谁都能伪造登录态。
     *
     * <p>现在测试改用这个有参构造器传自己的密钥，配置才真正生效。
     */
    public JwtUtil(@Value("${jwt.secret:}") String secret,
                   @Value("${jwt.expiration:86400000}") long expiration) {
        String configured = normalize(secret);
        if (configured == null) {
            // 没配 JWT_SECRET 时**随机生成**一个，而不是回退到写死的默认值。
            // 这样「clone 下来不配任何环境变量也能起」的便利还在，
            // 同时密钥只存在于本次进程的内存里，外人无从伪造登录态。
            // 代价：重启后旧令牌失效，需要重新登录（本地开发无所谓）。
            this.key = Jwts.SIG.HS256.key().build();
            log.warn("");
            log.warn("⚠️  未配置环境变量 JWT_SECRET，本次启动已随机生成临时签名密钥。");
            log.warn("⚠️  该密钥重启即失效（需重新登录）；生产环境请配置 JWT_SECRET（≥32 字节）。");
            log.warn("");
        } else {
            this.key = Keys.hmacShaKeyFor(configured.getBytes(StandardCharsets.UTF_8));
        }
        this.expiration = expiration;
    }

    /**
     * 校验并归一化配置里的密钥。
     *
     * <p>以前这里在「配置缺失 / 留空 / 占位符 `${JWT_SECRET}` 没解析出来」时会退回一个
     * 写死在源码里的默认值 —— 那个值公开在仓库里，等于把签名密钥交给了所有人。
     * 现在改为返回 {@code null}，由构造器随机生成，代码里不再存在任何「人人皆知的密钥」。
     *
     * @return 可用的密钥；未配置时返回 {@code null}
     */
    private static String normalize(String secret) {
        if (secret == null || secret.isBlank() || secret.contains("${")) {
            return null;
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "jwt.secret 太短（至少需要 32 字节，当前 " + secret.length() + " 字符）。"
                            + "HS256 要求密钥长度不低于 256 位，请换一个更长的 JWT_SECRET。");
        }
        return secret;
    }

    /**
     * 生成 JWT 令牌（使用默认过期时间）
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, expiration);
    }

    /** 生成自定义过期时间的 token（用于密码重置，5 分钟有效） */
    public String generateToken(Long userId, String username, String role, long customExpiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + customExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从令牌中解析用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从令牌中解析用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从令牌中解析用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 校验令牌是否有效（签名正确且未过期）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析并验证 JWT 签名，返回 Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
