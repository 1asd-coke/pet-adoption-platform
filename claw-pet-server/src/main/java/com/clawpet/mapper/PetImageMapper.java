package com.clawpet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawpet.entity.PetImage;
import org.apache.ibatis.annotations.Delete;

public interface PetImageMapper extends BaseMapper<PetImage> {
    @Delete("DELETE FROM pet_image WHERE pet_id = #{petId}")
    void deleteByPetId(Long petId);
}
