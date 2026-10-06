package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.PetComment;
import com.clawpet.service.CommentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器 - 宠物评论/回复的增删改查
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** 分页查询某宠物的评论（含回复） */
    @GetMapping("/list/{petId}")
    public Result<Page<PetComment>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Long petId) {
        return Result.success(commentService.listByPet(page, size, petId));
    }

    /** 查询当前用户的评论列表 */
    @GetMapping("/my")
    public Result<Page<PetComment>> listMy(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(commentService.listByUser(page, size, userId));
    }

    /** 查询回复我的评论列表 */
    @GetMapping("/replied")
    public Result<Page<PetComment>> listReplied(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(commentService.listRepliedToUser(page, size, userId));
    }

    /** 新增评论或回复 */
    @PostMapping
    public Result<Long> add(Authentication authentication, @RequestBody PetComment comment) {
        Long userId = (Long) authentication.getPrincipal();
        comment.setUserId(userId);
        Long id = commentService.add(comment);
        return Result.success("评论成功", id);
    }

    /** 更新评论内容 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PetComment comment) {
        comment.setId(id);
        commentService.update(comment);
        return Result.success("更新成功", null);
    }

    /** 删除评论（会同时删除其回复和关联通知） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success("删除成功", null);
    }
}
