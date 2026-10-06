package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawpet.entity.PetFavorite;
import com.clawpet.entity.PetImage;
import com.clawpet.entity.PetInfo;
import com.clawpet.mapper.PetFavoriteMapper;
import com.clawpet.mapper.PetImageMapper;
import com.clawpet.mapper.PetInfoMapper;
import com.clawpet.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现 - 收藏列表/添加/取消/检查
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final PetFavoriteMapper favoriteMapper;
    private final PetInfoMapper petInfoMapper;
    private final PetImageMapper petImageMapper;

    public FavoriteServiceImpl(PetFavoriteMapper favoriteMapper, PetInfoMapper petInfoMapper, PetImageMapper petImageMapper) {
        this.favoriteMapper = favoriteMapper;
        this.petInfoMapper = petInfoMapper;
        this.petImageMapper = petImageMapper;
    }

    /**
     * 获取用户的收藏列表，附带宠物名称/品种/状态/图片
     */
    @Override
    public List<PetFavorite> listByUser(Long userId) {
        List<PetFavorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<PetFavorite>()
                        .eq(PetFavorite::getUserId, userId)
                        .orderByDesc(PetFavorite::getCreatedAt));

        for (PetFavorite fav : favorites) {
            PetInfo pet = petInfoMapper.selectById(fav.getPetId());
            if (pet != null) {
                fav.setPetName(pet.getName());
                fav.setBreed(pet.getBreed());
                fav.setPetStatus(pet.getStatus());
                // 加载宠物图片
                List<PetImage> images = petImageMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PetImage>()
                                .eq(PetImage::getPetId, pet.getId())
                                .orderByAsc(PetImage::getSort));
                fav.setPetImageUrls(images.stream().map(PetImage::getUrl).collect(Collectors.toList()));
            }
        }

        return favorites;
    }

    /**
     * 添加收藏（重复收藏会抛异常）
     */
    @Override
    @Transactional
    public void add(Long userId, Long petId) {
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<PetFavorite>()
                        .eq(PetFavorite::getUserId, userId)
                        .eq(PetFavorite::getPetId, petId));

        if (count > 0) {
            throw new RuntimeException("已收藏该宠物");
        }

        PetFavorite favorite = new PetFavorite();
        favorite.setUserId(userId);
        favorite.setPetId(petId);
        favoriteMapper.insert(favorite);
    }

    /**
     * 取消收藏
     */
    @Override
    @Transactional
    public void remove(Long userId, Long petId) {
        favoriteMapper.delete(
                new LambdaQueryWrapper<PetFavorite>()
                        .eq(PetFavorite::getUserId, userId)
                        .eq(PetFavorite::getPetId, petId));
    }

    /**
     * 删除某用户的所有收藏（级联删除用户时使用）
     */
    @Override
    public void deleteByUserId(Long userId) {
        favoriteMapper.delete(new LambdaQueryWrapper<PetFavorite>()
                .eq(PetFavorite::getUserId, userId));
    }

    /**
     * 检查是否已收藏某宠物
     */
    @Override
    public boolean check(Long userId, Long petId) {
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<PetFavorite>()
                        .eq(PetFavorite::getUserId, userId)
                        .eq(PetFavorite::getPetId, petId));
        return count > 0;
    }
}
