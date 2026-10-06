package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小常识实体 - 独立文章列表
 */
@Data
@TableName("pet_tip")
public class PetTip {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 内容（支持换行） */
    private String content;

    /** 发布日期 */
    private LocalDateTime publishDate;

    /** 排序值（数值大的靠前） */
    private Integer sort;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
