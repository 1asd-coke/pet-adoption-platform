<template>
  <div class="home-page page-wrapper">
    <!-- Hero -->
    <div class="hero-section">
      <HeroBanner :image="heroImage" />
    </div>

    <!-- Stats -->
    <div class="stats-row" ref="statsRef">
      <div v-for="s in stats" :key="s.label" class="stat-block">
        <div class="stat-number" :style="{ color: s.color }" :data-target="s.target">{{ s.display }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- Pets -->
    <div class="pets-section" ref="petsSectionRef">
      <div class="section-head">
        <h2 class="section-title">
          待领养宠物
          <span class="section-count" v-if="availableTotal">共 {{ availableTotal }} 只小伙伴等待一个家</span>
        </h2>
        <el-button text type="primary" @click="$router.push('/pet')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="pet-grid">
        <div v-for="(pet, i) in petList" :key="pet.id" class="pet-card" :style="{ animationDelay: i * 0.04 + 's' }" @click="$router.push(`/pet/detail/${pet.id}`)">
          <div class="pet-image-wrap">
            <el-image class="pet-image" :src="pet.imageUrls?.[0]" fit="cover" lazy>
              <template #error>
                <div class="pet-placeholder">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="#aeaeb2"><ellipse cx="12" cy="16" rx="5" ry="4"/><circle cx="5" cy="10" r="2"/><circle cx="9" cy="6" r="2"/><circle cx="15" cy="6" r="2"/><circle cx="19" cy="10" r="2"/></svg>
                </div>
              </template>
            </el-image>
            <span class="pet-glass-tag">
              <span class="tag-dot"></span>待领养
            </span>
            <div class="pet-overlay">
              <span class="overlay-btn">立即申请领养</span>
            </div>
          </div>
          <div class="pet-body">
            <h4 class="pet-name">{{ pet.name }}</h4>
            <p class="pet-breed">{{ pet.breed || '未知品种' }}</p>
            <div class="pet-meta">
              <span v-if="pet.age" class="meta-chip">
                <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                {{ pet.age }}
              </span>
              <span v-if="pet.weight" class="meta-chip">
                <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L4 22h16L12 2z"/></svg>
                {{ pet.weight }}
              </span>
              <span v-if="pet.gender" class="meta-chip" :class="pet.gender === 'male' ? 'chip-male' : 'chip-female'">
                {{ pet.gender === 'male' ? '♂' : '♀' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { listPets } from '@/api/pet'
import { getHomeStats } from '@/api/stats'
import { getShelter } from '@/api/shelter'
import HeroBanner from '@/components/HeroBanner.vue'

const router = useRouter()
const petList = ref([])
const availableTotal = ref(0)
const heroImage = ref('')
const statsRef = ref(null)
const petsSectionRef = ref(null)
let observers = []

const stats = ref([
  { label: '待领养', target: 0, display: 0, color: 'var(--brand-primary)' },
  { label: '已领养', target: 0, display: 0, color: '#34c759' },
  { label: '宠物总数', target: 0, display: 0, color: 'var(--brand-primary-light)' },
  { label: '用户总数', target: 0, display: 0, color: '#7C7C8A' }
])

function animateNumbers() {
  const duration = 1000
  const startTime = performance.now()
  const targets = stats.value.map(s => s.target)
  const starts = targets.map(() => 0)

  function step(now) {
    const progress = Math.min((now - startTime) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    stats.value.forEach((s, i) => {
      s.display = Math.round(starts[i] + (targets[i] - starts[i]) * eased)
    })
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

function observe(el, className) {
  if (!el) return
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add(className)
        if (className === 'stats-visible') animateNumbers()
        observer.unobserve(entry.target)
      }
    })
  }, { threshold: 0.3 })
  observer.observe(el)
  observers.push(observer)
}

onMounted(async () => {
  try {
    const [petRes, shelterRes] = await Promise.all([
      listPets({ page: 1, size: 10, status: 'available' }),
      getShelter()
    ])
    petList.value = petRes.data?.records || []
    heroImage.value = shelterRes.data?.image || ''

    // 统计数字用真实值
    try {
      const r = await getHomeStats()
      if (r.data) {
        availableTotal.value = r.data.availableCount ?? 0
        stats.value = [
          { label: '待领养', target: r.data.availableCount ?? 0, display: 0, color: 'var(--brand-primary)' },
          { label: '已领养', target: r.data.adoptedCount ?? 0, display: 0, color: '#34c759' },
          { label: '宠物总数', target: r.data.petCount ?? 0, display: 0, color: 'var(--brand-primary-light)' },
          { label: '用户总数', target: r.data.userCount ?? 0, display: 0, color: '#7C7C8A' }
        ]
      }
    } catch {}
  } catch {}
  // 滚动观察器延迟到 dom 渲染后
  setTimeout(() => {
    observe(statsRef.value, 'stats-visible')
    observe(petsSectionRef.value, 'pets-visible')
  }, 100)
})

onUnmounted(() => {
  observers.forEach(o => o.disconnect())
})
</script>

<style scoped>
.home-page { padding-top: 0; padding-bottom: 60px; }

.hero-section {
  margin: 8px 0 32px;
}

/* ===== Stats ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 44px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s ease;
}
.stats-visible { opacity: 1; transform: translateY(0); }

.stat-block {
  background: #fff;
  border-radius: 16px;
  padding: 18px 20px 14px;
  border: 0.5px solid var(--border-light);
  text-align: center;
  box-shadow: 0 2px 8px var(--surface-primary);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.stat-block:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px var(--shadow-primary);
}

.stat-number {
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.03em;
  margin-bottom: 4px;
  transition: transform 0.3s ease;
}
.stat-block:hover .stat-number { transform: scale(1.05); }

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* ===== Pets ===== */
.pets-section { margin-bottom: 0; }

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.01em;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.section-count {
  font-size: 13px;
  font-weight: 400;
  color: var(--text-secondary);
  background: var(--surface-primary);
  padding: 3px 10px;
  border-radius: 8px;
  white-space: nowrap;
}

/* ===== Pet Card ===== */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.pet-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0;
  transform: translateY(24px);
  animation: cardIn 0.5s ease forwards;
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px var(--surface-primary);
}
.pets-visible .pet-card { opacity: 1; transform: translateY(0); }

@keyframes cardIn {
  to { opacity: 1; transform: translateY(0); }
}

.pet-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px var(--shadow-primary-hover);
  background: rgba(255, 255, 255, 0.85);
}
.pet-card:hover .pet-image { transform: scale(1.08); }
.pet-card:hover .pet-overlay { opacity: 1; }

.pet-image-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: linear-gradient(135deg, var(--brand-primary-light) 0%, #f3e9f7 100%);
}
.pet-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.pet-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
}

/* ===== Glass Tag ===== */
.pet-glass-tag {
  position: absolute;
  top: 10px; left: 10px;
  display: inline-flex; align-items: center; gap: 5px;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: var(--brand-primary);
  box-shadow: 0 2px 8px var(--shadow-primary);
}
.tag-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--brand-primary);
  box-shadow: 0 0 0 2px var(--shadow-primary-30);
  animation: dotPulse 2s ease-in-out infinite;
}
@keyframes dotPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.4); opacity: 0.7; }
}

/* ===== Overlay ===== */
.pet-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 40%, rgba(0,0,0,0.45) 100%);
  display: flex; align-items: flex-end; justify-content: center;
  padding-bottom: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}
.overlay-btn {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 18px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 0.5px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  letter-spacing: 0.05em;
}

/* ===== Body ===== */
.pet-body { padding: 10px 14px 14px; }
.pet-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 2px;
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pet-breed {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pet-meta { display: flex; gap: 4px; flex-wrap: wrap; }
.meta-chip {
  display: inline-flex; align-items: center; gap: 3px;
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 6px;
  background: var(--surface-primary);
  color: var(--brand-primary);
}
.meta-icon { width: 12px; height: 12px; flex-shrink: 0; }
.chip-male { color: #378add; background: rgba(55, 138, 221, 0.08); }
.chip-female { color: #ff5e8a; background: rgba(255, 94, 138, 0.08); }

.available { background: var(--brand-primary-light); color: var(--brand-primary); }

@media (max-width: 768px) {
  .pet-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .section-head { flex-direction: column; align-items: flex-start; gap: 8px; }
}
</style>
