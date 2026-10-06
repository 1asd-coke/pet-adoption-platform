/**
 * api/notification.js - 消息通知相关 API
 * 包含通知列表、未读计数、标记已读、删除等操作
 */
import request from './index'

/** 分页查询通知列表 @param {Object} params - { page, size } */
export function listNotifications(params) {
  return request.get('/api/notification/list', { params })
}

/** 获取未读通知数量 */
export function getUnreadCount() {
  return request.get('/api/notification/unread-count')
}

/** 标记单条通知为已读 @param {number|string} id */
export function markAsRead(id) {
  return request.put(`/api/notification/read/${id}`)
}

/** 标记所有通知为已读 */
export function markAllAsRead() {
  return request.put('/api/notification/read-all')
}

/** 删除通知 @param {number|string} id */
export function deleteNotification(id) {
  return request.delete(`/api/notification/${id}`)
}
