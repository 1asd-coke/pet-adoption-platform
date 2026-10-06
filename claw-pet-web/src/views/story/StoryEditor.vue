<template>
  <div class="editor-page page-wrapper">
    <header class="page-header">
      <div class="header-inner">
        <button class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>返回
        </button>
        <span class="header-title">{{ editingId ? '编辑故事' : '发布新故事' }}</span>
        <div class="header-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSubmit">
            {{ editingId ? '保存修改' : '发布' }}
          </el-button>
        </div>
      </div>
    </header>

    <div class="editor-card">
      <!-- 宠物卡片（领养记录信息） -->
      <div v-if="petInfo" class="pet-section">
        <el-image :src="petInfo.cover" fit="cover" class="pet-cover" />
        <div class="pet-info">
          <div class="pet-line">
            <h2 class="pet-name">{{ petInfo.name }}</h2>
            <el-tag v-if="editingId" size="small" type="warning">编辑模式</el-tag>
          </div>
          <div class="pet-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              领养于 {{ formatDate(petInfo.adoptTime) }}
            </span>
            <span class="meta-item" v-if="petInfo.breed">
              <el-icon><Star /></el-icon>
              {{ petInfo.breed }}
            </span>
          </div>
        </div>
        <el-select
          v-if="myRecords.length > 1 && !editingId"
          v-model="form.recordId"
          size="small"
          class="pet-selector"
        >
          <el-option
            v-for="r in myRecords"
            :key="r.recordId"
            :label="`${r.petName} (领养于 ${formatDate(r.adoptedAt)})`"
            :value="r.recordId"
          />
        </el-select>
      </div>

      <div v-else-if="loading" v-loading="loading" class="pet-section-skel"></div>

      <div v-else class="pet-empty">
        <el-empty
          v-if="myRecords.length === 0"
          description="当前账号还没有领养记录，无法发布故事"
        />
        <el-button v-else type="primary" @click="loadMyRecords">重新加载</el-button>
      </div>

      <!-- 写作区 -->
      <div class="write-area">
        <textarea
          v-model="form.story"
          class="story-textarea"
          placeholder="分享你和宠物的故事..."
          rows="20"
        ></textarea>
        <div class="editor-toolbar">
          <span class="char-count">{{ form.story.length }} 字</span>
          <span class="hint">支持换行 · 至少写 10 个字</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { listMyRecords } from '@/api/adopt'
import { publishStory, updateStory, listMyStories } from '@/api/story'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const saving = ref(false)
const loading = ref(false)
const myRecords = ref([])
const allStories = ref([])

const form = ref({ recordId: null, story: '' })
const editingId = ref(null)
const petInfo = ref(null)

const isEditMode = computed(() => !!route.query.id)
const storyId = computed(() => route.query.id)

onMounted(async () => {
  if (isEditMode.value) {
    await loadForEdit()
  } else {
    await loadMyRecords()
  }
})

// 监听 recordId 变化，同步更新宠物信息
watch(() => form.value.recordId, (newId) => {
  if (!newId || editingId.value) return
  const r = myRecords.value.find(x => x.recordId === newId)
  if (r) {
    petInfo.value = { name: r.petName, cover: r.petCover, breed: r.breed, adoptTime: r.adoptedAt }
  }
})

async function loadMyRecords() {
  loading.value = true
  try {
    const res = await listMyRecords()
    myRecords.value = res.data || []
    if (myRecords.value.length === 0) {
      console.warn('[StoryEditor] 当前用户没有领养记录，userId=', userStore.userInfo?.id)
    }
    const recordId = route.query.recordId ? Number(route.query.recordId) : null
    if (recordId) {
      const r = myRecords.value.find(x => x.recordId === recordId)
      if (r) {
        form.value.recordId = recordId
        petInfo.value = { name: r.petName, cover: r.petCover, breed: r.breed, adoptTime: r.adoptedAt }
      }
    } else if (myRecords.value.length === 1) {
      const r = myRecords.value[0]
      form.value.recordId = r.recordId
      petInfo.value = { name: r.petName, cover: r.petCover, breed: r.breed, adoptTime: r.adoptedAt }
    } else if (myRecords.value.length > 1) {
      // 多只宠物，第一只自动选中
      const r = myRecords.value[0]
      form.value.recordId = r.recordId
      petInfo.value = { name: r.petName, cover: r.petCover, breed: r.breed, adoptTime: r.adoptedAt }
    }
  } catch (e) {
    console.error('[StoryEditor] 加载领养记录失败：', e)
  } finally { loading.value = false }
}

async function loadForEdit() {
  loading.value = true
  try {
    // 加载所有故事找当前要编辑的
    const res = await listMyStories().catch(() => ({ data: [] }))
    allStories.value = res.data || []
    const target = allStories.value.find(s => s.id === Number(storyId.value))
    if (target) {
      editingId.value = target.recordId
      form.value.recordId = target.recordId
      form.value.story = target.story || ''
      petInfo.value = { name: target.petName, cover: target.petCover, breed: '', adoptTime: '' }
    }
    await loadMyRecords()
  } finally { loading.value = false }
}

function formatDate(t) {
  if (!t) return '-'
  if (typeof t === 'string') return t.substring(0, 10)
  return '-'
}

function goBack() { router.back() }

async function handleSubmit() {
  if (!form.value.recordId) {
    ElMessage.warning('请选择领养记录')
    return
  }
  if (form.value.story.trim().length < 10) {
    ElMessage.warning('故事内容至少 10 个字')
    return
  }
  saving.value = true
  try {
    if (isEditMode.value) {
      await updateStory(form.value.recordId, form.value.story.trim())
      ElMessage.success('故事已更新')
    } else {
      await publishStory(form.value.recordId, form.value.story.trim())
      ElMessage.success('故事已发布，管理员审核后展示')
    }
    router.push('/adoption-stories')
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally { saving.value = false }
}
</script>

<style scoped>
.editor-page { max-width: 860px; margin: 0 auto; }

.page-header {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 24px var(--surface-primary);
  position: sticky;
  top: 12px;
  z-index: 10;
}
.header-inner {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  gap: 14px;
}
.back-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: inherit;
}
.back-btn:hover { background: var(--brand-primary-light); color: var(--brand-primary); }
.header-title {
  flex: 1;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}
.header-actions { display: flex; gap: 8px; }

/* 写作卡片 */
.editor-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px var(--surface-primary);
}

/* 宠物信息条 */
.pet-section {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  background: var(--brand-primary-light);
  border-bottom: 1px solid var(--border-light);
}
.pet-section-skel { height: 100px; }
.pet-selector { width: 200px; flex-shrink: 0; }
.pet-empty {
  padding: 40px 20px;
  text-align: center;
}
.pet-cover {
  width: 80px; height: 80px;
  border-radius: 12px;
  flex-shrink: 0;
  background: var(--bg-tertiary);
}
.pet-info { flex: 1; min-width: 0; }
.pet-line { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.pet-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}
.pet-meta { display: flex; flex-wrap: wrap; gap: 12px; font-size: 12px; color: var(--text-secondary); }
.meta-item { display: inline-flex; align-items: center; gap: 4px; }
.meta-item .el-icon { font-size: 14px; }

/* 写作区 */
.write-area { padding: 0; }
.story-textarea {
  width: 100%;
  min-height: 360px;
  padding: 24px 28px;
  border: none;
  background: transparent;
  resize: vertical;
  font-family: inherit;
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-primary);
  outline: none;
}
.story-textarea::placeholder { color: var(--text-tertiary); }
.story-textarea:focus { outline: none; }
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 24px 16px;
  font-size: 12px;
  color: var(--text-tertiary);
}
.char-count { font-weight: 500; }
.hint { font-style: italic; }
</style>
