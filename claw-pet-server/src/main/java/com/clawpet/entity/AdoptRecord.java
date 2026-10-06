package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("adopt_record")
public class AdoptRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private Long petId;

    private Long userId;

    private LocalDateTime adoptTime;

    private String followupStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String petName;

    @TableField(exist = false)
    private String adopterName;
}
