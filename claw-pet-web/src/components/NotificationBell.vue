<template>
  <!-- 通知铃铛弹出面板：显示未读总数、最新 5 条通知，支持标记已读和跳转 -->
  <el-popover
    placement="bottom-end"
    :width="360"
    trigger="click"
    :hide-after="0"
    popper-class="notif-popover"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99" class="notif-trigger">
        <!-- 铃铛 SVG 图标 -->
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
      </el-badge>
    </template>

    <div class="notif-panel" @click.stop>
      <!-- 顶部 -->
      <div class="notif-header">
        <div class="notif-header-left">
          <span class="notif-title">消息通知</span>
          <span v-if="unreadCount" class="notif-unread-badge">{{ unreadCount }} 条未读</span>
        </div>
        <button class="notif-action" @click.stop="markAllRead" :disabled="!unreadCount">
          全部已读
        </button>
      </div>

      <!-- 列表 -->
      <div v-if="popupList.length === 0" class="notif-empty">
        <div class="notif-empty-icon">🔔</div>
        <div>暂无新消息</div>
      </div>
      <div v-else class="notif-list">
        <div
          v-for="item in popupList"
          :key="item.id"
          class="notif-item"
          :class="{ 'is-unread': !item.isRead }"
          @click.stop="handleItemClick(item)"
        >
          <!-- 按通知类型显示不同图标 -->
          <div class="notif-icon" :class="`icon-${item.type}`">
            <span v-if="item.type === 'COMMENT_REPLY'">💬</span>
            <span v-else-if="item.type === 'ADOPT_RESULT'">📋</span>
            <span v-else-if="item.type === 'NEW_APPLICATION'">📥</span>
            <span v-else>🔔</span>
          </div>
          <div class="notif-content">
            <div class="notif-item-title">{{ item.title }}</div>
            <div class="notif-item-desc">{{ item.content || '点击查看详情' }}</div>
            <div class="notif-item-time">{{ formatTime(item.createdAt) }}</div>
          </div>
          <!-- 未读红点 -->
          <div v-if="!item.isRead" class="notif-dot"></div>
        </div>
      </div>

      <!-- 底部 -->
      <div class="notif-footer">
        <button class="notif-footer-btn" @click.stop="goToNotification">查看全部消息</button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getUnreadCount, listNotifications, markAsRead } from '@/api/notification'
import { useUserStore } from '@/stores/user'
import { useWebSocket } from '@/composables/useWebSocket'

const router = useRouter()
const userStore = useUserStore()

const unreadCount = ref(0)
const popupList = ref([])

const { connect, disconnect, isConnected, onMessage } = useWebSocket()

let pollTimer = null

/**
 * 加载未读数量和最新 5 条通知
 * 若 WebSocket 已连接则跳过 HTTP 拉取
 */
async function loadData() {
  if (!userStore.isLoggedIn) {
    unreadCount.value = 0
    popupList.value = []
    return
  }
  if (isConnected.value) return
  try {
    const [countRes, listRes] = await Promise.all([
      getUnreadCount(),
      listNotifications({ page: 1, size: 5 })
    ])
    unreadCount.value = countRes.data ?? 0
    popupList.value = listRes.data?.records || []
  } catch {
    unreadCount.value = 0
  }
}

/** 处理 WebSocket 推送过来的消息 */
function handleWsMessage(data) {
  if (data.type === 'NEW_NOTIFICATION') {
    // 新通知插入列表头部，最多保留 5 条
    popupList.value.unshift({
      id: data.id,
      title: data.title,
      content: data.content,
      type: data.notificationType,
      relatedId: data.relatedId,
      petId: data.petId,
      createdAt: data.createdAt,
      isRead: 0
    })
    if (popupList.value.length > 5) popupList.value = popupList.value.slice(0, 5)
    unreadCount.value += 1
  } else if (data.type === 'UNREAD_COUNT') {
    // 服务端主动推送的未读计数
    unreadCount.value = data.count ?? 0
  }
}

// 监听登录态变化：登录时建立 WebSocket 并拉取数据，登出时断开
watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    const token = localStorage.getItem('token')
    if (token) connect(token)
    loadData()
  } else {
    disconnect()
    unreadCount.value = 0
    popupList.value = []
  }
})

/**
 * 格式化时间戳为相对时间
 * @param {string|number} t - 时间戳或日期字符串
 * @returns {string} 格式化后的文本（刚刚 / N分钟前 / N小时前 / MM-DD）
 */
function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return m + '-' + day
}

/** 跳转到消息中心 */
function goToNotification() {
  router.push('/message?tab=notification')
}

/**
 * 点击通知项：先标记已读，再根据类型跳转到对应页面
 * @param {Object} item - 通知对象
 */
async function handleItemClick(item) {
  if (!item.isRead) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  if (item.type === 'COMMENT_REPLY') {
    // 评论回复 → 跳转到宠物详情页对应评论锚点
    if (item.petId) router.push(`/pet/detail/${item.petId}#comment-${item.relatedId}`)
    else router.push('/message?tab=notification')
  } else if (item.type === 'ADOPT_RESULT') {
    // 领养结果 → 跳转到我的申请
    router.push('/adopt')
  } else if (item.type === 'NEW_APPLICATION') {
    // 新申请 → 管理员跳转到领养管理
    router.push('/admin/adopt-manage')
  } else {
    router.push('/message?tab=notification')
  }
}

/** 标记当前面板内所有未读通知为已读 */
async function markAllRead() {
  // 简单实现：标记当前显示的全部为已读
  for (const item of popupList.value) {
    if (!item.isRead) {
      try { await markAsRead(item.id) } catch {}
      item.isRead = 1
    }
  }
  unreadCount.value = 0
}

onMounted(() => {
  // 已登录状态建立 WebSocket
  if (userStore.isLoggedIn) {
    const token = localStorage.getItem('token')
    if (token) connect(token)
  }
  loadData()
  onMessage(handleWsMessage)
  // 兜底轮询（10 秒间隔）
  pollTimer = setInterval(loadData, 10000)
})

onUnmounted(() => {
  disconnect()
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.notif-trigger {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 6px;
  border-radius: 8px;
  transition: all 0.2s;
}
.notif-trigger:hover {
  background: var(--bg-tertiary);
  color: var(--brand-primary);
}

.notif-panel {
  font-size: 13px;
  color: var(--text-primary);
}

/* 头部 */
.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 4px 12px;
  border-bottom: 1px solid var(--border-light);
}
.notif-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.notif-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}
.notif-unread-badge {
  background: var(--brand-primary);
  color: #fff;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}
.notif-action {
  background: transparent;
  border: none;
  color: var(--brand-primary);
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: inherit;
}
.notif-action:hover:not(:disabled) {
  background: var(--surface-primary-strong);
}
.notif-action:disabled {
  color: var(--text-tertiary);
  cursor: not-allowed;
}

/* 空状态 */
.notif-empty {
  padding: 40px 0;
  text-align: center;
  color: var(--text-tertiary);
}
.notif-empty-icon {
  font-size: 36px;
  margin-bottom: 8px;
  opacity: 0.4;
}

/* 列表 */
.notif-list {
  max-height: 360px;
  overflow-y: auto;
  margin: 8px 0;
}
.notif-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
}
.notif-item:hover {
  background: var(--bg-tertiary);
}
.notif-item.is-unread {
  background: var(--surface-primary);
}
.notif-item.is-unread:hover {
  background: var(--surface-primary-strong);
}

.notif-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.notif-content {
  flex: 1;
  min-width: 0;
}
.notif-item-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notif-item-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.notif-item-time {
  font-size: 11px;
  color: var(--text-tertiary);
}
.notif-dot {
  position: absolute;
  top: 14px;
  right: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--brand-primary);
}

/* 底部 */
.notif-footer {
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
  text-align: center;
}
.notif-footer-btn {
  background: transparent;
  border: none;
  color: var(--brand-primary);
  font-size: 13px;
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 6px;
  font-family: inherit;
  font-weight: 500;
}
.notif-footer-btn:hover {
  background: var(--surface-primary-strong);
}
</style>
