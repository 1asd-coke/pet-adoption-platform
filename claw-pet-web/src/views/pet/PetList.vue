<template>
  <div class="pet-list-page page-wrapper">
    <!-- 顶部 Hero -->
    <header class="page-hero">
      <div class="hero-text">
        <h1 class="page-title">宠物列表</h1>
        <p class="page-subtitle">找到那个属于你的小伙伴</p>
      </div>
      <div class="hero-stats">
        <div class="stat-num">{{ total }}</div>
        <div class="stat-label">只小家伙</div>
      </div>
    </header>

    <!-- 筛选 + 搜索 -->
    <div class="filter-card">
      <div class="filter-rows">
        <div class="filter-row">
          <span class="filter-tag">分类</span>
          <div class="chips">
            <button class="chip" :class="{ active: query.categoryId === null }" @click="setCategory(null)">全部</button>
            <button v-for="cat in categories" :key="cat.id" class="chip" :class="{ active: query.categoryId === cat.id }" @click="setCategory(cat.id)">{{ cat.name }}</button>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-tag">状态</span>
          <div class="chips">
            <button class="chip" :class="{ active: query.status === '' }" @click="setFilter('status', '')">全部状态</button>
            <button class="chip" :class="{ active: query.status === 'available' }" @click="setFilter('status', 'available')">待领养</button>
            <button class="chip" :class="{ active: query.status === 'adopting' }" @click="setFilter('status', 'adopting')">领养中</button>
            <button class="chip" :class="{ active: query.status === 'adopted' }" @click="setFilter('status', 'adopted')">已领养</button>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-tag">性别</span>
          <div class="chips">
            <button class="chip" :class="{ active: query.gender === '' }" @click="setFilter('gender', '')">全部</button>
            <button class="chip" :class="{ active: query.gender === 'male' }" @click="setFilter('gender', 'male')">公</button>
            <button class="chip" :class="{ active: query.gender === 'female' }" @click="setFilter('gender', 'female')">母</button>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-tag">健康</span>
          <div class="chips">
            <button class="chip" :class="{ active: query.healthStatus === '' }" @click="setFilter('healthStatus', '')">全部</button>
            <button class="chip" :class="{ active: query.healthStatus === 'healthy' }" @click="setFilter('healthStatus', 'healthy')">健康</button>
            <button class="chip" :class="{ active: query.healthStatus === 'recovering' }" @click="setFilter('healthStatus', 'recovering')">恢复中</button>
          </div>
        </div>
        <div class="search-row">
          <div class="search-input-wrap">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input v-model="query.keyword" placeholder="搜索宠物名称或品种" class="search-input" @keyup.enter="search" />
            <button v-if="query.keyword" class="search-clear" @click="query.keyword = ''; search()">✕</button>
          </div>
          <button class="search-btn" @click="search">搜索</button>
        </div>
      </div>
    </div>

    <!-- 网格 -->
    <div v-if="petList.length" class="pet-grid">
      <div
        v-for="(pet, i) in petList"
        :key="pet.id"
        class="pet-card"
        :style="{ animationDelay: i * 0.04 + 's' }"
        @click="$router.push(`/pet/detail/${pet.id}`)"
      >
        <div class="pet-image-wrap">
          <el-image class="pet-image" :src="pet.imageUrls?.[0]" fit="cover" lazy>
            <template #error>
              <div class="pet-placeholder">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="#aeaeb2"><ellipse cx="12" cy="16" rx="5" ry="4"/><circle cx="5" cy="10" r="2"/><circle cx="9" cy="6" r="2"/><circle cx="15" cy="6" r="2"/><circle cx="19" cy="10" r="2"/></svg>
              </div>
            </template>
          </el-image>
          <div class="pet-overlay">
            <span class="overlay-text">查看详情</span>
          </div>
          <span class="pet-status" :class="`status-${pet.status}`">
            <span class="status-dot"></span>
            {{ statusText(pet.status) }}
          </span>
        </div>
        <div class="pet-body">
          <h4 class="pet-name">{{ pet.name }}</h4>
          <p class="pet-breed">{{ pet.breed || '未知品种' }}</p>
          <div class="pet-tags">
            <span v-if="pet.age" class="pet-tag">{{ pet.age }}岁</span>
            <span v-if="pet.weight" class="pet-tag">{{ pet.weight }}kg</span>
            <span v-if="pet.gender" class="pet-tag gender" :class="`gender-${pet.gender}`">
              {{ pet.gender === 'male' ? '♂' : '♀' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无符合条件的小伙伴" :image-size="100" class="empty-tip" />

    <div class="pagination-wrap" v-if="total > query.size">
      <el-pagination
        v-model:current-page="query.page"
        :page-size="query.size"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadPets"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listPets, listCategories } from '@/api/pet'

const route = useRoute()
const petList = ref([])
const categories = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null,
  status: '',
  keyword: '',
  gender: '',
  healthStatus: ''
})

function statusText(s) {
  return { available: '待领养', adopting: '领养中', adopted: '已领养' }[s] || s
}

function setCategory(id) {
  query.categoryId = id
  search()
}
function setFilter(key, value) {
  query[key] = value
  search()
}

watch(() => route.query, (q) => {
  if (q.categoryId !== undefined) {
    query.categoryId = q.categoryId ? Number(q.categoryId) : null
    query.page = 1
    loadPets()
  }
})

async function loadPets() {
  try {
    const res = await listPets(query)
    petList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {}
}

function search() {
  query.page = 1
  loadPets()
}

onMounted(async () => {
  try {
    const catRes = await listCategories()
    categories.value = (catRes.data || []).filter(c => c.status === 'active')
  } catch {}
  loadPets()
})
</script>

<style scoped>
.pet-list-page {
  padding-top: 8px;
  padding-bottom: 40px;
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
  margin-bottom: 16px;
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
  text-align: center;
  background: var(--brand-primary);
  padding: 12px 22px;
  border-radius: 14px;
  color: #fff;
  min-width: 80px;
  box-shadow: 0 4px 16px var(--shadow-primary-25);
}
.stat-num {
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}
.stat-label { font-size: 11px; opacity: 0.9; margin-top: 2px; letter-spacing: 0.1em; }

/* ===== Filter Card ===== */
.filter-card {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 20px 24px;
  box-shadow: 0 8px 32px var(--surface-primary);
  margin-bottom: 24px;
  animation: slideDown 0.5s ease 0.1s both;
}
.filter-rows { display: flex; flex-direction: column; gap: 12px; }
.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.filter-tag {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 600;
  letter-spacing: 0.05em;
  min-width: 36px;
}
.chips { display: flex; gap: 8px; flex-wrap: wrap; flex: 1; }
.chip {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.7);
  border: 0.5px solid var(--border-light);
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}
.chip:hover {
  background: #f3e9f7;
  color: var(--text-primary);
  border-color: var(--brand-primary);
}
.chip.active {
  background: var(--brand-primary);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 2px 8px var(--shadow-primary-30);
}

/* ===== Search ===== */
.search-row {
  display: flex;
  gap: 10px;
  margin-top: 4px;
  padding-top: 14px;
  border-top: 1px dashed var(--border-light);
}
.search-input-wrap {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: #fff;
  border: 0.5px solid var(--border-light);
  border-radius: 12px;
  padding: 0 14px;
  transition: all 0.2s;
}
.search-input-wrap:focus-within {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px rgba(212, 120, 158, 0.1);
}
.search-icon { width: 16px; height: 16px; color: #aeaeb2; flex-shrink: 0; }
.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--text-primary);
  padding: 10px 8px;
  font-family: inherit;
}
.search-input::placeholder { color: #aeaeb2; }
.search-clear {
  width: 18px; height: 18px;
  border: none;
  background: var(--border-light);
  border-radius: 50%;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 11px;
  line-height: 1;
  display: flex; align-items: center; justify-content: center;
}
.search-btn {
  background: var(--brand-primary);
  color: #fff;
  border: none;
  padding: 0 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  letter-spacing: 0.05em;
  box-shadow: 0 2px 8px var(--shadow-primary-25);
  transition: all 0.2s;
}
.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(212, 120, 158, 0.35);
}

/* ===== Grid ===== */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}
.pet-card {
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
.pet-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px var(--shadow-primary-hover);
  background: rgba(255, 255, 255, 0.85);
}
.pet-card:hover .pet-image { transform: scale(1.08); }
.pet-card:hover .pet-overlay { opacity: 1; }

@keyframes cardSlide {
  to { opacity: 1; transform: translateY(0); }
}

.pet-image-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f0eaf6;
}
.pet-image {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.pet-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: #f0eaf6;
}

.pet-status {
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

.pet-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(0,0,0,0.5) 100%);
  display: flex; align-items: flex-end; justify-content: center;
  padding-bottom: 16px;
  opacity: 0;
  transition: opacity 0.3s;
}
.overlay-text {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  border: 0.5px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
}

.pet-body { padding: 12px 14px; }
.pet-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 2px;
  letter-spacing: -0.01em;
}
.pet-breed {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pet-tags { display: flex; gap: 4px; flex-wrap: wrap; }
.pet-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 6px;
  background: var(--surface-primary);
  color: var(--brand-primary);
}
.pet-tag.gender { font-size: 13px; padding: 0 4px; background: transparent; }
.pet-tag.gender-male { color: #378add; }
.pet-tag.gender-female { color: #ff5e8a; }

.empty-tip { padding: 60px 0; }

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-bottom: 20px;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 600px) {
  .page-hero { flex-direction: column; gap: 12px; text-align: center; }
  .pet-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
}
</style>
