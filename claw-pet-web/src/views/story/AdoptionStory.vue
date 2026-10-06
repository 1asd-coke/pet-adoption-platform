<template>
  <div class="story-page page-wrapper">
    <header class="page-hero">
      <div class="hero-text">
        <h1 class="page-title">领养人和宠物</h1>
        <p class="page-subtitle">来自领养家庭的温馨瞬间</p>
      </div>
      <el-button type="primary" v-if="userStore.isLoggedIn" @click="goToEditor()">
        <el-icon><EditPen /></el-icon>我也要发布
      </el-button>
    </header>

    <!-- 故事卡片网格 -->
    <div v-loading="loading" class="story-grid">
      <div
        v-for="story in stories"
        :key="story.id"
        class="story-card"
        @click="openDetail(story)"
      >
        <div class="card-img-wrap">
          <el-image
            v-if="story.images && story.images[0]"
            :src="story.images[0]"
            fit="cover"
            class="card-img"
          />
          <div v-else class="card-img-placeholder">
            <el-icon :size="28"><Picture /></el-icon>
          </div>
          <div class="card-cover-tag">
            {{ story.adopterName || '领养人' }}
            <span class="cover-connector">·</span>
            {{ story.petName || '宠物' }}
          </div>
          <span v-if="isMine(story)" class="card-mine">我的故事</span>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !stories.length" description="还没有领养故事，去领养一只宠物吧" />

    <!-- 故事详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="detailTitle" width="580px" align-center>
      <div class="detail-body" v-if="detailStory">
        <div class="detail-images" v-if="detailStory.images && detailStory.images.length">
          <el-image
            v-for="(img, idx) in detailStory.images"
            :key="idx"
            :src="img"
            fit="cover"
            class="detail-img"
            :preview-src-list="detailStory.images"
            :initial-index="idx"
          />
        </div>
        <div class="detail-text">{{ detailStory.story || detailStory.content || '' }}</div>
        <div class="detail-footer">
          <span class="detail-date">{{ detailStory.followupTime }}</span>
          <button
            v-if="isMine(detailStory)"
            class="detail-edit"
            @click="switchToEdit(detailStory)"
          >编辑故事</button>
        </div>
      </div>
    </el-dialog>

    <!-- 发布/编辑故事弹窗（已废弃，改用整页编辑器） -->
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { listShowcaseStories } from '@/api/story'
import { listMyRecords } from '@/api/adopt'

const userStore = useUserStore()
const router = useRouter()
const stories = ref([])
const loading = ref(false)
const myRecords = ref([])
const publishedIds = ref(new Set())

// 详情弹窗
const detailVisible = ref(false)
const detailStory = ref(null)
const detailTitle = computed(() => {
  if (!detailStory.value) return ''
  return `${detailStory.value.adopterName || '领养人'} 与 ${detailStory.value.petName || '宠物'}`
})

function formatDate(t) {
  if (!t) return '-'
  if (typeof t === 'string') return t.substring(0, 10)
  return '-'
}

function isMine(story) {
  if (!story || !userStore.userInfo) return false
  return Number(story.userId) === Number(userStore.userInfo.id)
}

const notPublishedRecords = computed(() => myRecords.value)

async function loadStories() {
  loading.value = true
  try {
    const res = await listShowcaseStories()
    stories.value = res.data || []
    publishedIds.value = new Set(stories.value.map(s => s.recordId).filter(Boolean))
  } finally { loading.value = false }
}

async function loadMyRecords() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await listMyRecords()
    myRecords.value = res.data || []
  } catch {}
}

function openDetail(story) {
  detailStory.value = story
  detailVisible.value = true
}

function switchToEdit(story) {
  detailVisible.value = false
  router.push({ path: '/story/editor', query: { id: story.id } })
}

function goToEditor(recordId) {
  const query = recordId ? { recordId } : {}
  router.push({ path: '/story/editor', query })
}

async function handlePublishOrUpdate() {
  // 保留为空函数 - 兼容旧调用（实际逻辑在整页编辑器里）
}

onMounted(() => { loadStories(); loadMyRecords() })
</script>

<style scoped>
.story-page { max-width: 1100px; margin: 0 auto; }

.page-hero {
  display: flex; justify-content: space-between; align-items: center;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px; padding: 24px 32px; margin-bottom: 16px;
  box-shadow: 0 8px 32px var(--surface-primary);
}
.page-title { font-size: 26px; font-weight: 800; color: var(--text-primary); margin: 0 0 4px; letter-spacing: -0.02em; }
.page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

/* 卡片网格 */
.story-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.story-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.25s;
  aspect-ratio: 1 / 1;
}
.story-card:hover {
  background: #fff;
  transform: translateY(-3px);
  box-shadow: 0 10px 30px var(--shadow-primary-20);
}
.card-img-wrap {
  width: 100%; height: 100%;
  position: relative;
  overflow: hidden;
}
.card-img {
  width: 100%; height: 100%;
  transition: transform 0.3s;
}
.story-card:hover .card-img { transform: scale(1.05); }
.card-img-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: var(--brand-primary-light);
  color: var(--text-tertiary);
}
.card-cover-tag {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  padding: 30px 12px 10px;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}
.cover-connector { opacity: 0.5; }
.card-mine {
  position: absolute;
  top: 8px; right: 8px;
  background: var(--brand-primary);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
}

/* 详情弹窗 */
.detail-body {
  padding: 4px 0;
  max-height: 60vh;
  overflow-y: auto;
}
.detail-images {
  display: flex; gap: 4px; overflow-x: auto;
  margin-bottom: 16px;
}
.detail-img {
  width: 180px; height: 120px;
  border-radius: 8px; flex-shrink: 0;
  cursor: pointer; background: var(--bg-tertiary);
}
.detail-text {
  font-size: 15px; line-height: 1.8;
  color: var(--text-primary);
  white-space: pre-wrap; word-break: break-word;
}
.detail-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 16px; padding-top: 12px;
  border-top: 1px solid var(--border-light);
}
.detail-date { font-size: 12px; color: var(--text-tertiary); }
.detail-edit {
  background: transparent; border: 1px solid var(--border-light);
  color: var(--text-secondary); font-size: 12px;
  padding: 4px 12px; border-radius: 8px;
  cursor: pointer; font-family: inherit;
  transition: all 0.15s;
}
.detail-edit:hover { color: var(--brand-primary); border-color: var(--brand-primary); }
</style>
