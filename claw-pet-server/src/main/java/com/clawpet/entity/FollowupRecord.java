package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("followup_record")
public class FollowupRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private String content;

    private String images;

    private LocalDateTime followupTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
