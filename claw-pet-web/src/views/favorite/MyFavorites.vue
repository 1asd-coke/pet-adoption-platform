<template>
  <div class="my-favorites-page">
    <div class="page-container">
      <!-- Hero 头部 -->
      <header class="page-hero">
        <div class="hero-text">
          <h1 class="page-title">我的收藏</h1>
          <p class="page-subtitle">你心心念念的它们都在这里</p>
        </div>
        <div class="hero-stats">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
          </div>
          <div>
            <div class="stat-num">{{ favorites.length }}</div>
            <div class="stat-label">只小家伙</div>
          </div>
        </div>
      </header>

      <!-- 网格 -->
      <div v-loading="loading" class="favorites-grid">
        <div class="fav-grid" v-if="favorites.length">
          <article
            v-for="(fav, i) in favorites"
            :key="fav.id"
            class="fav-card"
            :style="{ animationDelay: i * 0.05 + 's' }"
            @click="goToPet(fav)"
          >
            <div class="fav-cover-wrap">
              <img :src="fav.petImageUrls?.[0] || defaultCover" class="fav-cover" />
              <span class="fav-status" :class="`status-${fav.petStatus}`">
                <span class="status-dot"></span>
                {{ fav.petStatus === 'available' ? '待领养' : fav.petStatus === 'adopting' ? '领养中' : '已领养' }}
              </span>
              <button class="remove-btn" @click.stop="handleRemove(fav)" title="取消收藏">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-2 14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
              </button>
            </div>
            <div class="fav-body">
              <h4 class="fav-name">{{ fav.petName || '未知' }}</h4>
              <p class="fav-breed">{{ fav.breed || '未知品种' }}</p>
              <div class="fav-meta">
                <span v-if="fav.petAge" class="fav-tag">{{ fav.petAge }}岁</span>
                <span v-if="fav.petWeight" class="fav-tag">{{ fav.petWeight }}kg</span>
                <span v-if="fav.petGender" class="fav-tag" :class="`gender-${fav.petGender}`">
                  {{ fav.petGender === 'male' ? '♂' : '♀' }}
                </span>
              </div>
            </div>
          </article>
        </div>

        <div v-else-if="!loading" class="empty-state">
          <div class="empty-illust">
            <svg viewBox="0 0 80 80" width="80" height="80" fill="none">
              <ellipse cx="40" cy="56" rx="22" ry="18" fill="var(--brand-primary-light)"/>
              <path d="M28 38 C 28 32 35 26 40 26 C 45 26 52 32 52 38 C 52 45 45 50 40 50 C 35 50 28 45 28 38 Z" fill="var(--brand-primary)" opacity="0.7"/>
              <ellipse cx="35" cy="36" rx="2" ry="2.5" fill="#fff"/>
              <ellipse cx="45" cy="36" rx="2" ry="2.5" fill="#fff"/>
            </svg>
          </div>
          <h3>还没有收藏</h3>
          <p>去宠物列表页点击爱心收藏你喜欢的小家伙</p>
          <button class="empty-btn" @click="$router.push('/pet')">去看看</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listFavorites, removeFavorite } from '@/api/favorite'
import { ElMessage } from 'element-plus'

const router = useRouter()
const defaultCover = 'https://via.placeholder.com/200x160?text=No+Image'
const loading = ref(false)
const favorites = ref([])

async function loadList() {
  loading.value = true
  try {
    const res = await listFavorites()
    favorites.value = res.data?.records || res.data || []
  } finally {
    loading.value = false
  }
}

function goToPet(fav) {
  router.push(`/pet/detail/${fav.petId || fav.id}`)
}

async function handleRemove(fav) {
  try {
    await removeFavorite(fav.petId || fav.id)
    ElMessage.success('已取消收藏')
    favorites.value = favorites.value.filter(f => f.id !== fav.id)
  } catch {}
}

onMounted(loadList)
</script>

<style scoped>
.my-favorites-page {
  min-height: 100vh;
  padding-bottom: 60px;
}
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px 0;
}

/* ===== Hero ===== */
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 24px 32px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px var(--surface-primary);
  animation: slideDown 0.5s ease;
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
  padding: 12px 20px;
  border-radius: 14px;
  color: #fff;
  box-shadow: 0 4px 16px var(--shadow-primary-25);
}
.stat-icon { display: flex; }
.stat-num { font-size: 22px; font-weight: 800; line-height: 1; }
.stat-label { font-size: 11px; opacity: 0.9; margin-top: 2px; letter-spacing: 0.1em; }

/* ===== Grid ===== */
.favorites-grid { min-height: 200px; }
.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}
.fav-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  transform: translateY(20px);
  animation: cardSlide 0.5s ease forwards;
  box-shadow: 0 2px 8px rgba(120, 90, 200, 0.05);
}
.fav-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px var(--shadow-primary-hover);
  background: rgba(255, 255, 255, 0.85);
}
.fav-card:hover .fav-cover { transform: scale(1.08); }
.fav-card:hover .remove-btn { opacity: 1; }

@keyframes cardSlide {
  to { opacity: 1; transform: translateY(0); }
}

.fav-cover-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f0eaf6;
}
.fav-cover {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.fav-status {
  position: absolute;
  top: 10px; left: 10px;
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 500;
  backdrop-filter: blur(8px);
  background: rgba(255, 255, 255, 0.85);
}
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-available { color: var(--brand-primary); }
.status-available .status-dot { background: var(--brand-primary); box-shadow: 0 0 0 2px var(--shadow-primary-20); }
.status-adopting { color: #ff9500; }
.status-adopting .status-dot { background: #ff9500; }
.status-adopted { color: #34c759; }
.status-adopted .status-dot { background: #34c759; }

.remove-btn {
  position: absolute;
  top: 10px; right: 10px;
  width: 30px; height: 30px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border: 0.5px solid rgba(255, 94, 138, 0.3);
  color: #ff5e8a;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  opacity: 0;
  transition: all 0.25s ease;
}
.remove-btn:hover {
  background: #ff5e8a;
  color: #fff;
  transform: scale(1.1);
}

.fav-body { padding: 12px 14px; }
.fav-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 2px;
  letter-spacing: -0.01em;
}
.fav-breed {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.fav-meta { display: flex; gap: 4px; flex-wrap: wrap; }
.fav-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 6px;
  background: var(--surface-primary);
  color: var(--brand-primary);
}
.fav-tag.gender { font-size: 13px; padding: 0 4px; background: transparent; }
.fav-tag.gender-male { color: #378add; }
.fav-tag.gender-female { color: #ff5e8a; }

/* ===== Empty ===== */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  box-shadow: 0 8px 32px var(--surface-primary);
  animation: slideDown 0.5s ease 0.1s both;
}
.empty-illust { margin-bottom: 16px; opacity: 0.7; }
.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
}
.empty-state p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 20px;
}
.empty-btn {
  background: var(--brand-primary);
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  box-shadow: 0 4px 14px var(--shadow-primary-30);
  transition: all 0.2s;
}
.empty-btn:hover { transform: translateY(-2px); }

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 600px) {
  .page-hero { flex-direction: column; gap: 12px; text-align: center; }
  .fav-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
}
</style>
