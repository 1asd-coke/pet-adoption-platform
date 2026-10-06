package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.AdoptionStory;
import com.clawpet.service.AdoptionStoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 领养故事控制器 - 独立的领养人故事 CRUD
 */
@RestController
@RequestMapping("/api/adoption-story")
public class AdoptionStoryController {

    private final AdoptionStoryService storyService;

    public AdoptionStoryController(AdoptionStoryService storyService) {
        this.storyService = storyService;
    }

    /** 公开：展示被标记的故事 */
    @GetMapping("/list")
    public Result<List<AdoptionStory>> listShowcase() {
        return Result.success(storyService.listShowcase());
    }

    /** 登录用户：获取自己发布的所有故事（编辑用） */
    @GetMapping("/my")
    public Result<List<AdoptionStory>> myStories(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(storyService.listByUser(userId));
    }

    /** 登录用户：发布故事 */
    @PostMapping("/{recordId}")
    public Result<Void> publish(Authentication auth,
                                @PathVariable Long recordId,
                                @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        storyService.publish(userId, recordId, body.get("story"));
        return Result.success("发布成功", null);
    }

    /** 登录用户：编辑故事 */
    @PutMapping("/{recordId}")
    public Result<Void> update(Authentication auth,
                               @PathVariable Long recordId,
                               @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        storyService.update(userId, recordId, body.get("story"));
        return Result.success("更新成功", null);
    }

    /** 管理员：全量列表 */
    @GetMapping("/admin-list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<AdoptionStory>> adminList() {
        return Result.success(storyService.listAll());
    }

    /** 管理员：切换展示状态 */
    @PutMapping("/{id}/showcase")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleShowcase(@PathVariable Long id,
                                       @RequestParam boolean showcase) {
        storyService.toggleShowcase(id, showcase);
        return Result.success("操作成功", null);
    }
}
