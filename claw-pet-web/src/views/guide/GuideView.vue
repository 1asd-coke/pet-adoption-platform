<template>
  <div class="guide-page page-wrapper">
    <header class="page-hero">
      <div class="hero-text">
        <h1 class="page-title">领养须知</h1>
        <p class="page-subtitle">领养前请仔细阅读，理性领养、负责到底</p>
      </div>
    </header>

    <div v-loading="loading">
      <article v-for="item in guides" :key="item.id" class="guide-card">
        <h2 class="guide-title">{{ item.title }}</h2>
        <div class="guide-content">
          <div v-for="(line, idx) in splitLines(item.content)" :key="idx" class="guide-line" v-html="line"></div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGuide } from '@/api/guide'

const guides = ref([])
const loading = ref(false)

function splitLines(content) {
  if (!content) return []
  return content.split('\n').filter(Boolean)
}

async function load() {
  loading.value = true
  try {
    const res = await getGuide()
    const data = res.data
    guides.value = Array.isArray(data) ? data : (data ? [data] : [])
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.guide-page { max-width: 900px; }
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 28px 32px;
  margin-bottom: 16px;
  box-shadow: 0 8px 32px var(--surface-primary);
}
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
.guide-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  padding: 28px 32px;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px var(--surface-primary);
}
.guide-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--brand-primary);
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--brand-primary-light);
}
.guide-content {
  line-height: 1.9;
  font-size: 15px;
  color: var(--text-primary);
}
.guide-line {
  margin: 6px 0;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
