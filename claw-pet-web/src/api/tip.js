/**
 * api/tip.js - 小常识 API
 */
import request from './index'

/** 分页查询小常识 */
export function listTips(params) {
  return request.get('/api/tip/list', { params })
}

/** 获取小常识详情 */
export function getTip(id) {
  return request.get(`/api/tip/${id}`)
}

/** 新增小常识（管理员） */
export function addTip(data) {
  return request.post('/api/tip', data)
}

/** 更新小常识（管理员） */
export function updateTip(data) {
  return request.put('/api/tip', data)
}

/** 删除小常识（管理员） */
export function deleteTip(id) {
  return request.delete(`/api/tip/${id}`)
}
