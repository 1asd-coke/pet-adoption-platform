package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.PetInfo;
import com.clawpet.service.CategoryService;
import com.clawpet.service.PetService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物控制器 - 宠物 CRUD、分页查询、搜索
 */
@RestController
@RequestMapping("/api/pet")
public class PetController {

    private final PetService petService;
    private final CategoryService categoryService;

    public PetController(PetService petService, CategoryService categoryService) {
        this.petService = petService;
        this.categoryService = categoryService;
    }

    /**
     * 分页查询宠物列表，支持分类/状态/关键词/性别/健康状况筛选
     */
    @GetMapping("/list")
    public Result<Page<PetInfo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String healthStatus) {
        Page<PetInfo> result = petService.list(page, size, categoryId, status, keyword, gender, healthStatus);
        return Result.success(result);
    }

    /**
     * 根据 ID 获取宠物详情
     */
    @GetMapping("/{id}")
    public Result<PetInfo> getById(@PathVariable Long id) {
        PetInfo pet = petService.getById(id);
        return Result.success(pet);
    }

    /**
     * 新增宠物
     */
    @PostMapping
    public Result<Long> create(Authentication authentication, @RequestBody PetInfo pet) {
        if (authentication != null) {
            pet.setPublishUserId((Long) authentication.getPrincipal());
        }
        Long id = petService.create(pet);
        return Result.success("创建成功", id);
    }

    /**
     * 更新宠物信息
     */
    @PutMapping
    public Result<Void> update(@RequestBody PetInfo pet) {
        petService.update(pet);
        return Result.success("更新成功", null);
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取分类列表（供前端选择分类用）
     */
    @GetMapping("/category/list")
    public Result<?> categoryList() {
        return Result.success(categoryService.listAll());
    }
}
