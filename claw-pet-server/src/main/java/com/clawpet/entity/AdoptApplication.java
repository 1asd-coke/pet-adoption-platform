package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 领养申请实体 - 对应 adopt_application 表
 */
@Data
@TableName("adopt_application")
public class AdoptApplication {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请领养的宠物 ID */
    private Long petId;

    /** 申请人用户 ID */
    private Long userId;

    /** 领养理由 */
    private String reason;

    /** 居住条件 */
    private String housingCondition;

    /** 养宠经验 */
    private String experience;

    /** 联系电话 */
    private String contactPhone;

    /** 联系地址 */
    private String contactAddress;

    /** 申请状态：pending / approved / rejected */
    private String status;

    /** 拒绝原因（审核拒绝时填写） */
    private String rejectReason;

    /** 审核人用户 ID */
    private Long reviewUserId;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ====== 非数据库字段 ======

    /** 宠物名称 */
    @TableField(exist = false)
    private String petName;

    /** 宠物品种 */
    @TableField(exist = false)
    private String petBreed;

    /** 申请人用户名 */
    @TableField(exist = false)
    private String userName;

    /** 申请人手机号 */
    @TableField(exist = false)
    private String phone;

    /** 地址（优先申请表地址，后取用户资料地址） */
    @TableField(exist = false)
    private String address;

    /** 申请人真实姓名 */
    @TableField(exist = false)
    private String realName;

    /** 申请人头像 */
    @TableField(exist = false)
    private String userAvatar;
}
