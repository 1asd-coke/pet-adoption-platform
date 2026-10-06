package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_image")
public class PetImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;

    private String url;

    private Integer isCover;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
