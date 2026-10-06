<template>
  <div class="hero-banner" :class="{ 'has-image': !!image, 'is-loaded': loaded }">
    <!-- 装饰元素 -->
    <div class="deco deco-1">
      <svg viewBox="0 0 24 24" fill="var(--brand-primary)" opacity="0.3"><ellipse cx="12" cy="16" rx="5" ry="4"/><circle cx="5" cy="10" r="2"/><circle cx="9" cy="6" r="2"/><circle cx="15" cy="6" r="2"/><circle cx="19" cy="10" r="2"/></svg>
    </div>
    <div class="deco deco-2">
      <svg viewBox="0 0 24 24" fill="#f9c74f" opacity="0.5"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
    </div>

    <div class="hero-content">
      <p class="hero-label">Adopt · 领养</p>
      <h1 class="hero-title">
        <span class="line line-1">遇见你</span>
        <span class="line line-2">是它最幸运的事</span>
      </h1>
      <p class="hero-desc">每一个等待的灵魂，都值得一个温柔的家</p>
      <el-button class="hero-cta" @click="$router.push('/pet')">
        开启领养之旅
        <el-icon class="btn-icon"><ArrowRight /></el-icon>
      </el-button>
    </div>

    <div v-if="image" class="hero-image-wrap">
      <img :src="image" class="hero-image" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const props = defineProps({ image: { type: String, default: '' } })
const loaded = ref(false)

onMounted(() => {
  setTimeout(() => { loaded.value = true }, 50)
})
</script>

<style scoped>
.hero-banner {
  position: relative;
  display: grid;
  grid-template-columns: auto 1fr;
  border-radius: 24px;
  overflow: hidden;
  min-height: 380px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px var(--surface-primary);
  gap: 0;
}

/* ===== 装饰元素 ===== */
.deco {
  position: absolute;
  z-index: 1;
  pointer-events: none;
}
.deco-1 { top: 24px; left: 24px; width: 22px; height: 22px; animation: float 4s ease-in-out infinite; }
.deco-2 { top: 24px; right: 24px; width: 16px; height: 16px; animation: float 5s ease-in-out infinite -1.5s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0); }
  50% { transform: translateY(-10px) rotate(15deg); }
}

/* ===== 左：内容 ===== */
.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 48px 48px 56px;
  max-width: 480px;
}

.hero-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--brand-primary);
  letter-spacing: 0.18em;
  text-transform: uppercase;
  margin: 0 0 12px;
  opacity: 0;
  transform: translateX(-20px);
  transition: all 0.6s ease 0.2s;
}
.is-loaded .hero-label { opacity: 1; transform: translateX(0); }

.hero-title {
  font-size: 34px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px;
  line-height: 1.2;
  letter-spacing: -0.025em;
}
.hero-title .line {
  display: block;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s ease;
}
.is-loaded .line-1 { opacity: 1; transform: translateY(0); transition-delay: 0.35s; }
.is-loaded .line-2 { opacity: 1; transform: translateY(0); transition-delay: 0.5s; }

.hero-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 22px;
  line-height: 1.5;
  max-width: 340px;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.6s ease 0.7s;
}
.is-loaded .hero-desc { opacity: 1; transform: translateY(0); }

.hero-cta {
  align-self: flex-start;
  background: var(--brand-primary) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 500;
  padding: 11px 24px;
  border-radius: 24px;
  font-size: 14px;
  letter-spacing: 0.02em;
  box-shadow: 0 4px 14px var(--shadow-primary-30);
  opacity: 0;
  transform: translateY(10px) scale(0.95);
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) 0.9s;
}
.is-loaded .hero-cta { opacity: 1; transform: translateY(0) scale(1); }
.hero-cta:hover {
  background: #c86a8f !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 24px var(--shadow-primary-40) !important;
}

.btn-icon { margin-left: 6px; transition: transform 0.25s ease; }
.hero-cta:hover .btn-icon { transform: translateX(4px); }

/* ===== 右：图片 ===== */
.hero-image-wrap {
  position: relative;
  margin-left: -24px;
  display: flex;
  align-items: flex-start;
  opacity: 0;
  transform: translateX(20px);
  transition: all 0.8s ease 0.4s;
}
.is-loaded .hero-image-wrap { opacity: 1; transform: translateX(0); }

.hero-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 18px;
  display: block;
  border: none;
  outline: none;
  box-shadow: 0 10px 32px rgba(0, 0, 0, 0.12);
  transition: transform 0.5s ease;
  margin: 14px 14px 14px 0;
}
.hero-banner:hover .hero-image { transform: scale(1.02); }

@media (max-width: 768px) {
  .hero-banner { grid-template-columns: 1fr; }
  .hero-image-wrap { display: none; }
  .hero-content { padding: 40px 24px; }
  .hero-title { font-size: 28px; }
}
</style>
