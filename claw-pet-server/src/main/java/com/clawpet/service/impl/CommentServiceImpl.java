package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.PetComment;
import com.clawpet.entity.PetInfo;
import com.clawpet.entity.User;
import com.clawpet.mapper.PetCommentMapper;
import com.clawpet.mapper.PetInfoMapper;
import com.clawpet.mapper.UserMapper;
import com.clawpet.service.CommentService;
import com.clawpet.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现 - 评论/回复的增删改查、级联通知
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final PetCommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PetInfoMapper petInfoMapper;
    private final NotificationService notificationService;

    public CommentServiceImpl(PetCommentMapper commentMapper, UserMapper userMapper,
                              PetInfoMapper petInfoMapper,
                              NotificationService notificationService) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.petInfoMapper = petInfoMapper;
        this.notificationService = notificationService;
    }

    /**
     * 填充评论的用户昵称、头像和回复目标用户名称
     */
    private void enrichComment(PetComment comment) {
        if (comment != null && comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setUserName(user.getNickname() != null && !user.getNickname().isEmpty()
                        ? user.getNickname() : user.getUsername());
                comment.setUserAvatar(user.getAvatar());
            }
        }
        // 填充回复目标用户名称
        if (comment != null && comment.getReplyToUserId() != null) {
            User replyUser = userMapper.selectById(comment.getReplyToUserId());
            if (replyUser != null) {
                comment.setReplyToUserName(replyUser.getNickname() != null && !replyUser.getNickname().isEmpty()
                        ? replyUser.getNickname() : replyUser.getUsername());
            }
        }
    }

    /**
     * 分页查询某宠物的评论（含回复），按 parentId 分组后返回顶级评论 + 子回复
     */
    @Override
    public Page<PetComment> listByPet(int page, int size, Long petId) {
        // 查所有评论（含回复），不分页回复
        List<PetComment> all = commentMapper.selectList(
                new LambdaQueryWrapper<PetComment>()
                        .eq(PetComment::getPetId, petId)
                        .eq(PetComment::getStatus, "active")
                        .orderByAsc(PetComment::getCreatedAt));

        // 填充用户信息
        all.forEach(this::enrichComment);

        // 按 parentId 分组
        Map<Long, List<PetComment>> grouped = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));

        // 取顶级评论（parentId = 0）
        List<PetComment> topLevel = grouped.getOrDefault(0L, new ArrayList<>());

        // 给每条顶级评论挂上回复
        for (PetComment comment : topLevel) {
            comment.setReplies(grouped.getOrDefault(comment.getId(), new ArrayList<>()));
        }

        // 自定义分页：取顶级评论
        int total = topLevel.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<PetComment> paged = from < total ? topLevel.subList(from, to) : new ArrayList<>();

        Page<PetComment> result = new Page<>(page, size, total);
        result.setRecords(paged);
        return result;
    }

    /**
     * 分页查询某用户发表的评论
     */
    @Override
    public Page<PetComment> listByUser(int page, int size, Long userId) {
        Page<PetComment> p = new Page<>(page, size);
        LambdaQueryWrapper<PetComment> qw = new LambdaQueryWrapper<PetComment>()
                .eq(PetComment::getUserId, userId)
                .eq(PetComment::getStatus, "active")
                .orderByDesc(PetComment::getCreatedAt);
        Page<PetComment> result = commentMapper.selectPage(p, qw);

        for (PetComment c : result.getRecords()) {
            enrichComment(c);
            // 填充宠物名称
            if (c.getPetId() != null) {
                PetInfo pet = petInfoMapper.selectById(c.getPetId());
                if (pet != null) c.setPetName(pet.getName());
            }
        }
        return result;
    }

    /**
     * 分页查询回复某用户的评论（别人回复我的）
     */
    @Override
    public Page<PetComment> listRepliedToUser(int page, int size, Long userId) {
        Page<PetComment> p = new Page<>(page, size);
        // 查询 replyToUserId = userId 的评论（别人回复该用户的）
        LambdaQueryWrapper<PetComment> qw = new LambdaQueryWrapper<PetComment>()
                .eq(PetComment::getReplyToUserId, userId)
                .eq(PetComment::getStatus, "active")
                .orderByDesc(PetComment::getCreatedAt);
        Page<PetComment> result = commentMapper.selectPage(p, qw);

        for (PetComment c : result.getRecords()) {
            enrichComment(c);
            if (c.getPetId() != null) {
                PetInfo pet = petInfoMapper.selectById(c.getPetId());
                if (pet != null) c.setPetName(pet.getName());
            }
        }
        return result;
    }

    /**
     * 新增评论或回复，如果是回复则通知被回复人
     */
    @Override
    @Transactional
    public Long add(PetComment comment) {
        comment.setStatus("active");
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        commentMapper.insert(comment);

        // 如果是回复评论，通知被回复人
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            Long replyToUserId = comment.getReplyToUserId();
            // 如果 replyToUserId 为空，取父评论的作者
            if (replyToUserId == null) {
                PetComment parent = commentMapper.selectById(comment.getParentId());
                if (parent != null) {
                    replyToUserId = parent.getUserId();
                }
            }
            // 不通知自己
            if (replyToUserId != null && !replyToUserId.equals(comment.getUserId())) {
                User replier = userMapper.selectById(comment.getUserId());
                String replierName = replier != null
                        ? (replier.getNickname() != null && !replier.getNickname().isEmpty()
                            ? replier.getNickname() : replier.getUsername())
                        : "某用户";
                notificationService.create(replyToUserId, "COMMENT_REPLY",
                        replierName + " 回复了你的评论",
                        comment.getContent(), comment.getId());
            }
        }

        return comment.getId();
    }

    /**
     * 更新评论内容
     */
    @Override
    @Transactional
    public void update(PetComment comment) {
        PetComment existing = commentMapper.selectById(comment.getId());
        if (existing == null) {
            throw new RuntimeException("评论不存在");
        }
        commentMapper.updateById(comment);
    }

    /**
     * 删除某用户的所有评论（级联删除用户时使用）
     */
    @Override
    public void deleteByUserId(Long userId) {
        commentMapper.delete(new LambdaQueryWrapper<PetComment>()
                .eq(PetComment::getUserId, userId));
    }

    /**
     * 删除评论（级联删除所有回复和关联通知）
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // 收集要删除的评论 ID（自身 + 所有回复）
        List<PetComment> replies = commentMapper.selectList(
                new LambdaQueryWrapper<PetComment>()
                        .eq(PetComment::getParentId, id));
        List<Long> idsToDelete = new ArrayList<>();
        idsToDelete.add(id);
        for (PetComment reply : replies) {
            idsToDelete.add(reply.getId());
        }

        // 删除评论及其回复
        commentMapper.deleteBatchIds(idsToDelete);

        // 级联删除关联的通知
        notificationService.deleteByRelatedIds(idsToDelete);
    }
}
