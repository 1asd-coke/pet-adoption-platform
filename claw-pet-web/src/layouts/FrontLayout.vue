<template>
  <!-- 鼠标发光映射 -->
  <MouseGlow />
  <!-- 前台布局容器：导航栏 + 路由视口 + 页脚 -->
  <el-container class="front-layout">
    <el-affix :offset="0">
      <el-menu mode="horizontal" :ellipsis="false" :show-timeout="100" :hide-timeout="200" :default-active="activeMenu" class="front-navbar">
        <!-- Logo / 品牌标识 -->
        <el-menu-item index="logo" class="logo-item" @click="goHome">
          <span class="logo-text">Claw Pet</span>
        </el-menu-item>
        <div class="flex-grow" />
        <el-menu-item index="home" @click="$router.push('/home')">
          <el-icon><HomeFilled /></el-icon>首页
        </el-menu-item>
        <!-- 宠物分类导航 - 动态加载所有活跃分类 -->
        <el-sub-menu index="pet" :class="{ 'route-active': activeMenu === 'pet' }">
          <template #title>
            <span class="nav-title-clickable" @click.stop="$router.push('/pet')">
              <el-icon><Goods /></el-icon>宠物列表
            </span>
            <el-icon class="el-sub-menu__icon-arrow"><ArrowDown /></el-icon>
          </template>
          <el-menu-item index="pet-all" @click="$router.push('/pet')">
            <span class="cat-all">全部</span>
          </el-menu-item>
          <el-menu-item
            v-for="cat in categories"
            :key="cat.id"
            :index="'pet-cat-' + cat.id"
            @click="$router.push(`/pet?categoryId=${cat.id}`)"
          >
            {{ cat.icon }} {{ cat.name }}
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="learn" :class="{ 'route-active': activeMenu === 'learn' }">
          <template #title>
            <span class="nav-title-clickable" @click.stop="$router.push('/guide')">
              <el-icon><Reading /></el-icon>关于领养
            </span>
            <el-icon class="el-sub-menu__icon-arrow"><ArrowDown /></el-icon>
          </template>
          <el-menu-item index="guide" @click="$router.push('/guide')">
            <el-icon><Document /></el-icon>领养须知
          </el-menu-item>
          <el-menu-item index="tip" @click="$router.push('/tip')">
            <el-icon><Tickets /></el-icon>小常识
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="story" @click="$router.push('/adoption-stories')">
          <el-icon><PictureFilled /></el-icon>领养故事
        </el-menu-item>
        <!-- 联系收容所 -->
        <el-sub-menu index="shelter" :class="{ 'route-active': activeMenu === 'shelter' }">
          <template #title>
            <el-icon><Location /></el-icon>联系收容所
          </template>
          <div class="shelter-dropdown" @click.stop>
            <div class="sd-name">{{ shelter?.name || '收容所信息' }}</div>
            <div class="sd-item"><el-icon><Location /></el-icon> {{ shelter?.address || '暂无' }}</div>
            <div class="sd-item"><el-icon><Phone /></el-icon> {{ shelter?.phone || '暂无' }}</div>
            <div class="sd-item" v-if="shelter?.workHours"><el-icon><Clock /></el-icon> {{ shelter.workHours }}</div>
            <div class="sd-item" v-if="shelter?.wechat"><el-icon><ChatLineSquare /></el-icon> 微信: {{ shelter.wechat }}</div>
          </div>
        </el-sub-menu>
        <div class="flex-grow" />
        <!-- 已登录用户：通知铃铛 + 用户菜单 -->
        <div class="nav-right">
        <template v-if="userStore.isLoggedIn">
          <NotificationBell />
          <el-sub-menu index="user">
            <template #title>
              <!-- 用户头像，未设置头像时展示首字母 -->
              <el-avatar :size="28" :src="userStore.userInfo?.avatar" class="user-avatar">
                {{ userStore.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username-text">{{ userStore.userInfo?.nickname || userStore.username }}</span>
              <el-tag
                :type="userStore.isAdmin ? 'danger' : 'primary'"
                size="small"
                class="role-tag"
              >{{ userStore.isAdmin ? '管理员' : '用户' }}</el-tag>
            </template>
            <!-- 普通用户可访问我的申请 -->
            <el-menu-item index="my-adopt" v-if="!userStore.isAdmin" @click="$router.push('/adopt')">
              <el-icon><Document /></el-icon>我的申请
            </el-menu-item>
            <el-menu-item index="my-fav" @click="$router.push('/favorite')">
              <el-icon><Star /></el-icon>我的收藏
            </el-menu-item>
            <el-menu-item index="my-comments" @click="$router.push('/message?tab=my')">
              <el-icon><ChatLineSquare /></el-icon>我的评论
            </el-menu-item>
            <el-menu-item index="profile" @click="$router.push('/profile')">
              <el-icon><User /></el-icon>个人中心
            </el-menu-item>
            <!-- 管理员入口 -->
            <el-menu-item index="admin" v-if="userStore.isAdmin" @click="$router.push('/admin/dashboard')">
              <el-icon><Setting /></el-icon>管理后台
            </el-menu-item>
            <el-menu-item index="theme" @click="showThemeSwitcher">
              <el-icon><MagicStick /></el-icon>主题切换
            </el-menu-item>
            <el-divider style="margin:4px 0" />
            <el-menu-item index="logout" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-menu-item>
          </el-sub-menu>
        </template>
        <!-- 未登录用户：登录/注册按钮 -->
        <template v-else>
          <el-menu-item index="login" @click="$router.push('/login')">登录</el-menu-item>
          <el-menu-item index="register" @click="$router.push('/register')">注册</el-menu-item>
        </template>
        </div>
      </el-menu>
    </el-affix>

    <!-- 页面主体 - 带路由切换动画 -->
    <el-main class="front-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <!-- 页脚 -->
    <el-footer class="front-footer">
      <div class="footer-content">
        <div class="footer-brand">Claw Pet</div>
        <p class="footer-text">用爱给毛孩子一个家 · 宠物领养平台</p>
      </div>
    </el-footer>
  </el-container>
  <!-- 主题选择面板（右侧滑出） -->
  <ThemeSwitcher ref="themeSwitcherRef" mode="panel" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getShelter } from '@/api/shelter'
import { listCategories } from '@/api/pet'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'

// 主题切换弹窗引用
const themeSwitcherRef = ref(null)
/** 打开主题选择弹窗 */
function showThemeSwitcher() {
  themeSwitcherRef.value?.show()
}
import NotificationBell from '@/components/NotificationBell.vue'
import MouseGlow from '@/components/MouseGlow.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
// 收容所信息和分类列表（用于导航栏展示）
const shelter = ref(null)
const categories = ref([])

// 页面挂载时并行获取收容所信息与宠物分类
onMounted(async () => {
  try {
    const [shelterRes, catRes] = await Promise.all([
      getShelter(),
      listCategories()
    ])
    shelter.value = shelterRes.data
    // 只展示状态为 active 的活跃分类
    categories.value = (catRes.data || []).filter(c => c.status === 'active')
  } catch { /* ignore */ }
})

/** 退出登录：清除用户状态并跳转首页 */
function handleLogout() {
  userStore.logout()
  router.push('/home')
}

/** 根据当前路由计算激活的菜单项 */
const activeMenu = computed(() => {
  const p = route.path
  if (p === '/home') return 'home'
  if (p === '/pet' || p.startsWith('/pet?')) return 'pet'
  if (p === '/guide' || p.startsWith('/tip') || p === '/guide') return 'learn'
  if (p === '/adoption-stories' || p.startsWith('/story')) return 'story'
  if (p === '/shelter' || p.startsWith('/shelter')) return 'shelter'
  return ''
})

/** 回到首页：已在首页则强制刷新，否则导航后刷新 */
function goHome() {
  if (route.path === '/home') {
    location.reload()
  } else {
    router.push('/home').then(() => location.reload())
  }
}
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.front-navbar {
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  height: 52px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.15);
  gap: 2px;
}
.nav-right {
  display: flex;
  align-items: center;
  margin-left: 12px;
  padding-left: 12px;
  border-left: 1px solid var(--border-light);
  height: 32px;
}
.nav-right:empty { display: none; }
.nav-title-clickable {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 0 20px;
  height: 52px;
  line-height: 52px;
  color: var(--text-primary);
  transition: color 0.2s;
}
.nav-title-clickable:hover {
  color: var(--brand-primary);
}

.logo-item {
  pointer-events: auto !important;
  cursor: pointer !important;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.02em;
}

.flex-grow {
  flex-grow: 1;
}

.username-text {
  margin-left: 6px;
  font-size: 13px;
}

.role-tag {
  margin-left: 6px;
}

.shelter-dropdown {
  padding: 14px 16px;
  min-width: 260px;
  line-height: 2;
  font-size: 13px;
  color: #1d1d1f;
  cursor: default;
}

.sd-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 6px;
  padding-bottom: 6px;
  border-bottom: 0.5px solid #e5e5ea;
}

.sd-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #86868b;
  font-size: 13px;
}

.front-main {
  flex: 1;
  padding: 0;
  background: linear-gradient(135deg, var(--bg-page) 0%, var(--brand-primary-light) 50%, var(--bg-page) 100%);
  background-attachment: fixed;
}

.front-footer {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: saturate(180%) blur(16px);
  -webkit-backdrop-filter: saturate(180%) blur(16px);
  border-top: 1px solid var(--border-light);
  color: var(--text-secondary);
  text-align: center;
  padding: 18px 16px;
  height: auto !important;
  flex-shrink: 0;
  position: relative;
  z-index: 5;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.footer-brand {
  font-size: 14px;
  font-weight: 700;
  color: var(--brand-primary);
  letter-spacing: 0.05em;
}

.footer-text {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.footer-text {
  margin: 0;
  font-size: 12px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
