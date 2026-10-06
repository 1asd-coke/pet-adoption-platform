package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_security_question")
public class UserSecurityQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String question;

    private String answer;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
