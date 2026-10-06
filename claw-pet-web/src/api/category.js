/**
 * api/category.js - 宠物分类相关 API
 * 包含分类的 CRUD（管理员后台使用）
 */
import request from './index'

/** 分页查询分类列表 @param {Object} params - { page, size } */
export function listCategories(params) {
  return request.get('/api/category/list', { params })
}

/** 新增分类（管理员） @param {Object} data - { name, icon, status } */
export function createCategory(data) {
  return request.post('/api/category', data)
}

/** 更新分类（管理员） @param {number|string} id @param {Object} data - { name, icon, status } */
export function updateCategory(id, data) {
  return request.put(`/api/category/${id}`, data)
}

/** 删除分类（管理员） @param {number|string} id */
export function deleteCategory(id) {
  return request.delete(`/api/category/${id}`)
}
