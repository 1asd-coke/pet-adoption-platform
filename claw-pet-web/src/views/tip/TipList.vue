<template>
  <div class="tip-list-page page-wrapper">
    <div class="page-bg-deco">
      <div class="deco-circle c1"></div>
      <div class="deco-circle c2"></div>
      <div class="deco-circle c3"></div>
    </div>

    <header class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 11h.01M15 11h.01M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z"/>
            <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
            <path d="M9 9L3.5 6.5M15 9l5.5-2.5"/>
          </svg>
        </div>
        <div>
          <h1 class="page-title">小常识</h1>
          <p class="page-subtitle">养宠知识 · 领养注意事项 · 宠物健康护理</p>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-item">
          <div class="stat-num">{{ total }}</div>
          <div class="stat-label">篇知识</div>
        </div>
      </div>
    </header>

    <!-- 分类快速导航（待后端支持 category 字段后启用） -->
    <!-- <div class="filter-bar">
      <button
        class="filter-chip"
        :class="{ active: !activeCategory }"
        @click="activeCategory = null; loadPage(1)"
      >全部</button>
      <button
        v-for="cat in categories"
        :key="cat"
        class="filter-chip"
        :class="{ active: activeCategory === cat }"
        @click="activeCategory = cat; loadPage(1)"
      >{{ cat }}</button>
    </div> -->

    <div v-loading="loading" class="tip-grid">
      <article
        v-for="(t, idx) in list"
        :key="t.id"
        class="tip-card"
        :style="{ animationDelay: `${idx * 0.06}s` }"
        @click="$router.push(`/tip/${t.id}`)"
      >
        <div class="tip-card-head">
          <div class="tip-tag">{{ t.category || '常识' }}</div>
          <span class="tip-date">{{ formatDate(t.publishDate) }}</span>
        </div>
        <h3 class="tip-title">{{ t.title }}</h3>
        <p class="tip-preview">{{ preview(t.content) }}</p>
        <div class="tip-footer">
          <span class="read-more">
            阅读全文
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </span>
        </div>
      </article>
    </div>

    <el-empty v-if="!loading && !list.length" description="暂无内容" />

    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="loadPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listTips } from '@/api/tip'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

function formatDate(t) {
  if (!t) return '-'
  const s = typeof t === 'string' ? t : (t.replace ? t.replace('T', ' ').substring(0, 10) : '')
  return s
}

function preview(content) {
  if (!content) return ''
  return content.substring(0, 80).replace(/\n/g, ' ')
}

async function loadPage(page) {
  currentPage.value = page
  loading.value = true
  try {
    const res = await listTips({ page, size: pageSize.value })
    const data = res.data || {}
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => loadPage(1))
</script>

<style scoped>
.tip-list-page {
  max-width: 1100px;
  position: relative;
}

/* ===== 背景装饰 ===== */
.page-bg-deco {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}
.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}
.c1 {
  width: 400px; height: 400px;
  top: -80px; right: -120px;
  background: radial-gradient(circle, var(--brand-primary), transparent);
}
.c2 {
  width: 250px; height: 250px;
  bottom: 60px; left: -60px;
  background: radial-gradient(circle, var(--brand-primary-light), transparent);
}
.c3 {
  width: 180px; height: 180px;
  top: 40%; right: 10%;
  background: radial-gradient(circle, #34c759, transparent);
}

/* ===== Hero ===== */
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, var(--brand-primary-light) 0%, rgba(255,255,255,0.7) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 28px 32px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px var(--surface-primary);
  position: relative;
  z-index: 1;
}
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon {
  width: 56px; height: 56px;
  display: flex; align-items: center; justify-content: center;
  background: #fff;
  color: var(--brand-primary);
  border-radius: 16px;
  box-shadow: 0 4px 16px var(--shadow-primary-20);
  flex-shrink: 0;
}
.hero-icon svg { width: 30px; height: 30px; }
.page-title {
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
.hero-stats { display: flex; gap: 16px; }
.stat-item {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 12px 20px;
  text-align: center;
  min-width: 80px;
}
.stat-num {
  font-size: 24px;
  font-weight: 800;
  color: var(--brand-primary);
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* ===== 分类筛选 ===== */
.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
  flex-wrap: wrap;
}
.filter-chip {
  padding: 8px 18px;
  border-radius: 20px;
  border: 1px solid var(--border-light);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.filter-chip:hover {
  background: #fff;
  border-color: var(--brand-primary-light);
  color: var(--brand-primary);
}
.filter-chip.active {
  background: var(--brand-primary);
  color: #fff;
  border-color: var(--brand-primary);
}

/* ===== 卡片网格 ===== */
.tip-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  position: relative;
  z-index: 1;
}
.tip-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 18px;
  padding: 20px 22px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  min-height: 200px;
  position: relative;
  overflow: hidden;
  animation: cardIn 0.4s ease both;
}
.tip-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--brand-primary), var(--brand-primary-light));
  opacity: 0;
  transition: opacity 0.3s;
}
.tip-card:hover {
  background: #fff;
  transform: translateY(-4px);
  box-shadow: 0 12px 32px var(--shadow-primary-20);
  border-color: var(--brand-primary-light);
}
.tip-card:hover::before { opacity: 1; }
@keyframes cardIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.tip-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.tip-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  color: var(--brand-primary);
  background: var(--brand-primary-light);
  padding: 3px 10px;
  border-radius: 10px;
  letter-spacing: 0.05em;
}
.tip-date {
  font-size: 12px;
  color: var(--text-tertiary);
}

.tip-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tip-preview {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
  flex: 1;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tip-footer {
  border-top: 1px dashed var(--border-light);
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.read-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-tertiary);
  font-weight: 500;
  transition: all 0.2s;
}
.read-more svg { width: 14px; height: 14px; transition: transform 0.2s; }
.tip-card:hover .read-more { color: var(--brand-primary); }
.tip-card:hover .read-more svg { transform: translateX(3px); }

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
  position: relative;
  z-index: 1;
}

@media (max-width: 600px) {
  .page-hero { padding: 20px; flex-direction: column; align-items: flex-start; gap: 12px; }
  .hero-stats { width: 100%; }
  .stat-item { flex: 1; }
  .tip-grid { grid-template-columns: 1fr; }
}
</style>
