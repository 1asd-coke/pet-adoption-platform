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

    /** 更新领养须知（管理员） */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody AdoptionGuide guide) {
        guideService.update(guide);
        return Result.success("更新成功", null);
    }
}
