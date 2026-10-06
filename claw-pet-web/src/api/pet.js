/**
 * api/pet.js - 宠物相关 API
 * 包含宠物的 CRUD 以及分类列表查询
 */
import request from './index'

/** 分页查询宠物列表 @param {Object} params - { page, size, categoryId, keyword, ... } */
export function listPets(params) {
  return request.get('/api/pet/list', { params })
}

/** 查询宠物详情 @param {number|string} id */
export function getPet(id) {
  return request.get(`/api/pet/${id}`)
}

/** 新增宠物（管理员） @param {FormData|Object} data */
export function createPet(data) {
  return request.post('/api/pet', data)
}

/** 更新宠物信息（管理员） @param {Object} data - { id, name, categoryId, ... } */
export function updatePet(data) {
  return request.put('/api/pet', data)
}

/** 删除宠物（管理员） @param {number|string} id */
export function deletePet(id) {
  return request.delete(`/api/pet/${id}`)
}

/** 获取所有分类（用于下拉选择等） */
export function listCategories() {
  return request.get('/api/category/list')
}
