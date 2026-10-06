package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.User;
import com.clawpet.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器 - 用户资料修改和账号注销
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    /** 更新个人资料（昵称/手机/实名信息等） */
    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication authentication, @RequestBody User user) {
        Long userId = (Long) authentication.getPrincipal();
        authService.updateProfile(userId, user);
        return Result.success("更新成功", null);
    }

    /** 注销当前用户账号 */
    @PostMapping("/profile/delete")
    public Result<Void> deleteProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.deleteAccount(userId);
        return Result.success("账号已删除", null);
    }
}
