/**
 * api/index.js - axios 请求封装
 * 统一处理 baseURL、超时、请求头（token 自动注入）、响应拦截（错误提示、401 跳转）
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({ baseURL: '', timeout: 30000 })

// 请求拦截器：自动注入 Bearer token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器：统一处理业务错误码和网络错误
request.interceptors.response.use(
  response => {
    const res = response.data
    // 业务状态码非 200 视为失败
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    const status = error.response?.status
    // 401 未授权 / 403 凭证无效 → 清除本地凭证并跳转登录页
    if (status === 401 || status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (location.hash !== '#/login') {
        window.location.href = '/#/login'
      }
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
