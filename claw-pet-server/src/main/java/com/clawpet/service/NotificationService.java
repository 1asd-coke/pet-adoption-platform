package com.clawpet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.Notification;

import java.util.List;

public interface NotificationService {
    void create(Long userId, String type, String title, String content, Long relatedId);
    Page<Notification> listByUser(Long userId, int page, int size);
    Long unreadCount(Long userId);
    void markRead(Long id, Long userId);
    void markAllRead(Long userId);
    void delete(Long id, Long userId);
    void deleteByRelatedIds(List<Long> relatedIds);
}
