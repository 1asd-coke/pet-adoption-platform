/**
 * api/favorite.js - 宠物收藏相关 API
 * 包含收藏列表获取、添加/取消收藏、检查收藏状态
 */
import request from './index'

/** 获取我的收藏列表 */
export function listFavorites() {
  return request.get('/api/favorite/list')
}

/** 添加收藏 @param {number|string} petId */
export function addFavorite(petId) {
  return request.post(`/api/favorite/${petId}`)
}

/** 取消收藏 @param {number|string} petId */
export function removeFavorite(petId) {
  return request.delete(`/api/favorite/${petId}`)
}

/** 检查是否已收藏指定宠物 @param {number|string} petId @returns {Promise<{data: boolean}>} */
export function checkFavorite(petId) {
  return request.get(`/api/favorite/check/${petId}`)
}
