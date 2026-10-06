/**
 * api/guide.js - 领养须知 API
 */
import request from './index'

/** 获取领养须知 */
export function getGuide() {
  return request.get('/api/guide')
}

/** 新增/更新某一章节（管理员，id 为空时新增） */
export function updateGuide(data) {
  return request.put('/api/guide', data)
}

/** 删除某一章节（管理员） */
export function deleteGuide(id) {
  return request.delete(`/api/guide/${id}`)
}
