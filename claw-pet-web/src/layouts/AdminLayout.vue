<template>
  <!-- 鼠标发光映射 -->
  <MouseGlow />
  <!-- 后台布局容器：固定侧栏 + 顶栏 + 路由内容区 -->
  <el-container class="admin-layout">
    <!-- Sidebar -->
    <el-aside width="200px" class="admin-sidebar">
      <div class="sidebar-header" @click="goHome">
        <svg class="sidebar-logo" viewBox="0 0 24 24" width="22" height="22" fill="var(--brand-primary)">
          <ellipse cx="12" cy="16" rx="5" ry="4"/>
          <circle cx="5" cy="10" r="2"/><circle cx="9" cy="6" r="2"/>
          <circle cx="15" cy="6" r="2"/><circle cx="19" cy="10" r="2"/>
        </svg>
        <span class="sidebar-title">Claw Pet</span>
      </div>
      <!-- 后台导航菜单 - 路由模式 -->
      <el-menu
        :default-active="route.path"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/pet-manage">
          <el-icon><Goods /></el-icon>
          <span>宠物管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/category-manage">
          <el-icon><Collection /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/adopt-manage">
          <el-icon><Document /></el-icon>
          <span>领养管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/record-manage">
          <el-icon><List /></el-icon>
          <span>领养记录</span>
        </el-menu-item>
        <el-menu-item index="/admin/user-manage">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/guide-manage">
          <el-icon><Document /></el-icon>
          <span>须知管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tip-manage">
          <el-icon><Reading /></el-icon>
          <span>常识管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/story-manage">
          <el-icon><PictureFilled /></el-icon>
          <span>故事管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/shelter-edit">
          <el-icon><Location /></el-icon>
          <span>收容所</span>
        </el-menu-item>
        <div class="menu-divider"></div>
        <!-- 返回前台 -->
        <el-menu-item index="/home" class="back-home">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回前台</span>
        </el-menu-item>
      </el-menu>

      <!-- 底部管理员用户信息与操作 -->
      <div class="sidebar-footer">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="admin-user">
            <el-avatar :size="28" :src="userStore.userInfo?.avatar">
              {{ userStore.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="admin-username">{{ userStore.username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-aside>

    <!-- Main Content -->
    <el-container class="admin-main-area">
      <!-- 顶栏 - 显示当前页面标题 -->
      <el-header class="admin-header">
        <h2 class="header-title">{{ headerTitle }}</h2>
        <div class="header-tags">
          <span class="header-tag">
            <span class="tag-dot"></span>
            管理后台
          </span>
        </div>
      </el-header>
      <!-- 路由视图出口 -->
      <el-main class="admin-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MouseGlow from '@/components/MouseGlow.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 路由路径 → 中文标题映射 */
const routeTitles = {
  '/admin/dashboard': '仪表盘',
  '/admin/pet-manage': '宠物管理',
  '/admin/category-manage': '分类管理',
  '/admin/adopt-manage': '领养管理',
  '/admin/record-manage': '领养记录',
  '/admin/user-manage': '用户管理',
  '/admin/shelter-edit': '收容所信息',
  '/admin/story-manage': '故事管理',
  '/admin/guide-manage': '须知管理',
  '/admin/tip-manage': '常识管理'
}

/** 根据当前路由路径计算顶栏标题 */
const headerTitle = computed(() => routeTitles[route.path] || '管理后台')

/** 处理用户菜单命令：跳转个人中心 / 退出登录 */
function handleCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

/** 点击侧栏 Logo 回到仪表盘 */
function goHome() {
  router.push('/admin/dashboard').then(() => location.reload())
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-page) 0%, var(--brand-primary-light) 50%, var(--bg-page) 100%);
  background-attachment: fixed;
}

/* ===== Sidebar ===== */
.admin-sidebar {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  width: 200px;
  border-right: 1px solid rgba(255, 255, 255, 0.5);
}

.sidebar-header {
  padding: 22px 16px 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: background 0.2s;
}
.sidebar-header:hover { background: rgba(255, 255, 255, 0.2); }

.sidebar-logo { flex-shrink: 0; }
.sidebar-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 10px 0;
}

.sidebar-menu .el-menu-item {
  color: var(--text-secondary);
  margin: 1px 8px;
  border-radius: 10px;
  height: 38px;
  line-height: 38px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.5);
  color: var(--text-primary);
  transform: translateX(2px);
}

.sidebar-menu .el-menu-item.is-active {
  background: var(--brand-primary);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px var(--shadow-primary-30);
}

.sidebar-menu .el-menu-item .el-icon {
  font-size: 16px;
  margin-right: 8px;
}

.menu-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.5), transparent);
  margin: 6px 16px;
}

.back-home { font-size: 12px; opacity: 0.7; }

.sidebar-footer {
  padding: 10px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.2);
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 10px;
  transition: background 0.2s;
}
.admin-user:hover { background: rgba(255, 255, 255, 0.4); }

.admin-username {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

/* ===== Main Area ===== */
.admin-main-area {
  margin-left: 200px;
  background: transparent;
}

.admin-header {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  height: 60px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}
.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.01em;
}
.header-tags { display: flex; gap: 8px; }
.header-tag {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 4px 12px;
  background: rgba(212, 120, 158, 0.1);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  color: var(--brand-primary);
}
.tag-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--brand-primary);
  box-shadow: 0 0 0 2px var(--shadow-primary-20);
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.7; }
}

.admin-content {
  padding: 24px;
  background: transparent;
}
</style>






