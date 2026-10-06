/**
 * api/comment.js - 评论相关 API
 * 包含宠物评论的增删查，以及我收到的回复查询
 */
import request from './index'

/** 获取某个宠物的评论列表 @param {number|string} petId @param {Object} params - { page, size, sort, ... } */
export function listComments(petId, params) {
  return request.get(`/api/comment/list/${petId}`, { params })
}

/** 获取我发表的评论 @param {Object} params - { page, size } */
export function getMyComments(params) {
  return request.get('/api/comment/my', { params })
}

/** 获取我收到的回复 @param {Object} params - { page, size } */
export function getRepliedComments(params) {
  return request.get('/api/comment/replied', { params })
}

/** 添加评论 @param {Object} data - { petId, content, parentId? } */
export function addComment(data) {
  return request.post('/api/comment', data)
}

/** 删除评论 @param {number|string} id */
export function deleteComment(id) {
  return request.delete(`/api/comment/${id}`)
}
