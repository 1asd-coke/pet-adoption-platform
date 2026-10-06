<template>
  <div class="admin-page">
    <el-card class="theme-card" v-loading="loading">
      <template #header>
        <span>领养故事管理（标记展示）</span>
      </template>

      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="宠物" min-width="100">
          <template #default="{ row }">{{ row.petName || '-' }}</template>
        </el-table-column>
        <el-table-column label="领养人" min-width="100">
          <template #default="{ row }">{{ row.adopterName || '-' }}</template>
        </el-table-column>
        <el-table-column label="故事内容" min-width="250">
          <template #default="{ row }">
            <span v-if="row.story" class="story-preview">{{ row.story }}</span>
            <span v-else class="text-muted">（无）</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="140">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.showcase ? 'success' : 'info'" size="small">
              {{ row.showcase ? '已展示' : '未展示' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.showcase ? 'danger' : 'primary'"
              size="small"
              @click="toggleShow(row)"
            >
              {{ row.showcase ? '取消展示' : '标记展示' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAllStories, toggleShowcase } from '@/api/story'

const list = ref([])
const loading = ref(false)

function formatDate(t) {
  if (!t) return '-'
  return typeof t === 'string' ? t.replace('T', ' ').substring(0, 10) : '-'
}

async function load() {
  loading.value = true
  try {
    const res = await listAllStories()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function toggleShow(row) {
  const newVal = !row.showcase
  try {
    await toggleShowcase(row.id, newVal)
    ElMessage.success(newVal ? '已展示' : '已取消展示')
    load()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.admin-page { max-width: 1100px; margin: 0 auto; }
.story-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13px;
  color: var(--text-secondary);
  max-width: 300px;
}
.text-muted { color: var(--text-tertiary); font-size: 12px; }
</style>
