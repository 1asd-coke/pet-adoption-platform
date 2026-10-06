package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.dto.StatsDTO;
import com.clawpet.service.StatsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计控制器 - 首页统计和管理员面板统计数据
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** 获取首页统计概览（宠物/用户/申请数量） */
    @GetMapping("/home")
    public Result<StatsDTO> home() {
        return Result.success(statsService.getHomeStats());
    }

    /** 获取管理员面板详细统计数据（分类分布、趋势、审核状态） */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(Authentication authentication) {
        return Result.success(statsService.getDashboardStats());
    }
}
