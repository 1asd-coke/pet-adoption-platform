/**
 * api/stats.js - 数据统计相关 API
 * 包含首页统计和后台仪表盘统计
 */
import request from './index'

/** 获取首页统计数据（宠物总数、领养成功数等） */
export function getHomeStats() {
  return request.get('/api/stats/home')
}

/** 获取管理后台仪表盘数据（趋势、分布等） */
export function getDashboardStats() {
  return request.get('/api/stats/dashboard')
}
