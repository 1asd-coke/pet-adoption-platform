package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("pet_favorite")
public class PetFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long petId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String petName;

    @TableField(exist = false)
    private String breed;

    @TableField(exist = false)
    private String petStatus;

    @TableField(exist = false)
    private List<String> petImageUrls;
}
