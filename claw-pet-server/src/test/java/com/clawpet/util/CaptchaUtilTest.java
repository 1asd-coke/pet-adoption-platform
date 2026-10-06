package com.clawpet.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class CaptchaUtilTest {

    @Test
    void 生成验证码_应返回非空图片和答案() {
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generate();
        assertThat(result).isNotNull();
        assertThat(result.getImageBase64()).isNotBlank();
        assertThat(result.getAnswer()).isNotBlank();
    }

    @Test
    void 生成的答案_应为数字字符串() {
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generate();
        assertThat(result.getAnswer()).matches("\\d+");
    }

    @Test
    void 多次生成_每次图片应不同() {
        CaptchaUtil.CaptchaResult r1 = CaptchaUtil.generate();
        CaptchaUtil.CaptchaResult r2 = CaptchaUtil.generate();
        assertThat(r1.getImageBase64()).isNotEqualTo(r2.getImageBase64());
    }

    @Test
    void 图片Base64_应为合法编码() {
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generate();
        // CaptchaUtil 返回纯 Base64（无 data:image/ 前缀）
        assertThat(result.getImageBase64()).doesNotContain(" ");
        // Base64 解码不应抛异常
        assertThat(Base64.getDecoder().decode(result.getImageBase64())).isNotEmpty();
    }
}
