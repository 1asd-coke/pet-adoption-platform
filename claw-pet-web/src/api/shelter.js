/**
 * api/shelter.js - 收容所信息相关 API
 * 包含收容所信息的获取与更新（管理员）
 */
import request from './index'

/** 获取收容所信息 */
export function getShelter() {
  return request.get('/api/shelter')
}

/** 更新收容所信息（管理员） @param {Object} data - { name, address, phone, workHours, wechat } */
export function updateShelter(data) {
  return request.put('/api/shelter', data)
}
