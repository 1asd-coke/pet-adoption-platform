/**
 * api/user.js - 用户管理相关 API（管理员后台）
 * 包含用户列表查询和删除操作
 */
import request from './index'

/** 获取所有用户列表 */
export function listUsers() {
  return request.get('/api/users/list')
}

/** 删除指定用户（管理员） @param {number|string} id */
export function deleteUser(id) {
  return request.post('/api/users/delete', { userId: id })
}
