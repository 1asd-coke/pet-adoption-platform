package com.clawpet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawpet.entity.PetFavorite;
import org.apache.ibatis.annotations.Delete;

public interface PetFavoriteMapper extends BaseMapper<PetFavorite> {
    @Delete("DELETE FROM pet_favorite WHERE pet_id = #{petId}")
    void deleteByPetId(Long petId);
}
