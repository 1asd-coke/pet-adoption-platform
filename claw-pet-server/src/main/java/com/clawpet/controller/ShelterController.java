package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.ShelterInfo;
import com.clawpet.service.ShelterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 收容所信息控制器 - 获取和更新收容所信息
 */
@RestController
@RequestMapping("/api/shelter")
public class ShelterController {

    private final ShelterService shelterService;

    public ShelterController(ShelterService shelterService) {
        this.shelterService = shelterService;
    }

    /** 获取收容所信息 */
    @GetMapping
    public Result<ShelterInfo> get() {
        return Result.success(shelterService.get());
    }

    /** 更新收容所信息（仅管理员） */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody ShelterInfo info) {
        shelterService.update(info);
        return Result.success();
    }
}
