package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 - 对应 users 表
 */
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（唯一） */
    private String username;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 真实姓名（实名认证） */
    private String realName;

    /** 身份证号（实名认证） */
    private String idCard;

    /** 联系地址 */
    private String address;

    /** 头像 URL */
    private String avatar;

    /** 实名认证状态：unauth / verified */
    private String authStatus;

    /** 用户角色：user / admin */
    private String role;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;
}
