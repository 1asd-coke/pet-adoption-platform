package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 领养故事实体 - 独立的领养人故事表（与回访记录解耦）
 */
@Data
@TableName("adoption_story")
public class AdoptionStory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 领养记录 ID */
    private Long recordId;

    /** 宠物 ID */
    private Long petId;

    /** 领养人用户 ID */
    private Long userId;

    /** 领养人小故事 */
    private String story;

    /** 管理员展示标记 */
    private Integer showcase;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 非数据库字段
    @TableField(exist = false)
    private String petName;

    @TableField(exist = false)
    private String petCover;

    @TableField(exist = false)
    private String adopterName;

    @TableField(exist = false)
    private java.util.List<String> images;

    @TableField(exist = false)
    private String followupTime;
}
