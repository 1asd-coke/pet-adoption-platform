<template>
  <div class="gallery">
    <div class="gallery-main">
      <transition name="fade" mode="out-in">
        <img :src="currentSrc" :key="currentSrc" class="gallery-image" :alt="petName" />
      </transition>
      <div v-if="images.length > 1" class="gallery-thumbs">
        <div
          v-for="(img, idx) in images"
          :key="idx"
          class="thumb"
          :class="{ active: currentIndex === idx }"
          @click="currentIndex = idx"
        >
          <img :src="img" :alt="`${petName} ${idx + 1}`" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  images: { type: Array, required: true },
  petName: { type: String, default: '宠物' }
})

const currentIndex = ref(0)
const currentSrc = computed(() => props.images[currentIndex.value] || props.images[0] || '')
</script>

<style scoped>
.gallery { animation: slideInLeft 0.5s ease both; position: sticky; top: 20px; }
.gallery-main {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 12px;
  box-shadow: 0 8px 32px var(--surface-primary);
}
.gallery-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 16px;
  display: block;
}
.gallery-thumbs {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  overflow-x: auto;
  padding: 4px;
}
.thumb {
  width: 60px; height: 60px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  flex-shrink: 0;
  opacity: 0.6;
}
.thumb:hover { opacity: 0.9; }
.thumb.active {
  border-color: var(--brand-primary);
  opacity: 1;
}
.thumb img { width: 100%; height: 100%; object-fit: cover; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@keyframes slideInLeft { from { opacity: 0; transform: translateX(-20px); } to { opacity: 1; transform: translateX(0); } }
</style>
