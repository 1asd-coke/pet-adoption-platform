package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.Notification;
import com.clawpet.mapper.NotificationMapper;
import com.clawpet.mapper.PetCommentMapper;
import com.clawpet.service.NotificationService;
import com.clawpet.websocket.NotificationWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务实现 - 通知的创建、查询、已读管理和 WebSocket 实时推送
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final PetCommentMapper petCommentMapper;
    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> TYPE_LABEL_MAP = Map.of(
            "COMMENT_REPLY", "评论回复",
            "ADOPT_RESULT", "领养结果",
            "NEW_APPLICATION", "新申请"
    );

    public NotificationServiceImpl(NotificationMapper notificationMapper,
                                    PetCommentMapper petCommentMapper,
                                    NotificationWebSocketHandler webSocketHandler,
                                    ObjectMapper objectMapper) {
        this.notificationMapper = notificationMapper;
        this.petCommentMapper = petCommentMapper;
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建通知并保存到数据库，通过 WebSocket 实时推送给用户
     */
    @Override
    @Transactional
    public void create(Long userId, String type, String title, String content, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        notificationMapper.insert(n);

        // 通过 WebSocket 实时推送
        pushToUser(userId, n);
    }

    /**
     * 将新通知通过 WebSocket 推送给用户
     */
    private void pushToUser(Long userId, Notification notification) {
        try {
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("type", "NEW_NOTIFICATION");
            msg.put("id", notification.getId());
            msg.put("notificationType", notification.getType());
            msg.put("typeLabel", TYPE_LABEL_MAP.getOrDefault(notification.getType(), notification.getType()));
            msg.put("title", notification.getTitle());
            msg.put("content", notification.getContent());
            msg.put("relatedId", notification.getRelatedId());
            msg.put("createdAt", notification.getCreatedAt());
            // 如果是 COMMENT_REPLY，补充 petId
            Long petId = null;
            if ("COMMENT_REPLY".equals(notification.getType()) && notification.getRelatedId() != null) {
                petId = petCommentMapper.selectPetIdById(notification.getRelatedId());
            }
            if (petId != null) msg.put("petId", petId);

            // 发两个消息：新通知 + 未读数变更
            String json = objectMapper.writeValueAsString(msg);
            webSocketHandler.sendNotification(userId, json);

            // 同时推送一个简洁的未读数更新消息
            long unreadCount = unreadCount(userId);
            Map<String, Object> unreadMsg = new LinkedHashMap<>();
            unreadMsg.put("type", "UNREAD_COUNT");
            unreadMsg.put("count", unreadCount);
            String unreadJson = objectMapper.writeValueAsString(unreadMsg);
            webSocketHandler.sendNotification(userId, unreadJson);
        } catch (JsonProcessingException e) {
            // JSON 序列化失败，忽略推送
        }
    }

    /**
     * 分页查询用户的通知列表，填充 typeLabel 和 petId
     */
    @Override
    public Page<Notification> listByUser(Long userId, int page, int size) {
        Page<Notification> p = new Page<>(page, size);
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);
        Page<Notification> result = notificationMapper.selectPage(p, qw);
        // 填充 typeLabel 和 petId 给前端显示
        for (Notification n : result.getRecords()) {
            n.setTypeLabel(TYPE_LABEL_MAP.getOrDefault(n.getType(), n.getType()));
            // COMMENT_REPLY 类型：通过 relatedId（评论ID）查 petId
            if ("COMMENT_REPLY".equals(n.getType()) && n.getRelatedId() != null) {
                Long petId = petCommentMapper.selectPetIdById(n.getRelatedId());
                if (petId != null) n.setPetId(petId);
            }
        }
        return result;
    }

    /**
     * 查询用户未读通知数量
     */
    @Override
    public Long unreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
    }

    /**
     * 标记单条通知为已读
     */
    @Override
    @Transactional
    public void markRead(Long id, Long userId) {
        Notification n = notificationMapper.selectOne(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getId, id)
                        .eq(Notification::getUserId, userId));
        if (n != null) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    /**
     * 标记用户所有通知为已读
     */
    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .set(Notification::getIsRead, 1));
    }

    /**
     * 删除单条通知
     */
    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        notificationMapper.delete(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getId, id)
                        .eq(Notification::getUserId, userId));
    }

    /**
     * 根据关联 ID 批量删除通知（评论删除时级联）
     */
    @Override
    @Transactional
    public void deleteByRelatedIds(List<Long> relatedIds) {
        if (relatedIds == null || relatedIds.isEmpty()) return;
        notificationMapper.delete(
                new LambdaQueryWrapper<Notification>()
                        .in(Notification::getRelatedId, relatedIds)
                        .eq(Notification::getType, "COMMENT_REPLY"));
    }
}
