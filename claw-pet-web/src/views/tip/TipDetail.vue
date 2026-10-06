<template>
  <div class="tip-detail-page page-wrapper" v-loading="loading">
    <article class="detail-card" v-if="tip">
      <h1 class="detail-title">{{ tip.title }}</h1>
      <div class="detail-meta">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
        <span>{{ formatDate(tip.publishDate) }}</span>
      </div>
      <div class="detail-content">
        <p v-for="(line, idx) in lines" :key="idx">{{ line }}</p>
      </div>
      <div class="detail-footer">
        <button class="back-btn" @click="$router.push('/tip')">返回列表</button>
      </div>
    </article>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getTip } from '@/api/tip'

const route = useRoute()
const tip = ref(null)
const loading = ref(false)

const lines = computed(() => {
  if (!tip.value?.content) return []
  return tip.value.content.split('\n').filter(Boolean)
})

function formatDate(t) {
  if (!t) return '-'
  return typeof t === 'string' ? t.substring(0, 10) : '-'
}

async function load(id) {
  loading.value = true
  try {
    const res = await getTip(id)
    tip.value = res.data
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, (id) => id && load(id), { immediate: true })
onMounted(() => route.params.id && load(route.params.id))
</script>

<style scoped>
.tip-detail-page { max-width: 800px; }
.detail-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  padding: 40px 48px;
  box-shadow: 0 8px 32px var(--surface-primary);
}
.detail-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 12px;
  letter-spacing: -0.02em;
  line-height: 1.3;
}
.detail-meta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-tertiary);
  margin-bottom: 24px;
  padding: 4px 10px;
  background: var(--brand-primary-light);
  border-radius: 8px;
}
.detail-content {
  line-height: 1.9;
  font-size: 15px;
  color: var(--text-primary);
}
.detail-content p {
  margin: 8px 0;
  white-space: pre-wrap;
  word-break: break-word;
}
.detail-footer {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}
.back-btn {
  background: transparent;
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  padding: 8px 20px;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.back-btn:hover {
  color: var(--brand-primary);
  border-color: var(--brand-primary);
}
</style>
