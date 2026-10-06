/**
 * api/story.js - 领养人和宠物（领养故事）API
 * 基于独立的 adoption_story 表
 */
import request from './index'

/** 公开：获取被展示的故事 */
export function listShowcaseStories() {
  return request.get('/api/adoption-story/list')
}

/** 领养人：发布故事 */
export function publishStory(recordId, story) {
  return request.post(`/api/adoption-story/${recordId}`, { story })
}

/** 领养人：编辑故事 */
export function updateStory(recordId, story) {
  return request.put(`/api/adoption-story/${recordId}`, { story })
}

/** 管理员：全量列表 */
export function listAllStories() {
  return request.get('/api/adoption-story/admin-list')
}

/** 登录用户：我的所有故事（含未展示的） */
export function listMyStories() {
  return request.get('/api/adoption-story/my')
}

/** 管理员：切换展示状态 */
export function toggleShowcase(storyId, showcase) {
  return request.put(`/api/adoption-story/${storyId}/showcase`, null, { params: { showcase } })
}
