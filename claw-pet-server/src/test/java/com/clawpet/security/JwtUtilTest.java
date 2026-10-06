package com.clawpet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // 用固定密钥构造，测试才可复现（>=32 字节，HS256 的硬性要求）
        jwtUtil = new JwtUtil("TestSecretKeyForUnitTest12345678901234567890", 86400000L);
    }

    @Test
    void 正常生成Token_应成功() {
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        assertThat(token).isNotBlank();
    }

    @Test
    void 正常解析Token_应返回用户信息() {
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(1L);
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("testuser");
        assertThat(jwtUtil.getRoleFromToken(token)).isEqualTo("user");
    }

    @Test
    void Token过期_验证应返回false() throws InterruptedException {
        // 将过期时间改为 100ms（反射修改 final 字段）
        ReflectionTestUtils.setField(jwtUtil, "expiration", 100L);
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        Thread.sleep(200);
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void 篡改Token_验证应返回false() {
        assertThat(jwtUtil.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void 空Token_验证应返回false() {
        assertThat(jwtUtil.validateToken("")).isFalse();
        assertThat(jwtUtil.validateToken(null)).isFalse();
    }

    // ========== 密钥来源（安全加固后的行为） ==========

    @Test
    void 未配置密钥_应随机生成而不是抛异常() {
        // 保证「clone 下来不配任何环境变量也能起」
        JwtUtil a = new JwtUtil("", 86400000L);
        JwtUtil b = new JwtUtil(null, 86400000L);
        JwtUtil c = new JwtUtil("${JWT_SECRET}", 86400000L); // 占位符没解析出来的情况

        assertThat(a.generateToken(1L, "u", "user")).isNotBlank();
        assertThat(b.generateToken(1L, "u", "user")).isNotBlank();
        assertThat(c.generateToken(1L, "u", "user")).isNotBlank();
    }

    @Test
    void 未配置密钥_两次启动的密钥必须不同() {
        // 这是本次加固的核心：代码里不再有「人人皆知」的密钥。
        // 若两实例可以互相验签，说明还残留着某个固定默认值。
        JwtUtil first = new JwtUtil("", 86400000L);
        JwtUtil second = new JwtUtil("", 86400000L);

        String tokenFromFirst = first.generateToken(1L, "admin", "admin");

        assertThat(first.validateToken(tokenFromFirst)).isTrue();
        assertThat(second.validateToken(tokenFromFirst))
                .as("第二个实例不该能验过第一个实例签发的 token，否则等于存在公开的固定密钥")
                .isFalse();
        // 反向也验一次，避免只是单向巧合
        assertThat(first.validateToken(second.generateToken(1L, "admin", "admin"))).isFalse();
    }

    @Test
    void 密钥太短_应启动即失败() {
        // 不是静默降级，也不是悄悄换一个 —— 直接拒绝启动，让人没法糊弄过去
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> new JwtUtil("too-short", 86400000L)
        ).getMessage()).contains("32 字节");
    }
}
