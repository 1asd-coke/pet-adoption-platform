package com.clawpet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawpet.entity.PetComment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

public interface PetCommentMapper extends BaseMapper<PetComment> {

    @Select("SELECT pet_id FROM pet_comment WHERE id = #{id}")
    Long selectPetIdById(@Param("id") Long id);

    @Delete("DELETE FROM pet_comment WHERE pet_id = #{petId}")
    void deleteByPetId(Long petId);
}
