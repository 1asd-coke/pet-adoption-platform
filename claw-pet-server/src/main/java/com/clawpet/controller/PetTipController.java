package com.clawpet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.common.Result;
import com.clawpet.entity.PetTip;
import com.clawpet.service.PetTipService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 小常识控制器
 */
@RestController
@RequestMapping("/api/tip")
public class PetTipController {

    private final PetTipService tipService;

    public PetTipController(PetTipService tipService) {
        this.tipService = tipService;
    }

    /** 公开：分页查询小常识列表 */
    @GetMapping("/list")
    public Result<Page<PetTip>> list(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return Result.success(tipService.listPublic(page, size));
    }

    /** 公开：获取小常识详情 */
    @GetMapping("/{id}")
    public Result<PetTip> detail(@PathVariable Long id) {
        return Result.success(tipService.detail(id));
    }

    /** 管理员：新增 */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> add(@RequestBody PetTip tip) {
        tipService.add(tip);
        return Result.success("新增成功", null);
    }

    /** 管理员：更新 */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody PetTip tip) {
        tipService.update(tip);
        return Result.success("更新成功", null);
    }

    /** 管理员：删除 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        tipService.delete(id);
        return Result.success("删除成功", null);
    }
}
