<template>
  <div class="pet-manage-page admin-page">
    <!-- Toolbar -->
    <el-card class="theme-card" style="margin-bottom: 16px;">
      <div class="toolbar">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增宠物
        </el-button>
        <div class="toolbar-divider"></div>
        <el-input v-model="searchKeyword" placeholder="搜索宠物名称" clearable @clear="loadList" @keyup.enter="loadList" style="flex: 1; min-width: 160px;" />
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadList" style="width: 140px;">
          <el-option label="全部" value="" />
          <el-option label="待领养" value="available" />
          <el-option label="领养中" value="adopting" />
          <el-option label="已领养" value="adopted" />
        </el-select>
        <el-select v-model="categoryFilter" placeholder="分类筛选" clearable @change="loadList" style="width: 140px;">
          <el-option label="全部" value="" />
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-button type="primary" plain @click="loadList">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <!-- Table -->
    <el-card class="theme-card">
      <el-table :data="petList" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="图片" width="60">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrls?.[0]"
              style="width: 36px; height: 36px; border-radius: 6px;"
              fit="cover"
            >
              <template #error><div style="text-align:center;font-size:18px;">🐾</div></template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column label="分类" min-width="100">
          <template #default="{ row }">
            {{ row.categoryName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="性别" width="60" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.gender === 'male' ? '#667eea' : '#f472b6', fontWeight: 600 }">
              {{ row.gender === 'male' ? '♂' : '♀' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <span class="status-pill" :class="`status-${row.status}`">
              <span class="status-dot"></span>
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="80" align="center" />
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">
            {{ row.createdAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="primary" link @click="toggleStatus(row)">
              {{ row.status === 'available' ? '下架' : '上架' }}
            </el-button>
            <el-popconfirm
              :title="`确定删除「${row.name}」？`"
              confirm-button-text="删除"
              cancel-button-text="取消"
              confirm-button-type="danger"
              icon-color="#ff5e8a"
              width="240"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > 0">
        <span class="page-info">共 {{ totalPages }} 页</span>
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadList"
          background
        />
      </div>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑宠物' : '新增宠物'"
      width="620px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" v-loading="saving">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入宠物名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品种" prop="breed">
              <el-input v-model="form.breed" placeholder="品种" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model="form.age" placeholder="如：2岁" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="male">♂ 公</el-radio>
                <el-radio value="female">♀ 母</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="体重" prop="weight">
              <el-input v-model="form.weight" placeholder="如：4.5kg" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康状态" prop="healthStatus">
              <el-select v-model="form.healthStatus" placeholder="健康状态" style="width: 100%">
                <el-option label="健康" value="healthy" />
                <el-option label="生病" value="sick" />
                <el-option label="康复中" value="recovering" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="疫苗" prop="vaccineStatus">
          <el-radio-group v-model="form.vaccineStatus">
            <el-radio value="unvaccinated">未接种</el-radio>
            <el-radio value="vaccinated">已接种</el-radio>
            <el-radio value="vaccinating">接种中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="领养状态" prop="status">
          <el-select v-model="form.status" placeholder="领养状态" style="width: 100%">
            <el-option label="待领养" value="available" />
            <el-option label="领养中" value="adopting" />
            <el-option label="已领养" value="adopted" />
            <el-option label="已下架" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="宠物描述" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="/api/upload/pet-image"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemoveImage"
            :file-list="imageList"
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { compressImage } from '@/utils/compressImage'
import { ElMessage } from 'element-plus'
import { listPets, createPet, updatePet, deletePet, listCategories } from '@/api/pet'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const petList = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const totalPages = computed(() => Math.ceil(total.value / size.value) || 1)
const searchKeyword = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const formRef = ref(null)
const imageList = ref([])

const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  breed: '',
  age: '',
  gender: 'male',
  weight: '',
  healthStatus: 'healthy',
  vaccineStatus: 'unvaccinated',
  status: 'available',
  description: '',
  imageUrls: []
})

const rules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function statusLabel(status) {
  if (status === 'available') return '待领养'
  if (status === 'adopting') return '领养中'
  return '已领养'
}

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (statusFilter.value) params.status = statusFilter.value
    if (categoryFilter.value) params.categoryId = categoryFilter.value
    const res = await listPets(params)
    petList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  searchKeyword.value = ''
  statusFilter.value = ''
  categoryFilter.value = ''
  page.value = 1
  loadList()
}

function openAddDialog() {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.categoryId = null
  form.breed = ''
  form.age = ''
  form.gender = 'male'
  form.weight = ''
  form.healthStatus = 'healthy'
  form.vaccineStatus = 'unvaccinated'
  form.status = 'available'
  form.description = ''
  form.imageUrls = []
  imageList.value = []
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.categoryId = row.categoryId
  form.breed = row.breed || ''
  form.age = row.age || ''
  form.gender = row.gender || 'male'
  form.weight = row.weight || ''
  form.healthStatus = row.healthStatus || 'healthy'
  form.vaccineStatus = row.vaccineStatus || 'unvaccinated'
  form.status = row.status || 'available'
  form.description = row.description || ''
  form.imageUrls = row.imageUrls || []
  imageList.value = (row.imageUrls || []).map(url => ({ url, name: url.split('/').pop() }))
  dialogVisible.value = true
}

function handleUploadSuccess(res) {
  if (res.code === 200 && res.data) {
    form.imageUrls.push(res.data)
    imageList.value.push({ url: res.data, name: res.data.split('/').pop() })
  }
}

function handleRemoveImage(uploadFile, fileList) {
  form.imageUrls = fileList.map(f => f.url || f.response?.data)
  imageList.value = fileList
}

async function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.error('只能上传图片格式'); return false }
  const compressed = await compressImage(file)
  return compressed !== file ? compressed : true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updatePet({ ...form })
    } else {
      await createPet({ ...form })
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deletePet(id)
    ElMessage.success('删除成功')
    loadList()
  } catch { /* ignore */ }
}

async function toggleStatus(row) {
  const newStatus = row.status === 'available' ? 'adopted' : 'available'
  try {
    await updatePet({ id: row.id, status: newStatus })
    ElMessage.success(newStatus === 'available' ? '已上架' : '已下架')
    loadList()
  } catch { /* ignore */ }
}

onMounted(async () => {
  try {
    const catRes = await listCategories()
    categories.value = (catRes.data || []).filter(c => c.status === 'active')
  } catch { /* ignore */ }
  loadList()
})
</script>

<style scoped>
.pet-manage-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.toolbar-divider {
  width: 1px;
  height: 18px;
  background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.1), transparent);
  margin: 0 4px;
}

/** 状态徽章 - 灵动小圆点 */
.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 8px;
  white-space: nowrap;
}
.status-pill .status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 0 2px currentColor;
  opacity: 0.4;
}
.status-pill.status-available { background: rgba(52, 199, 89, 0.12); color: #34c759; }
.status-pill.status-adopting { background: rgba(255, 149, 0, 0.12); color: #ff9500; }
.status-pill.status-adopted { background: rgba(120, 120, 130, 0.12); color: #787882; }
.pagination-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 16px 0 8px;
  flex-shrink: 0;
}
.page-info {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}
/* 表格卡片撑满剩余空间，分页始终固定在底部 */
.pet-manage-page .theme-card:last-child {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.pet-manage-page .theme-card:last-child :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding-bottom: 0;
}
/* 表格内容区可滚动，表头固定 */
.pet-manage-page .theme-card:last-child :deep(.el-table) {
  flex: 1;
  overflow-y: auto;
}
</style>
