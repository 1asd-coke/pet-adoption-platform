package com.clawpet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawpet.entity.AdoptApplication;
import org.apache.ibatis.annotations.Delete;

public interface AdoptApplicationMapper extends BaseMapper<AdoptApplication> {
    @Delete("DELETE FROM adopt_application WHERE pet_id = #{petId}")
    void deleteByPetId(Long petId);
}
