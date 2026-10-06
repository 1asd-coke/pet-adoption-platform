<template>
  <!-- 鼠标发光映射：模拟苹果液态玻璃的表面光效 -->
  <div
    class="mouse-glow"
    :style="glowStyle"
  />
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const mouseX = ref(-999)
const mouseY = ref(-999)
const isVisible = ref(false)

let rafId = null
let targetX = -999
let targetY = -999

function onMouseMove(e) {
  targetX = e.clientX
  targetY = e.clientY
  isVisible.value = true
}

function onMouseLeave() {
  isVisible.value = false
}

function tick() {
  mouseX.value += (targetX - mouseX.value) * 0.15
  mouseY.value += (targetY - mouseY.value) * 0.15
  if (Math.abs(mouseX.value - targetX) > 0.5 || Math.abs(mouseY.value - targetY) > 0.5) {
    rafId = requestAnimationFrame(tick)
  }
}

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseleave', onMouseLeave)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseleave', onMouseLeave)
  if (rafId) cancelAnimationFrame(rafId)
})

const glowStyle = computed(() => ({
  left: `${mouseX.value}px`,
  top: `${mouseY.value}px`,
  opacity: isVisible.value ? 1 : 0
}))
</script>

<style scoped>
.mouse-glow {
  position: fixed;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 0;
  transition: opacity 0.3s ease;
  will-change: left, top;
}

/* 表面镜面高光：暖白光斑，模拟光线在玻璃表面的反射 */
.mouse-glow::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: radial-gradient(
    circle at center,
    rgba(255, 255, 255, 0.08) 0%,
    rgba(255, 255, 255, 0.03) 20%,
    transparent 50%
  );
}

/* 内部漫射光晕：品牌色在玻璃内部散射的柔光 */
.mouse-glow::after {
  content: '';
  position: absolute;
  inset: -40px;
  border-radius: 50%;
  background: radial-gradient(
    circle at center,
    var(--brand-primary) 0%,
    transparent 55%
  );
  opacity: 0.03;
}
</style>
