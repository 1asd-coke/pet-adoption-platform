/**
 * api/adopt.js - 领养申请相关 API
 * 包含申请提交、列表查询、审核、取消、删除等操作
 */
import request from './index'

/** 提交领养申请 @param {Object} data - { petId, reason, ... } */
export function applyAdoption(data) {
  return request.post('/api/adopt/apply', data)
}

/** 分页查询领养申请列表 @param {Object} params - { page, size, status, ... } */
export function listApplications(params) {
  return request.get('/api/adopt/list', { params })
}

/** 查询单个申请详情 @param {number|string} id */
export function getApplication(id) {
  return request.get(`/api/adopt/${id}`)
}

/** 审核领养申请（管理员） @param {Object} data - { id, status, remark } */
export function reviewApplication(data) {
  return request.put('/api/adopt/review', data)
}

/** 取消领养申请 @param {number|string} id */
export function cancelApplication(id) {
  return request.put(`/api/adopt/cancel/${id}`)
}

/** 删除领养申请 @param {number|string} id */
export function deleteApplication(id) {
  return request.delete(`/api/adopt/${id}`)
}

/** 分页查询我的领养记录 @param {Object} params */
export function listMyRecords(params) {
  return request.get('/api/adopt/my-pets', { params })
}

/** 分页查询所有领养记录（管理员用） */
export function listAllRecords(params) {
  return request.get('/api/record/list', { params })
}
