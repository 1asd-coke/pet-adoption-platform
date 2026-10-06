/**
 * api/record.js - 领养记录相关 API
 * 包含领养记录查询、回访记录添加与查询
 */
import request from './index'

/** 分页查询领养记录 @param {Object} params - { page, size, status, ... } */
export function listRecords(params) {
  return request.get('/api/record/list', { params })
}

/** 查询单条领养记录详情 @param {number|string} id */
export function getRecord(id) {
  return request.get(`/api/record/${id}`)
}

/** 添加回访记录 @param {Object} data - { recordId, content, type } */
export function addFollowup(data) {
  return request.post('/api/record/followup', data)
}

/** 查询领养记录的所有回访记录 @param {number|string} recordId */
export function listFollowups(recordId) {
  return request.get(`/api/record/followup/${recordId}`)
}
