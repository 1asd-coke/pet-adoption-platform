package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.PetFavorite;
import com.clawpet.service.FavoriteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器 - 宠物收藏/取消收藏/检查收藏状态
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /** 获取当前用户的收藏列表 */
    @GetMapping("/list")
    public Result<List<PetFavorite>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(favoriteService.listByUser(userId));
    }

    /** 收藏指定宠物 */
    @PostMapping("/{petId}")
    public Result<Void> add(Authentication authentication, @PathVariable Long petId) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.add(userId, petId);
        return Result.success("收藏成功", null);
    }

    /** 取消收藏指定宠物 */
    @DeleteMapping("/{petId}")
    public Result<Void> remove(Authentication authentication, @PathVariable Long petId) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.remove(userId, petId);
        return Result.success("取消收藏", null);
    }

    /** 检查是否已收藏指定宠物 */
    @GetMapping("/check/{petId}")
    public Result<Boolean> check(Authentication authentication, @PathVariable Long petId) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isFavorited = favoriteService.check(userId, petId);
        return Result.success(isFavorited);
    }
}
