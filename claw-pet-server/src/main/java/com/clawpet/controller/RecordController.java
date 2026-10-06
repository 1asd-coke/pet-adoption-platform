package com.clawpet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.common.Result;
import com.clawpet.entity.AdoptRecord;
import com.clawpet.entity.FollowupRecord;
import com.clawpet.service.RecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 领养记录控制器 - 领养记录查询和回访管理（故事功能已独立到 AdoptionStoryController）
 */
@RestController
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/api/record/list")
    public Result<Page<AdoptRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(recordService.listAll(page, size));
    }

    @GetMapping("/api/record/{id}")
    public Result<AdoptRecord> getById(@PathVariable Long id) {
        return Result.success(recordService.getById(id));
    }

    @PostMapping("/api/record/followup")
    public Result<Void> addFollowup(@RequestBody FollowupRecord followup) {
        recordService.addFollowup(followup);
        return Result.success("回访记录添加成功", null);
    }

    @GetMapping("/api/record/followup/{recordId}")
    public Result<List<FollowupRecord>> getFollowups(@PathVariable Long recordId) {
        return Result.success(recordService.getFollowups(recordId));
    }
}
