package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.dto.LoginRequest;
import com.clawpet.dto.LoginResponse;
import com.clawpet.entity.User;
import com.clawpet.service.AuthService;
import com.clawpet.util.CaptchaUtil;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器 - 登录/注册/验证码/密码管理/密保问题
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final StringRedisTemplate redisTemplate;

    public AuthController(AuthService authService, StringRedisTemplate redisTemplate) {
        this.authService = authService;
        this.redisTemplate = redisTemplate;
    }

    /** 获取算术验证码（Base64 图片） */
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        CaptchaUtil.CaptchaResult cap = CaptchaUtil.generate();
        String key = UUID.randomUUID().toString().replace("-", "");
        // 5 分钟过期
        redisTemplate.opsForValue().set("captcha:" + key, cap.getAnswer(), 5, TimeUnit.MINUTES);
        return Result.success(Map.of(
                "captchaKey", key,
                "captchaImage", "data:image/jpeg;base64," + cap.getImageBase64()
        ));
    }

    /** 用户登录，校验验证码后返回 JWT 令牌 */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 验证码校验
        validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /** 用户注册，校验验证码后创建账号 */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody LoginRequest request) {
        // 验证码校验
        validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        authService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 校验验证码，失败抛异常
     */
    private void validateCaptcha(String key, String code) {
        if (key == null || code == null || key.isBlank() || code.isBlank()) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        String redisKey = "captcha:" + key;
        String correct = redisTemplate.opsForValue().getAndDelete(redisKey);
        if (correct == null) {
            throw new IllegalArgumentException("验证码已过期，请刷新");
        }
        if (!correct.equals(code.trim())) {
            throw new IllegalArgumentException("验证码错误");
        }
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public Result<User> getCurrentUser(Authentication authentication) {
        // 未登录直接返回 null，不抛 NPE
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.success(null);
        }
        Long userId = (Long) authentication.getPrincipal();
        User user = authService.getCurrentUser(userId);
        return Result.success(user);
    }

    /** 退出登录（前端清除 token 即可） */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    /** 已登录用户修改密码（token 鉴权） */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("newPassword");
        String oldPassword = body.get("oldPassword");
        if (username == null || username.isEmpty()) {
            return Result.badRequest("用户名不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.badRequest("新密码至少 6 位");
        }
        authService.changePassword(username, oldPassword, newPassword);
        return Result.<Void>success();
    }

    /** 验证当前登录用户密码（敏感操作前用） */
    @PostMapping("/verify-password")
    public Result<Void> verifyPassword(Authentication authentication, @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        String password = body.get("password");
        if (password == null || password.isEmpty()) return Result.error(400, "密码不能为空");
        boolean ok = authService.verifyPassword(userId, password);
        if (!ok) return Result.error(401, "密码错误");
        return Result.success();
    }

    // ======== 多密保问题 & 忘记密码 ========

    /** 添加一个密保问题（登录后） */
    @PostMapping("/security-question")
    public Result<Void> addSecurityQuestion(
            Authentication authentication,
            @RequestParam String question,
            @RequestParam String answer) {
        Long userId = (Long) authentication.getPrincipal();
        authService.addSecurityQuestion(userId, question, answer);
        return Result.success("添加成功", null);
    }

    /** 删除一个密保问题（登录后） */
    @DeleteMapping("/security-question/{id}")
    public Result<Void> deleteSecurityQuestion(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        authService.deleteSecurityQuestion(userId, id);
        return Result.success("删除成功", null);
    }

    /** 获取密保问题列表（忘记密码第一步） */
    @GetMapping("/forgot-password")
    public Result<List<String>> getSecurityQuestions(@RequestParam String username) {
        List<String> questions = authService.getSecurityQuestions(username);
        return Result.success(questions);
    }

    /** 选一个问题验证答案（忘记密码第二步） */
    @PostMapping("/verify-answer")
    public Result<String> verifyAnswer(
            @RequestParam String username,
            @RequestParam Long questionId,
            @RequestParam String answer) {
        String token = authService.verifySecurityAnswer(username, questionId, answer);
        return Result.success(token);
    }

    /** 使用重置 token 设置新密码（忘记密码第三步） */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return Result.success("密码重置成功", null);
    }
}
