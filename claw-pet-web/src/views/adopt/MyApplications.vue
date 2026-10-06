<template>
  <div class="my-applications-page">
    <div class="page-container">
      <!-- Hero 头部 -->
      <header class="page-hero">
        <div class="hero-text">
          <h1 class="page-title">我的领养申请</h1>
          <p class="page-subtitle">你提交的温暖话语，和收到的每一份回应</p>
        </div>
        <div class="hero-stats">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="22" height="22">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
            </svg>
          </div>
          <div>
            <div class="stat-num">{{ applications.length }}</div>
            <div class="stat-label">次申请</div>
          </div>
        </div>
      </header>

      <!-- 已领养宠物 -->
      <div v-if="myPets.length" class="section">
        <h3 class="section-title">
          <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18" style="vertical-align:-3px;margin-right:6px"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
          已领养宠物
        </h3>
        <div class="my-pets-grid">
          <article
            v-for="pet in myPets"
            :key="pet.id"
            class="my-pet-card"
            @click="$router.push(`/pet/detail/${pet.id}`)"
          >
            <div class="my-pet-icon">
              <img v-if="pet.image" :src="pet.image" class="my-pet-img" />
              <span v-else>🐾</span>
            </div>
            <div class="my-pet-body">
              <div class="my-pet-name">{{ pet.name }}</div>
              <div class="my-pet-breed">{{ pet.breed }}</div>
              <div class="my-pet-date">领养于 {{ formatTime(pet.adoptedAt) }}</div>
            </div>
            <span class="my-pet-arrow">→</span>
          </article>
        </div>
      </div>

      <!-- 申请记录 -->
      <div class="section">
        <h3 class="section-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="18" height="18" style="vertical-align:-3px;margin-right:6px"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          申请记录
        </h3>

        <div v-if="loading" v-loading="loading" class="list-card"></div>

        <div v-else-if="applications.length" class="apps-list">
          <article
            v-for="(app, i) in applications"
            :key="app.id"
            class="app-card"
            :style="{ animationDelay: i * 0.04 + 's' }"
          >
            <div class="app-icon">{{ app.petName?.charAt(0) || '?' }}</div>
            <div class="app-body">
              <div class="app-row">
                <span class="app-pet-name">{{ app.petName }}</span>
                <span class="app-breed">· {{ app.petBreed || app.pet?.breed || '-' }}</span>
              </div>
              <div class="app-meta">
                <span class="app-time">📅 {{ formatTime(app.createdAt) }}</span>
                <span v-if="app.status === 'rejected' && (app.rejectReason || app.remark)" class="app-reason">
                  ❌ {{ app.rejectReason || app.remark }}
                </span>
              </div>
            </div>
            <div class="app-right">
              <span class="app-status" :class="`status-${app.status}`">
                <span class="status-dot"></span>
                {{ statusLabel(app.status) }}
              </span>
              <button
                v-if="app.status === 'pending'"
                class="app-cancel"
                @click="handleCancel(app.id)"
              >取消</button>
            </div>
          </article>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">📭</div>
          <h3>还没有申请</h3>
          <p>看到喜欢的宠物就大胆申请吧</p>
          <button class="empty-btn" @click="$router.push('/pet')">去逛逛</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listApplications, cancelApplication } from '@/api/adopt'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const applications = ref([])
const myPets = ref([])

function statusLabel(status) {
  const map = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  return map[status] || status
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

async function loadList() {
  loading.value = true
  try {
    const [appRes, petRes] = await Promise.all([
      listApplications({}),
      request.get('/api/adopt/my-pets').catch(() => ({ data: [] }))
    ])
    applications.value = appRes.data?.records || appRes.data || []
    myPets.value = petRes.data?.data || []
  } finally {
    loading.value = false
  }
}

async function handleCancel(id) {
  try {
    await ElMessageBox.confirm('确定要取消这条领养申请吗？', '提示', { type: 'warning' })
    await cancelApplication(id)
    ElMessage.success('已取消')
    loadList()
  } catch {}
}

onMounted(() => {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  loadList()
})
</script>

<style scoped>
.my-applications-page {
  min-height: 100vh;
  padding-bottom: 60px;
}
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
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
  margin-bottom: 20px;
  box-shadow: 0 8px 32px var(--surface-primary);
  animation: slideDown 0.5s ease;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-12px); }
  to { opacity: 1; transform: translateY(0); }
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

/* ===== Section ===== */
.section { margin-bottom: 28px; }
.section-title {
  display: flex;
  align-items: center;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 14px;
  letter-spacing: -0.01em;
}
.section-title svg { color: var(--brand-primary); }

/* ===== 已领养宠物 ===== */
.my-pets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
.my-pet-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s;
}
.my-pet-card:hover {
  background: #fff;
  border-color: var(--brand-primary);
  box-shadow: 0 8px 24px var(--shadow-primary-25);
  transform: translateY(-2px);
}
.my-pet-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--brand-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
  overflow: hidden;
}
.my-pet-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.my-pet-body { flex: 1; min-width: 0; }
.my-pet-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}
.my-pet-breed {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.my-pet-date {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 4px;
  font-variant-numeric: tabular-nums;
}
.my-pet-arrow {
  color: var(--text-tertiary);
  font-size: 18px;
  transition: transform 0.2s;
}
.my-pet-card:hover .my-pet-arrow {
  color: var(--brand-primary);
  transform: translateX(3px);
}

/* ===== 申请记录 ===== */
.apps-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.app-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  padding: 16px 20px;
  transition: all 0.25s;
  animation: fadeIn 0.4s ease both;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.app-card:hover {
  background: #fff;
  box-shadow: 0 6px 18px rgba(0,0,0,0.05);
}

.app-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--brand-primary-light);
  color: var(--brand-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  flex-shrink: 0;
}

.app-body { flex: 1; min-width: 0; }
.app-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-size: 14px;
}
.app-pet-name {
  font-weight: 700;
  color: var(--text-primary);
  font-size: 15px;
}
.app-breed {
  color: var(--text-secondary);
  font-size: 13px;
}
.app-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
  flex-wrap: wrap;
}
.app-time {
  font-size: 12px;
  color: var(--text-tertiary);
  font-variant-numeric: tabular-nums;
}
.app-reason {
  font-size: 12px;
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.08);
  padding: 2px 8px;
  border-radius: 4px;
}

.app-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.app-status {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 8px;
  white-space: nowrap;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.status-pending { background: rgba(255, 149, 0, 0.12); color: #ff9500; }
.status-approved { background: rgba(52, 199, 89, 0.12); color: #34c759; }
.status-rejected { background: rgba(245, 108, 108, 0.12); color: #f56c6c; }

.app-cancel {
  background: transparent;
  border: 1px solid #f56c6c;
  color: #f56c6c;
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.app-cancel:hover {
  background: #f56c6c;
  color: #fff;
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 20px;
}
.empty-icon {
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
