package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawpet.entity.PetCategory;
import com.clawpet.mapper.PetCategoryMapper;
import com.clawpet.service.CategoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务实现 - 分类 CRUD，含缓存管理
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final PetCategoryMapper categoryMapper;

    public CategoryServiceImpl(PetCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * 获取所有分类列表（缓存 30 分钟）
     */
    @Override
    @Cacheable(value = "categoryList")
    public List<PetCategory> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<PetCategory>()
                        .orderByAsc(PetCategory::getSort)
                        .orderByAsc(PetCategory::getId));
    }

    /**
     * 根据 ID 获取分类
     */
    @Override
    public PetCategory getById(Long id) {
        PetCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        return category;
    }

    /**
     * 新建分类，清除缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = "categoryList", allEntries = true)
    public Long create(PetCategory category) {
        categoryMapper.insert(category);
        return category.getId();
    }

    /**
     * 更新分类，清除缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = "categoryList", allEntries = true)
    public void update(PetCategory category) {
        PetCategory existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        categoryMapper.updateById(category);
    }

    /**
     * 删除分类，清除缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = "categoryList", allEntries = true)
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
