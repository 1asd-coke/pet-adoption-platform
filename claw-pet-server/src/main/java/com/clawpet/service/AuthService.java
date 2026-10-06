package com.clawpet.service;

import com.clawpet.dto.LoginRequest;
import com.clawpet.dto.LoginResponse;
import com.clawpet.entity.User;

import java.util.List;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    User getCurrentUser(Long id);
    void register(LoginRequest request);
    void updateProfile(Long id, User user);
    void deleteAccount(Long id);

    // ======== 多密保问题 ========

    /** 添加一个密保问题 */
    void addSecurityQuestion(Long userId, String question, String answer);

    /** 已登录用户修改密码 */
    void changePassword(String username, String oldPassword, String newPassword);

    /** 删除一个密保问题 */
    void deleteSecurityQuestion(Long userId, Long questionId);

    /** 获取该用户的密保问题列表（不含答案） */
    List<String> getSecurityQuestions(String username);

    /** 验证密保答案，返回重置 token */
    String verifySecurityAnswer(String username, Long questionId, String answer);

    /** 使用重置 token 设置新密码 */
    void resetPassword(String token, String newPassword);

    /** 校验当前用户密码是否正确 */
    boolean verifyPassword(Long userId, String password);
}
