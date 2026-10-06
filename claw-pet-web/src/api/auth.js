/**
 * api/auth.js - 认证与用户相关 API
 * 包含登录注册、个人信息、密保问题、密码管理等功能
 */
import request from './index'

/** 登录 @param {Object} data - { username, password } */
export function login(data) {
  return request.post('/api/auth/login', data)
}

/** 注册 @param {Object} data - { username, password, ... } */
export function register(data) {
  return request.post('/api/auth/register', data)
}

/** 获取当前登录用户信息 */
export function getCurrentUser() {
  return request.get('/api/auth/me')
}

/** 获取验证码 */
export function getCaptcha() {
  return request.get('/api/auth/captcha')
}

/** 更新个人资料 @param {Object} data - { nickname, avatar, phone, ... } */
export function updateProfile(data) {
  return request.put('/api/profile', data)
}

/** 注销账号 */
export function deleteAccount() {
  return request.post('/api/profile/delete')
}

/** 添加一个密保问题 @param {string} question @param {string} answer */
export function addSecurityQuestion(question, answer) {
  return request.post('/api/auth/security-question', null, { params: { question, answer } })
}

/** 删除一个密保问题 @param {number|string} id */
export function deleteSecurityQuestion(id) {
  return request.delete(`/api/auth/security-question/${id}`)
}

/** 获取密保问题列表（忘记密码第一步） @param {string} username */
export function getSecurityQuestions(username) {
  return request.get('/api/auth/forgot-password', { params: { username } })
}

/** 选一个问题验证答案（忘记密码第二步） @param {string} username @param {number} questionId @param {string} answer */
export function verifyAnswer(username, questionId, answer) {
  return request.post('/api/auth/verify-answer', null, { params: { username, questionId, answer } })
}

/** 修改密码（已登录状态下用 token 鉴权） @param {Object} data - { oldPassword, newPassword } */
export function changePassword(data) {
  return request.post('/api/auth/change-password', data)
}

/** 重置密码（忘记密码第三步，用 token） @param {string} token @param {string} newPassword */
export function resetPassword(token, newPassword) {
  return request.post('/api/auth/reset-password', null, { params: { token, newPassword } })
}
