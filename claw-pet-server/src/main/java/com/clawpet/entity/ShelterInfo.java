package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shelter_info")
public class ShelterInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String address;
    private String phone;
    private String email;
    private String workHours;
    private String wechat;
    private String description;
    private String image;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
