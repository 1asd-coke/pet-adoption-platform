<template>
  <div class="info-side">
    <div class="info-card">
      <div class="info-head">
        <div class="info-status">
          <span class="status-dot" :class="`dot-${pet.status}`"></span>
          {{ statusText }}
        </div>
        <h1 class="info-name">{{ pet.name }}</h1>
        <p v-if="pet.breed" class="info-subtitle">{{ pet.breed }}</p>
      </div>

      <div class="info-facts">
        <div class="fact" v-if="pet.age != null">
          <div class="fact-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          </div>
          <div class="fact-text">
            <div class="fact-value">{{ pet.age }}</div>
            <div class="fact-label">岁</div>
          </div>
        </div>
        <div class="fact">
          <div class="fact-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M2 12h20"/></svg>
          </div>
          <div class="fact-text">
            <div class="fact-value" :class="pet.gender === 'male' ? 'gender-male' : 'gender-female'">
              {{ pet.gender === 'male' ? '公' : pet.gender === 'female' ? '母' : '?' }}
            </div>
            <div class="fact-label">性别</div>
          </div>
        </div>
        <div class="fact" v-if="pet.weight">
          <div class="fact-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L4 22h16L12 2z"/></svg>
          </div>
          <div class="fact-text">
            <div class="fact-value">{{ pet.weight }}</div>
            <div class="fact-label">kg</div>
          </div>
        </div>
        <div class="fact" :class="`fact-${pet.healthStatus === 'healthy' ? 'ok' : 'warn'}`">
          <div class="fact-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          </div>
          <div class="fact-text">
            <div class="fact-value">{{ pet.healthStatus === 'healthy' ? '健康' : pet.healthStatus === 'recovering' ? '恢复中' : '未知' }}</div>
            <div class="fact-label">身体状态</div>
          </div>
        </div>
        <div class="fact" :class="`fact-${pet.vaccineStatus === 'done' ? 'ok' : 'warn'}`">
          <div class="fact-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
          </div>
          <div class="fact-text">
            <div class="fact-value">{{ pet.vaccineStatus === 'done' ? '已接种' : '未接种' }}</div>
            <div class="fact-label">疫苗</div>
          </div>
        </div>
        <div class="fact fact-fav" :class="{ active: isFavorited }" @click="$emit('toggle-favorite')">
          <div class="fact-icon">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
          </div>
          <div class="fact-text">
            <div class="fact-value">{{ isFavorited ? '已收藏' : '收藏' }}</div>
            <div class="fact-label">这只小家伙</div>
          </div>
        </div>
      </div>

      <div class="info-actions">
        <slot name="actions" />
      </div>
    </div>

    <!-- About 描述 -->
    <section v-if="pet.description" class="about-section">
      <div class="section-head">
        <h2 class="section-title">关于 {{ pet.name }}</h2>
        <span class="section-line"></span>
      </div>
      <div class="about-content">
        <p class="about-text">{{ pet.description }}</p>
        <div class="about-deco">
          <svg viewBox="0 0 100 100" width="100" height="100" fill="none" opacity="0.3">
            <circle cx="50" cy="50" r="35" stroke="var(--brand-primary)" stroke-width="1" stroke-dasharray="2 4"/>
            <circle cx="50" cy="50" r="20" stroke="var(--brand-primary)" stroke-width="1"/>
            <circle cx="50" cy="50" r="5" fill="var(--brand-primary)"/>
          </svg>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  pet: { type: Object, required: true },
  isFavorited: { type: Boolean, default: false }
})

defineEmits(['toggle-favorite'])

const statusMap = { available: '待领养', adopting: '领养中', adopted: '已领养', offline: '已下架' }
const statusText = computed(() => {
  const s = props.pet?.status
  if (!s) return '待领养'
  return statusMap[s] || '待领养'
})
</script>

<style scoped>
/* ===== Info Side ===== */
.info-side { animation: slideInRight 0.5s ease 0.1s both; }
.info-card {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 8px 32px var(--surface-primary);
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}
.info-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--brand-primary);
  margin-bottom: 6px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}
.status-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #34c759;
  box-shadow: 0 0 0 3px rgba(52, 199, 89, 0.2);
  animation: pulse 2s ease-in-out infinite;
}
.dot-adopting { background: #ff9500; box-shadow: 0 0 0 3px rgba(255, 149, 0, 0.2); }
.dot-adopted { background: #aeaeb2; box-shadow: 0 0 0 3px rgba(174, 174, 178, 0.2); }
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}
.info-name {
  font-size: 34px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.03em;
  line-height: 1.1;
}
.info-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 4px 0 0;
}

/* ===== Facts Grid ===== */
.info-facts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.fact {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 14px 8px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 14px;
  border: 0.5px solid var(--border-light);
  transition: all 0.25s ease;
}
.fact-ok { background: rgba(52, 199, 89, 0.06); border-color: rgba(52, 199, 89, 0.2); }
.fact-warn { background: rgba(255, 149, 0, 0.06); border-color: rgba(255, 149, 0, 0.2); }
.fact-fav { cursor: pointer; }
.fact-fav:hover { background: rgba(255, 142, 201, 0.1); border-color: var(--brand-primary); }
.fact-fav.active { background: rgba(255, 94, 138, 0.1); border-color: var(--brand-primary); }
.fact-fav.active .fact-icon { color: #ff5e8a; }
.fact-fav .fact-icon { color: #b0a8c0; }

.fact-icon {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  color: var(--brand-primary);
  background: var(--brand-primary-light);
  border-radius: 50%;
}
.fact-icon svg { width: 20px; height: 20px; }
.fact-text { line-height: 1.2; text-align: center; }
.fact-value {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}
.fact-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 1px;
}
.gender-male { color: #378add; }
.gender-female { color: #ff5e8a; }

/* ===== Info Actions (Slot) ===== */
.info-actions { padding-top: 4px; }

/* ===== About Section ===== */
.about-section {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px var(--surface-primary);
  animation: slideInUp 0.5s ease 0.1s both;
  position: relative;
  overflow: hidden;
}
.section-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.01em;
  white-space: nowrap;
}
.section-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, var(--brand-primary-light), transparent);
}
.about-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  position: relative;
  z-index: 1;
}
.about-text {
  flex: 1;
  font-size: 15px;
  color: #4a4a5a;
  line-height: 1.9;
  margin: 0;
  white-space: pre-wrap;
}
.about-deco { flex-shrink: 0; opacity: 0.4; }
@media (max-width: 600px) {
  .about-deco { display: none; }
}

@keyframes slideInRight { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
@keyframes slideInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>
