<template>
  <div class="message-center-page page-wrapper">
    <!-- Hero -->
    <header class="page-hero">
      <div class="hero-text">
        <h1 class="page-title">消息中心</h1>
        <p class="page-subtitle">系统的回信、你的发言、别人的回应，都在这里</p>
      </div>
      <div class="hero-stats">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="22" height="22">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
        </div>
        <div>
          <div class="stat-num">{{ unreadCount }}</div>
          <div class="stat-label">条未读</div>
        </div>
      </div>
    </header>

    <!-- Tabs -->
    <div class="tab-bar">
      <button class="tab-btn" :class="{ active: activeTab === 'notification' }" @click="switchTab('notification')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14">
          <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>
        </svg>
        系统通知
        <span class="tab-badge">{{ notifCount }}</span>
        <span v-if="unreadCount" class="tab-dot"></span>
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'my' }" @click="switchTab('my')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
        </svg>
        我的评论
        <span class="tab-badge">{{ myCount }}</span>
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'replied' }" @click="switchTab('replied')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14">
          <polyline points="9 17 4 12 9 7"/><path d="M20 18v-2a4 4 0 0 0-4-4H4"/>
        </svg>
        被回复
        <span class="tab-badge">{{ repliedCount }}</span>
      </button>
      <div class="tab-actions">
        <button v-if="activeTab === 'notification'" class="tab-action-btn" @click="markAllRead">
          全部已读
        </button>
      </div>
    </div>

    <!-- 系统通知列表 -->
    <div v-if="activeTab === 'notification'" v-loading="loadingNotif" class="message-list">
      <div v-if="!notifications.length && !loadingNotif" class="empty-state">
        <div class="empty-illust">🔔</div>
        <h3>暂无通知</h3>
        <p>系统会在这里通知你领养结果、评论回复等消息</p>
      </div>
      <article
        v-for="item in notifications"
        :key="item.id"
        class="msg-card"
        :class="{ unread: !item.isRead }"
        @click="handleNotifClick(item)"
      >
        <div class="msg-icon" :class="`icon-${item.type?.toLowerCase()}`">
          <svg v-if="item.type === 'COMMENT_REPLY'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <svg v-else-if="item.type === 'ADOPT_RESULT'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
          </svg>
        </div>
        <div class="msg-body">
          <div class="msg-title">
            <span class="title-text">{{ item.title }}</span>
            <span v-if="item.typeLabel" class="type-tag">{{ item.typeLabel }}</span>
          </div>
          <div v-if="item.content" class="msg-content">{{ item.content }}</div>
          <div class="msg-time">{{ formatTime(item.createdAt) }}</div>
        </div>
        <button class="msg-delete" @click.stop="removeNotif(item)" title="删除">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14">
            <polyline points="3 6 5 6 21 6"/><path d="M19 6l-2 14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2L5 6"/>
          </svg>
        </button>
      </article>
      <div v-if="notifTotal > 20" class="pagination-wrap">
        <el-pagination background layout="prev, pager, next" :total="notifTotal" :page-size="20" :current-page="notifPage" @current-change="loadNotifPage" />
      </div>
    </div>

    <!-- 我的评论列表 -->
    <div v-else-if="activeTab === 'my'" v-loading="loadingMy" class="message-list">
      <div v-if="!myComments.length && !loadingMy" class="empty-state">
        <div class="empty-illust">
          <svg viewBox="0 0 100 100" width="100" height="100" fill="none">
            <ellipse cx="50" cy="70" rx="28" ry="22" fill="var(--brand-primary-light)"/>
            <path d="M30 50 C 30 40 40 30 50 30 C 60 30 70 40 70 50 C 70 60 60 68 50 68 C 40 68 30 60 30 50 Z" fill="var(--brand-primary)" opacity="0.7"/>
            <ellipse cx="42" cy="48" rx="3" ry="3.5" fill="#fff"/>
            <ellipse cx="58" cy="48" rx="3" ry="3.5" fill="#fff"/>
            <path d="M44 60 L 50 65 L 56 60" stroke="#fff" stroke-width="1.5" fill="none" stroke-linecap="round"/>
          </svg>
        </div>
        <h3>还没有评论</h3>
        <p>看到喜欢的宠物就大胆留言吧</p>
        <button class="empty-btn" @click="$router.push('/pet')">去逛逛</button>
      </div>
      <article
        v-for="c in myComments"
        :key="c.id"
        class="msg-card"
        @click="$router.push(`/pet/detail/${c.petId}#comment-${c.id}`)"
      >
        <div class="msg-icon icon-comment">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <div class="msg-body">
          <div class="msg-title">
            <span class="title-text">评论了 <b class="pet-name">{{ c.petName || '宠物' }}</b></span>
            <span class="type-tag">我的评论</span>
          </div>
          <div class="msg-content">{{ c.content }}</div>
          <div class="msg-time">{{ formatTime(c.createdAt) }}</div>
        </div>
        <button class="msg-delete" @click.stop="deleteMyComment(c)" title="删除">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14">
            <polyline points="3 6 5 6 21 6"/><path d="M19 6l-2 14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2L5 6"/>
          </svg>
        </button>
      </article>
    </div>

    <!-- 被回复列表 -->
    <div v-else-if="activeTab === 'replied'" v-loading="loadingReplied" class="message-list">
      <div v-if="!repliedComments.length && !loadingReplied" class="empty-state">
        <div class="empty-illust">💬</div>
        <h3>还没有被回复</h3>
        <p>收到回复时会在这里通知你</p>
      </div>
      <article
        v-for="c in repliedComments"
        :key="c.id"
        class="msg-card"
        @click="$router.push(`/pet/detail/${c.petId}#comment-${c.parentId || c.id}`)"
      >
        <div class="msg-icon icon-reply">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
            <polyline points="9 17 4 12 9 7"/><path d="M20 18v-2a4 4 0 0 0-4-4H4"/>
          </svg>
        </div>
        <div class="msg-body">
          <div class="msg-title">
            <span class="title-text"><b>{{ c.replyUserName || '用户' }}</b> 回复了你</span>
            <span class="type-tag">被回复</span>
          </div>
          <div class="msg-content">{{ c.replyContent || c.content }}</div>
          <div class="msg-time">在 <b>{{ c.petName }}</b> · {{ formatTime(c.createdAt) }}</div>
        </div>
        <button class="msg-delete" @click.stop="deleteMyComment(c)" title="删除">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14">
            <polyline points="3 6 5 6 21 6"/><path d="M19 6l-2 14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2L5 6"/>
          </svg>
        </button>
      </article>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listNotifications, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'
import { getMyComments, getRepliedComments, deleteComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref(route.query.tab || 'notification')

const loadingNotif = ref(false)
const notifications = ref([])
const notifTotal = ref(0)
const notifPage = ref(1)

const loadingMy = ref(false)
const myComments = ref([])

const loadingReplied = ref(false)
const repliedComments = ref([])

const notifCount = computed(() => notifications.value.length)
const myCount = computed(() => myComments.value.length)
const repliedCount = computed(() => repliedComments.value.length)
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

function switchTab(tab) {
  activeTab.value = tab
  router.replace({ query: { ...route.query, tab } })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  if (d.getFullYear() === now.getFullYear()) return month + '-' + day
  return d.getFullYear() + '-' + month + '-' + day
}

async function loadNotifPage(page) {
  notifPage.value = page
  loadingNotif.value = true
  try {
    const res = await listNotifications({ page, size: 20 })
    notifications.value = res.data?.records || []
    notifTotal.value = res.data?.total || 0
  } finally {
    loadingNotif.value = false
  }
}

async function loadMy() {
  loadingMy.value = true
  try {
    const res = await getMyComments({})
    myComments.value = res.data?.records || res.data || []
  } finally {
    loadingMy.value = false
  }
}

async function loadReplied() {
  loadingReplied.value = true
  try {
    const res = await getRepliedComments({})
    repliedComments.value = res.data?.records || res.data || []
  } finally {
    loadingReplied.value = false
  }
}

async function markAllRead() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => (n.isRead = 1))
    ElMessage.success('已全部标记已读')
  } catch {}
}

async function handleNotifClick(item) {
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
    } catch {}
  }
  if (item.type === 'COMMENT_REPLY' && item.petId) {
    router.push(`/pet/detail/${item.petId}#comment-${item.relatedId}`)
  } else if (item.type === 'ADOPT_RESULT') {
    switchTab('my')
  }
}

async function removeNotif(item) {
  try {
    await ElMessageBox.confirm('确定删除这条通知？', '提示', { type: 'warning' })
    await deleteNotification(item.id)
    notifications.value = notifications.value.filter(n => n.id !== item.id)
    notifTotal.value--
    ElMessage.success('已删除')
  } catch {}
}

async function deleteMyComment(c) {
  try {
    await ElMessageBox.confirm('确定删除这条评论？', '提示', { type: 'warning' })
    await deleteComment(c.id)
    myComments.value = myComments.value.filter(x => x.id !== c.id)
    ElMessage.success('已删除')
  } catch {}
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  loadNotifPage(1)
  loadMy()
  loadReplied()
})
</script>

<style scoped>
.message-center-page {
  min-height: 100vh;
  padding-bottom: 60px;
}

/* ===== Hero ===== */
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 24px 32px;
  margin-bottom: 16px;
  box-shadow: 0 8px 32px var(--surface-primary);
}
.hero-text h1 {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 4px;
  letter-spacing: -0.02em;
}
.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}
.hero-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--brand-primary);
  color: #fff;
  padding: 12px 20px;
  border-radius: 14px;
  box-shadow: 0 4px 16px var(--shadow-primary-25);
}
.stat-icon { display: flex; align-items: center; }
.stat-num {
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 2px;
}

/* ===== Tabs ===== */
.tab-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 14px;
  padding: 6px;
}
.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  border-radius: 10px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
  position: relative;
}
.tab-btn:hover { background: rgba(255, 255, 255, 0.5); color: var(--text-primary); }
.tab-btn.active {
  background: var(--brand-primary);
  color: #fff;
  box-shadow: 0 2px 8px var(--shadow-primary-30);
}
.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: rgba(255, 255, 255, 0.4);
  color: inherit;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
}
.tab-btn.active .tab-badge {
  background: rgba(255, 255, 255, 0.25);
}
.tab-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  background: #ff5e8a;
  border-radius: 50%;
  box-shadow: 0 0 0 2px var(--bg-card);
}
.tab-actions { margin-left: auto; }
.tab-action-btn {
  background: transparent;
  border: 1px solid var(--border-light);
  color: var(--brand-primary);
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.tab-action-btn:hover {
  background: var(--brand-primary);
  color: #fff;
  border-color: var(--brand-primary);
}

/* ===== 列表 ===== */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.msg-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 14px;
  padding: 14px 18px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}
.msg-card:hover {
  background: #fff;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}
.msg-card.unread {
  background: rgba(120, 90, 200, 0.05);
  border-color: rgba(120, 90, 200, 0.15);
}
.msg-card.unread::before {
  content: '';
  position: absolute;
  left: 0; top: 14px; bottom: 14px;
  width: 3px;
  background: var(--brand-primary);
  border-radius: 0 2px 2px 0;
}

.msg-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.icon-comment_reply, .icon-comment { background: rgba(55, 138, 221, 0.12); color: #378add; }
.icon-adopt_result { background: rgba(52, 199, 89, 0.12); color: #34c759; }
.icon-new_application { background: rgba(255, 149, 0, 0.12); color: #ff9500; }
.icon-reply { background: rgba(120, 90, 200, 0.12); color: var(--brand-primary); }
.icon-default { background: var(--bg-tertiary); color: var(--text-secondary); }

.msg-body { flex: 1; min-width: 0; }
.msg-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}
.title-text { font-weight: 600; }
.pet-name { color: var(--brand-primary); font-weight: 600; }
.type-tag {
  font-size: 11px;
  color: var(--text-tertiary);
  background: var(--bg-tertiary);
  padding: 1px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.msg-content {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 6px;
  line-height: 1.5;
  word-break: break-word;
}
.msg-time {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 6px;
  font-variant-numeric: tabular-nums;
}

.msg-delete {
  background: transparent;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  transition: all 0.2s;
  flex-shrink: 0;
  opacity: 0;
}
.msg-card:hover .msg-delete { opacity: 1; }
.msg-delete:hover {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.08);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  margin-top: 8px;
}
.empty-illust {
  font-size: 60px;
  margin-bottom: 16px;
  opacity: 0.6;
}
.empty-state h3 {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px;
}
.empty-state p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 18px;
}
.empty-btn {
  background: var(--brand-primary);
  color: #fff;
  border: none;
  padding: 10px 28px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
  box-shadow: 0 4px 14px var(--shadow-primary-30);
}
.empty-btn:hover {
  background: var(--brand-primary-active);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px var(--shadow-primary-40);
}
</style>
