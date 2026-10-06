package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.PetCategory;
import com.clawpet.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器 - 宠物分类 CRUD
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** 获取所有分类列表 */
    @GetMapping("/list")
    public Result<List<PetCategory>> list() {
        return Result.success(categoryService.listAll());
    }

    /** 根据 ID 获取分类 */
    @GetMapping("/{id}")
    public Result<PetCategory> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /** 新建分类 */
    @PostMapping
    public Result<Long> create(@RequestBody PetCategory category) {
        Long id = categoryService.create(category);
        return Result.success("创建成功", id);
    }

    /** 更新分类 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PetCategory category) {
        category.setId(id);
        categoryService.update(category);
        return Result.success("更新成功", null);
    }

    /** 删除分类 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }
}
