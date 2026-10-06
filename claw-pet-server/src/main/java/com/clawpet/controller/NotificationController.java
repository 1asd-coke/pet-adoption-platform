package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.Notification;
import com.clawpet.service.NotificationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器 - 用户的站内通知列表、已读/未读管理
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** 分页查询当前用户的通知列表 */
    @GetMapping("/list")
    public Result<Page<Notification>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(notificationService.listByUser(userId, page, size));
    }

    /** 获取未读通知数量 */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(notificationService.unreadCount(userId));
    }

    /** 标记单条通知为已读 */
    @PutMapping("/read/{id}")
    public Result<Void> markRead(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markRead(id, userId);
        return Result.success(null);
    }

    /** 标记所有通知为已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllRead(userId);
        return Result.success(null);
    }

    /** 删除单条通知 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.delete(id, userId);
        return Result.success(null);
    }
}
