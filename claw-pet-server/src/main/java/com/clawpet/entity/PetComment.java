package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("pet_comment")
public class PetComment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;

    private Long userId;

    private String content;

    private Long parentId;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private List<PetComment> replies;

    @TableField(exist = false)
    private String replyToUserName;

    @TableField(exist = false)
    private String petName;

    private Long replyToUserId;
}
