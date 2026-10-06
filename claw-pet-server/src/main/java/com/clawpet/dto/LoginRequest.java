package com.clawpet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录/注册请求 DTO - 包含用户名、密码及可选的密保/验证码信息
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 密保问题（注册时可选） */
    private String securityQuestion;

    /** 密保答案（注册时可选） */
    private String securityAnswer;

    /** 验证码 key（从 GET captcha 接口获取） */
    private String captchaKey;

    /** 用户输入的验证码 */
    private String captchaCode;
}
