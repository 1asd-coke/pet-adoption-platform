package com.clawpet.dto;

import lombok.Data;

/**
 * 登录响应 DTO - 返回 JWT 令牌和用户基本信息
 */
@Data
public class LoginResponse {
    /** JWT 认证令牌 */
    private String token;
    /** 登录用户信息 */
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String phone;
        private String realName;
        private String idCard;
        private String address;
        /** 头像 URL */
        private String avatar;
        /** 实名认证状态：unauth / verified */
        private String authStatus;
        /** 用户角色：user / admin */
        private String role;
    }
}
