<template>
  <div class="record-manage-page admin-page">
    <el-card class="table-card" shadow="never">
      <el-table :data="records" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="petName" label="宠物名称" min-width="120" />
        <el-table-column prop="adopterName" label="领养人" min-width="100" />
        <el-table-column label="领养日期" min-width="160">
          <template #default="{ row }">
            {{ row.formattedAdoptDate || (row.adoptTime ? row.adoptTime.replace('T', ' ').substring(0, 16) : '-') }}
          </template>
        </el-table-column>
        <el-table-column label="回访状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.followupStatus === 'done' ? 'success' : row.followupStatus === 'pending' ? 'warning' : 'info'" size="small">
              {{ row.followupStatus === 'done' ? '已完成' : row.followupStatus === 'pending' ? '待回访' : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openFollowupDialog(row)">回访</el-button>
            <el-button type="success" size="small" @click="viewFollowups(row)">查看回访</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !records.length" description="暂无领养记录" />

      <div class="pagination-wrapper" v-if="total > 0">
        <span class="page-info">共 {{ totalPages }} 页</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadList"
          background
        />
      </div>
    </el-card>

    <!-- Add Followup Dialog -->
    <el-dialog v-model="followupVisible" title="添加回访记录" width="500px">
      <el-form :model="followupForm" label-width="80px">
        <el-form-item label="宠物">
          <el-input :model-value="followupForm.petName" disabled />
        </el-form-item>
        <el-form-item label="领养人">
          <el-input :model-value="followupForm.adopterName" disabled />
        </el-form-item>
        <el-form-item label="回访内容" required>
          <el-input v-model="followupForm.content" type="textarea" :rows="4" placeholder="请输入回访内容" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="/api/upload/pet-image"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleFollowupUploadSuccess"
            :on-remove="handleFollowupRemoveImage"
            :file-list="followupImageList"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div style="font-size: 12px; color: #86868b; margin-top: 4px;">JPG/PNG/GIF，单张不超过 30MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followupVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAddFollowup">保存</el-button>
      </template>
    </el-dialog>

    <!-- View Followups Dialog -->
    <el-dialog v-model="viewVisible" title="回访记录" width="600px">
      <div v-loading="viewLoading">
        <div v-for="item in followups" :key="item.id" class="followup-item">
          <div class="followup-header">
            <span class="followup-time">{{ item.createTime || '-' }}</span>
          </div>
          <div class="followup-content">{{ item.content }}</div>
          <div v-if="parseImages(item.images).length" class="followup-images">
            <el-image
              v-for="(img, idx) in parseImages(item.images)"
              :key="idx"
              :src="img"
              fit="cover"
              class="followup-img"
              :preview-src-list="parseImages(item.images)"
            />
          </div>
        </div>
        <el-empty v-if="!viewLoading && !followups.length" description="暂无回访记录" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { Plus } from '@element-plus/icons-vue'
import { listRecords, addFollowup, listFollowups } from '@/api/record'
import { ElMessage } from 'element-plus'
import { compressImage } from '@/utils/compressImage'

const loading = ref(false)
const saving = ref(false)
const viewLoading = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(15)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)
const followupVisible = ref(false)
const viewVisible = ref(false)
const followups = ref([])
const followupImageList = ref([])

const userStore = useUserStore()
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.token}` }))

async function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.error('只能上传图片格式'); return false }
  const compressed = await compressImage(file)
  return compressed !== file ? compressed : true
}

const followupForm = ref({
  recordId: null,
  petName: '',
  adopterName: '',
  content: ''
})

async function loadList() {
  loading.value = true
  try {
    const res = await listRecords({ page: currentPage.value, size: pageSize.value })
    records.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function openFollowupDialog(row) {
  if (followupForm.value.recordId !== row.id) {
    // 同一条记录关闭后再开，保留输入不重置
    followupForm.value = {
      recordId: row.id,
      petName: row.petName || '-',
      adopterName: row.adopterName || '-',
      content: ''
    }
    followupImageList.value = []
  } else {
    followupForm.value.recordId = row.id
    followupForm.value.petName = row.petName || '-'
    followupForm.value.adopterName = row.adopterName || '-'
    // content / followupImageList 保留
  }
  followupVisible.value = true
}

function handleFollowupUploadSuccess(res, file) {
  if (res.code === 200) {
    followupImageList.value.push({ url: res.data, name: file.name })
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function handleFollowupRemoveImage(file) {
  const idx = followupImageList.value.findIndex(f => f.url === file.url)
  if (idx > -1) followupImageList.value.splice(idx, 1)
}

async function handleAddFollowup() {
  if (!followupForm.value.content.trim()) {
    ElMessage.warning('请输入回访内容')
    return
  }
  saving.value = true
  try {
    await addFollowup({
      recordId: followupForm.value.recordId,
      content: followupForm.value.content,
      images: JSON.stringify(followupImageList.value.map(f => f.url))
    })
    ElMessage.success('回访记录添加成功')
    followupVisible.value = false
    // 保存成功后清空，下次新回访重新输入
    followupForm.value.content = ''
    followupImageList.value = []
    loadList()
  } finally {
    saving.value = false
  }
}

async function viewFollowups(row) {
  viewVisible.value = true
  viewLoading.value = true
  try {
    const res = await listFollowups(row.id)
    followups.value = res.data || []
  } finally {
    viewLoading.value = false
  }
}

/** 解析后端返回的图片字段（JSON 字符串） */
function parseImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

onMounted(loadList)
</script>

<style scoped>
.record-manage-page {
  padding: 0;
}
.table-card {
  border-radius: 12px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 8px 0;
}
.page-info {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}
.followup-item {
  padding: 16px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}
.followup-header {
  margin-bottom: 8px;
}
.followup-time {
  font-size: 12px;
  color: #909399;
}
.followup-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}
.followup-images {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.followup-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  cursor: pointer;
}
</style>
