/**
 * stores/user.js - Pinia 用户状态管理
 * 管理登录态、用户信息、token 的读写与持久化（localStorage）
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 恢复持久化状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  // 计算属性：是否已登录 / 是否为管理员 / 用户名
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const username = computed(() => userInfo.value?.username || '')

  /**
   * 登录：调用 API，写入 token 和用户信息到 state 和 localStorage
   * @param {Object} credentials - { username, password }
   */
  async function login(credentials) {
    const res = await loginApi(credentials)
    const userData = res.data.user
    token.value = res.data.token
    userInfo.value = userData
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  /** 退出登录：清空所有状态和缓存 */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  /**
   * 获取当前用户最新信息并更新缓存
   * 若请求失败（如 token 过期）则自动退出
   */
  async function fetchUser() {
    try {
      const res = await getCurrentUser()
      userInfo.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch {
      logout()
    }
  }

  return { token, userInfo, isLoggedIn, isAdmin, username, login, logout, fetchUser }
})
