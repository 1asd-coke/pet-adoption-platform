/**
 * router/index.js - Vue Router 配置
 * 定义前台（FrontLayout）和后台（AdminLayout）两套路由结构
 */
import { createRouter, createWebHashHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  // 登录页（独立布局，不嵌套在 FrontLayout 中）
  {
    path: '/login',
    component: () => import('@/views/auth/AuthView.vue')
  },
  {
    path: '/register',
    component: () => import('@/views/auth/AuthView.vue')
  },
  {
    path: '/forgot-password',
    component: () => import('@/views/auth/ForgotPassword.vue')
  },
  // 前台路由 - 统一使用 FrontLayout 包裹
  {
    path: '/',
    component: () => import('@/layouts/FrontLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: '/home', component: () => import('@/views/home/HomeView.vue') },
      { path: '/pet', component: () => import('@/views/pet/PetList.vue') },
      { path: '/pet/detail/:id', component: () => import('@/views/pet/PetDetail.vue') },
      { path: '/adopt', component: () => import('@/views/adopt/MyApplications.vue') },
      { path: '/message', component: () => import('@/views/message/MessageCenter.vue') },
      { path: '/notification', redirect: '/message?tab=notification' },
      { path: '/favorite', component: () => import('@/views/favorite/MyFavorites.vue') },
      { path: '/my-comments', redirect: '/message?tab=my' },
      { path: '/profile', component: () => import('@/views/profile/ProfileView.vue') },
      // 领养须知 / 小常识 / 领养人和宠物
      { path: '/guide', component: () => import('@/views/guide/GuideView.vue') },
      { path: '/tip', component: () => import('@/views/tip/TipList.vue') },
      { path: '/tip/:id', component: () => import('@/views/tip/TipDetail.vue') },
      { path: '/adoption-stories', component: () => import('@/views/story/AdoptionStory.vue') },
      { path: '/story/editor', component: () => import('@/views/story/StoryEditor.vue') }
    ]
  },
  // 后台管理路由 - 统一使用 AdminLayout 包裹
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: '/admin/dashboard', component: () => import('@/views/admin/DashboardView.vue') },
      { path: '/admin/pet-manage', component: () => import('@/views/admin/PetManage.vue') },
      { path: '/admin/category-manage', component: () => import('@/views/admin/CategoryManage.vue') },
      { path: '/admin/adopt-manage', component: () => import('@/views/admin/AdoptManage.vue') },
      { path: '/admin/record-manage', component: () => import('@/views/admin/RecordManage.vue') },
      { path: '/admin/user-manage', component: () => import('@/views/admin/UserManage.vue') },
      { path: '/admin/shelter-edit', component: () => import('@/views/admin/ShelterEdit.vue') },
      { path: '/admin/guide-manage', component: () => import('@/views/admin/GuideManage.vue') },
      { path: '/admin/tip-manage', component: () => import('@/views/admin/TipManage.vue') },
      { path: '/admin/story-manage', component: () => import('@/views/admin/StoryManage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  // 控制滚动行为：默认滚顶，但保留浏览器前进/后退的位置，以及 hash 锚点跳转
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth', top: 80 }
    return { top: 0 }
  }
})

// 全局守卫：管理后台需登录 + 校验 JWT 是否过期
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path.startsWith('/admin')) {
    // 未登录
    if (!token) {
      ElMessage.error('请先登录')
      return next('/login')
    }
    // 校验 token 是否过期（解码 JWT payload 中的 exp 字段）
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      if (payload.exp * 1000 < Date.now()) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.error('登录已过期，请重新登录')
        return next('/login')
      }
    } catch {
      // token 格式异常，清除并跳转登录
      localStorage.removeItem('token')
      return next('/login')
    }
  }
  next()
})

export default router
