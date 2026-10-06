package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.PetCategory;
import com.clawpet.entity.PetImage;
import com.clawpet.entity.PetInfo;
import com.clawpet.mapper.PetCategoryMapper;
import com.clawpet.mapper.PetImageMapper;
import com.clawpet.mapper.PetInfoMapper;
import com.clawpet.mapper.PetCommentMapper;
import com.clawpet.mapper.PetFavoriteMapper;
import com.clawpet.mapper.AdoptApplicationMapper;
import com.clawpet.service.PetService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 宠物服务实现 - 宠物 CRUD、分页搜索、缓存管理
 */
@Service
public class PetServiceImpl implements PetService {

    private final PetInfoMapper petInfoMapper;
    private final PetCategoryMapper petCategoryMapper;
    private final PetImageMapper petImageMapper;
    private final PetCommentMapper petCommentMapper;
    private final PetFavoriteMapper petFavoriteMapper;
    private final AdoptApplicationMapper adoptApplicationMapper;

    public PetServiceImpl(PetInfoMapper petInfoMapper,
                          PetCategoryMapper petCategoryMapper,
                          PetImageMapper petImageMapper,
                          PetCommentMapper petCommentMapper,
                          PetFavoriteMapper petFavoriteMapper,
                          AdoptApplicationMapper adoptApplicationMapper) {
        this.petInfoMapper = petInfoMapper;
        this.petCategoryMapper = petCategoryMapper;
        this.petImageMapper = petImageMapper;
        this.petCommentMapper = petCommentMapper;
        this.petFavoriteMapper = petFavoriteMapper;
        this.adoptApplicationMapper = adoptApplicationMapper;
    }

    /**
     * 分页查询宠物列表，支持多条件筛选和关键字搜索（名称/品种/分类）
     */
    @Override
    public Page<PetInfo> list(int page, int size, Long categoryId, String status, String keyword,
                              String gender, String healthStatus) {
        Page<PetInfo> p = new Page<>(page, size);

        // 找出所有启用的分类 ID
        List<Long> activeCategoryIds = petCategoryMapper.selectList(
                new LambdaQueryWrapper<PetCategory>().eq(PetCategory::getStatus, "active")
        ).stream().map(PetCategory::getId).collect(Collectors.toList());

        LambdaQueryWrapper<PetInfo> wrapper = new LambdaQueryWrapper<PetInfo>()
                // 只显示启用分类下的宠物
                .in(activeCategoryIds.isEmpty() ? false : true, PetInfo::getCategoryId, activeCategoryIds)
                .eq(categoryId != null, PetInfo::getCategoryId, categoryId)
                .eq(StringUtils.hasText(status), PetInfo::getStatus, status)
                .eq(StringUtils.hasText(gender), PetInfo::getGender, gender)
                .eq(StringUtils.hasText(healthStatus), PetInfo::getHealthStatus, healthStatus);

        // 关键字搜索：名称 + 品种（未选分类时也匹配分类名）
        if (StringUtils.hasText(keyword)) {
            if (categoryId == null) {
                // 未选分类时，也按分类名模糊匹配（完整词 → 单字兜底）
                List<Long> matchedCategoryIds = petCategoryMapper.selectList(
                        new LambdaQueryWrapper<PetCategory>()
                                .like(PetCategory::getName, keyword)
                ).stream().map(PetCategory::getId).collect(Collectors.toList());
                // 完整词没匹配上时，拆单字再试（"小兔兔"→"兔"匹配"兔子"）
                if (matchedCategoryIds.isEmpty() && keyword.length() > 1) {
                    for (char c : keyword.toCharArray()) {
                        String s = String.valueOf(c);
                        List<Long> ids = petCategoryMapper.selectList(
                                new LambdaQueryWrapper<PetCategory>()
                                        .like(PetCategory::getName, s)
                        ).stream().map(PetCategory::getId).collect(Collectors.toList());
                        matchedCategoryIds.addAll(ids);
                        if (!ids.isEmpty()) break; // 找到第一个匹配的分类就停止
                    }
                }
                // 把所有匹配条件放在同一个 AND 组内
                wrapper.and(w -> {
                    w.like(PetInfo::getName, keyword)
                     .or().like(PetInfo::getBreed, keyword);
                    if (!matchedCategoryIds.isEmpty()) {
                        w.or().in(PetInfo::getCategoryId, matchedCategoryIds);
                    }
                });
            } else {
                // 已选分类时：先检查关键词是否命中分类名（如"猫"→"猫咪"）
                // 命中则关键词是冗余信号，忽略它，避免误过滤
                PetCategory selectedCat = petCategoryMapper.selectById(categoryId);
                boolean keywordMatchesCategory = selectedCat != null
                        && StringUtils.hasText(selectedCat.getName())
                        && selectedCat.getName().contains(keyword);
                if (keywordMatchesCategory) {
                    // 不加搜索条件，仅按分类过滤
                } else {
                    wrapper.and(w -> w
                            .like(PetInfo::getName, keyword)
                            .or().like(PetInfo::getBreed, keyword)
                            .or().like(PetInfo::getDescription, keyword));
                }
            }
        }

        wrapper.orderByDesc(PetInfo::getCreatedAt);

        Page<PetInfo> result = petInfoMapper.selectPage(p, wrapper);

        for (PetInfo pet : result.getRecords()) {
            loadExtraInfo(pet);
        }

        return result;
    }

    /**
     * 根据 ID 获取宠物详情（缓存），自动递增浏览量
     */
    @Override
    @Cacheable(value = "petDetail", key = "#id")
    public PetInfo getById(Long id) {
        PetInfo pet = petInfoMapper.selectById(id);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }

        pet.setViewCount(pet.getViewCount() == null ? 1 : pet.getViewCount() + 1);
        petInfoMapper.updateById(pet);

        loadExtraInfo(pet);
        return pet;
    }

    /**
     * 新增宠物，清除列表和详情缓存
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "petList", allEntries = true),
            @CacheEvict(value = "petDetail", allEntries = true)
    })
    public Long create(PetInfo pet) {
        pet.setViewCount(0);
        petInfoMapper.insert(pet);
        saveImages(pet.getId(), pet.getImageUrls());
        return pet.getId();
    }

    /**
     * 更新宠物信息，同步更新图片关联
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "petList", allEntries = true),
            @CacheEvict(value = "petDetail", allEntries = true)
    })
    public void update(PetInfo pet) {
        PetInfo existing = petInfoMapper.selectById(pet.getId());
        if (existing == null) {
            throw new RuntimeException("宠物不存在");
        }
        petInfoMapper.updateById(pet);
        // 先删除旧的图片关联，再插入新的
        petImageMapper.delete(new LambdaQueryWrapper<PetImage>()
                .eq(PetImage::getPetId, pet.getId()));
        saveImages(pet.getId(), pet.getImageUrls());
    }

    /**
     * 保存宠物图片关联（首张设为封面）
     */
    private void saveImages(Long petId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            PetImage image = new PetImage();
            image.setPetId(petId);
            image.setUrl(imageUrls.get(i));
            image.setIsCover(i == 0 ? 1 : 0);
            image.setSort(i);
            petImageMapper.insert(image);
        }
    }

    /**
     * 删除宠物
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "petList", allEntries = true),
            @CacheEvict(value = "petDetail", allEntries = true)
    })
    public void delete(Long id) {
        petImageMapper.deleteByPetId(id);
        petCommentMapper.deleteByPetId(id);
        petFavoriteMapper.deleteByPetId(id);
        adoptApplicationMapper.deleteByPetId(id);
        petInfoMapper.deleteById(id);
    }

    /**
     * 更新宠物领养状态
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "petList", allEntries = true),
            @CacheEvict(value = "petDetail", allEntries = true)
    })
    public void updateStatus(Long id, String status) {
        PetInfo pet = petInfoMapper.selectById(id);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        pet.setStatus(status);
        petInfoMapper.updateById(pet);
    }

    /**
     * 加载宠物的附加信息：分类名称和图片列表
     */
    private void loadExtraInfo(PetInfo pet) {
        if (pet.getCategoryId() != null) {
            PetCategory category = petCategoryMapper.selectById(pet.getCategoryId());
            if (category != null) {
                pet.setCategoryName(category.getName());
            }
        }

        List<PetImage> images = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>()
                        .eq(PetImage::getPetId, pet.getId())
                        .orderByAsc(PetImage::getSort)
        );
        pet.setImageUrls(images.stream().map(PetImage::getUrl).collect(Collectors.toList()));
    }
}
