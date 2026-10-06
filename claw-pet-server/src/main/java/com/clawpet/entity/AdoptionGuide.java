package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 领养须知实体 - 单条富文本内容，参考 ShelterInfo 模式
 */
@Data
@TableName("adoption_guide")
public class AdoptionGuide {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题（默认"领养须知"） */
    private String title;

    /** 富文本内容（支持换行） */
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
