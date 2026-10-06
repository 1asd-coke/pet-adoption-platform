package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.AdoptionGuide;
import com.clawpet.service.AdoptionGuideService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 领养须知控制器
 */
@RestController
@RequestMapping("/api/guide")
public class AdoptionGuideController {

    private final AdoptionGuideService guideService;

    public AdoptionGuideController(AdoptionGuideService guideService) {
        this.guideService = guideService;
    }

    /** 获取所有领养须知（公开） */
    @GetMapping
    public Result<List<AdoptionGuide>> get() {
        return Result.success(guideService.list());
    }

    /** 新增/更新某一章节（管理员，id 为空时新增） */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody AdoptionGuide guide) {
        guideService.update(guide);
        return Result.success("更新成功", null);
    }

    /** 删除某一章节（管理员） */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        guideService.delete(id);
        return Result.success("删除成功", null);
    }
}
