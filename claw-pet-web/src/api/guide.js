/**
 * api/guide.js - 领养须知 API
 */
import request from './index'

/** 获取领养须知 */
export function getGuide() {
  return request.get('/api/guide')
}

/** 更新领养须知（管理员） */
export function updateGuide(data) {
  return request.put('/api/guide', data)
}
