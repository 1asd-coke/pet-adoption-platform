package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.User;
import com.clawpet.mapper.UserMapper;
import com.clawpet.service.CommentService;
import com.clawpet.service.FavoriteService;
import com.clawpet.service.AdoptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器 - 管理员操作用户列表查询和删除
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserMapper userMapper;
    private final CommentService commentService;
    private final FavoriteService favoriteService;
    private final AdoptService adoptService;

    public UserController(UserMapper userMapper,
                          CommentService commentService,
                          FavoriteService favoriteService,
                          AdoptService adoptService) {
        this.userMapper = userMapper;
        this.commentService = commentService;
        this.favoriteService = favoriteService;
        this.adoptService = adoptService;
    }

    /** 分页查询用户列表（仅管理员） */
    @GetMapping("/users/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> p = new Page<>(page, size);
        Page<User> result = userMapper.selectPage(p,
                new LambdaQueryWrapper<User>()
                        .select(User::getId, User::getUsername, User::getNickname,
                                User::getEmail, User::getPhone, User::getAuthStatus,
                                User::getRole, User::getCreatedAt)
                        .orderByDesc(User::getCreatedAt));
        return Result.success(result);
    }

    /** 删除用户（仅管理员）— 级联清理评论、收藏、领养申请 */
    @PostMapping("/users/delete")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Result<Void> deleteUser(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null) return Result.error(400, "用户ID不能为空");
        commentService.deleteByUserId(userId);
        favoriteService.deleteByUserId(userId);
        adoptService.deleteByUserId(userId);
        userMapper.deleteById(userId);
        return Result.success();
    }
}
