package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawpet.dto.LoginRequest;
import com.clawpet.dto.LoginResponse;
import com.clawpet.entity.User;
import com.clawpet.entity.UserSecurityQuestion;
import com.clawpet.mapper.UserMapper;
import com.clawpet.mapper.UserSecurityQuestionMapper;
import com.clawpet.security.JwtUtil;
import com.clawpet.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现 - 登录/注册/密码管理/密保问题
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserSecurityQuestionMapper securityMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserMapper userMapper, UserSecurityQuestionMapper securityMapper,
                           JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.securityMapper = securityMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录：校验用户名密码，生成 JWT 令牌返回
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setPhone(user.getPhone());
        userInfo.setRealName(user.getRealName());
        userInfo.setIdCard(user.getIdCard());
        userInfo.setAddress(user.getAddress());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setAuthStatus(user.getAuthStatus());
        userInfo.setRole(user.getRole());
        response.setUser(userInfo);

        return response;
    }

    /**
     * 获取当前登录用户信息（脱敏，移除密码）
     */
    @Override
    public User getCurrentUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 用户注册：创建新用户，可选添加密保问题
     */
    @Override
    @Transactional
    public void register(LoginRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );

        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());
        user.setRole("user");
        user.setAuthStatus("unauth");
        userMapper.insert(user);

        // 注册时可选设置密保问题
        if (request.getSecurityQuestion() != null && !request.getSecurityQuestion().isEmpty()
                && request.getSecurityAnswer() != null && !request.getSecurityAnswer().isEmpty()) {
            registerSecurityQuestionOnSignup(user.getId(), request.getSecurityQuestion(), request.getSecurityAnswer());
        }
    }

    /** 注册时添加第一个密保问题 */
    private void registerSecurityQuestionOnSignup(Long userId, String question, String answer) {
        UserSecurityQuestion sq = new UserSecurityQuestion();
        sq.setUserId(userId);
        sq.setQuestion(question);
        sq.setAnswer(passwordEncoder.encode(answer));
        securityMapper.insert(sq);
    }

    /**
     * 更新用户资料：自动根据实名信息更新认证状态
     */
    @Override
    @Transactional
    public void updateProfile(Long id, User user) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setId(id);
        user.setPassword(null);
        user.setUsername(null);
        user.setRole(null);

        // 自动实名认证
        String realName = user.getRealName();
        String idCard = user.getIdCard();
        if (realName != null && !realName.isEmpty() && idCard != null && !idCard.isEmpty()) {
            user.setAuthStatus("verified");
        } else if (realName != null || idCard != null) {
            user.setAuthStatus("unauth");
        }

        userMapper.updateById(user);
    }

    /**
     * 删除用户账号
     */
    @Override
    @Transactional
    public void deleteAccount(Long id) {
        userMapper.deleteById(id);
    }

    // ======== 多密保问题 ========

    /** 重置 token 过期时间：5 分钟 */
    private static final long RESET_TOKEN_EXPIRATION = 300_000;

    /**
     * 为用户添加密保问题
     */
    @Override
    @Transactional
    public void addSecurityQuestion(Long userId, String question, String answer) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        UserSecurityQuestion sq = new UserSecurityQuestion();
        sq.setUserId(userId);
        sq.setQuestion(question);
        sq.setAnswer(passwordEncoder.encode(answer));
        securityMapper.insert(sq);
    }

    /**
     * 删除用户的某个密保问题
     */
    @Override
    @Transactional
    public void deleteSecurityQuestion(Long userId, Long questionId) {
        securityMapper.delete(
                new LambdaQueryWrapper<UserSecurityQuestion>()
                        .eq(UserSecurityQuestion::getId, questionId)
                        .eq(UserSecurityQuestion::getUserId, userId)
        );
    }

    /**
     * 获取用户的所有密保问题列表（忘记密码第一步）
     */
    @Override
    public List<String> getSecurityQuestions(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) throw new RuntimeException("用户不存在");

        List<UserSecurityQuestion> list = securityMapper.selectList(
                new LambdaQueryWrapper<UserSecurityQuestion>()
                        .eq(UserSecurityQuestion::getUserId, user.getId())
                        .orderByAsc(UserSecurityQuestion::getId)
        );

        // 空列表不抛错，返回空数组，由前端决定如何展示
        return list.stream().map(sq -> sq.getId() + ":" + sq.getQuestion()).collect(Collectors.toList());
    }

    /**
     * 验证密保答案正确则返回重置密码的临时令牌（忘记密码第二步）
     */
    @Override
    public String verifySecurityAnswer(String username, Long questionId, String answer) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) throw new RuntimeException("用户不存在");

        UserSecurityQuestion sq = securityMapper.selectOne(
                new LambdaQueryWrapper<UserSecurityQuestion>()
                        .eq(UserSecurityQuestion::getId, questionId)
                        .eq(UserSecurityQuestion::getUserId, user.getId())
        );
        if (sq == null) throw new RuntimeException("密保问题不存在");

        if (!passwordEncoder.matches(answer, sq.getAnswer()))
            throw new RuntimeException("密保答案错误");

        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), RESET_TOKEN_EXPIRATION);
    }

    /**
     * 使用重置令牌设置新密码（忘记密码第三步）
     */
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!jwtUtil.validateToken(token)) throw new RuntimeException("重置链接已过期，请重新验证");

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /**
     * 验证用户密码是否正确（敏感操作前校验）
     */
    @Override
    public boolean verifyPassword(Long userId, String password) {
        User user = userMapper.selectById(userId);
        if (user == null) return false;
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 修改密码：支持旧密码验证模式和密保重置模式
     */
    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) throw new RuntimeException("用户不存在");
        // 密保模式：oldPassword 为空，跳过旧密码校验
        if (oldPassword != null && !oldPassword.isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("当前密码错误");
            }
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
