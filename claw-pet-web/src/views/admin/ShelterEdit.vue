<template>
  <div class="shelter-edit-page admin-page">
    <el-card shadow="never">
      <template #header>
        <span>收容所信息设置</span>
      </template>
      <el-form :model="form" label-width="120px" v-loading="loading">
        <el-form-item label="收容所名称">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="微信号">
              <el-input v-model="form.wechat" placeholder="请输入微信号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业时间">
              <el-input v-model="form.workHours" placeholder="例如: 周一至周日 09:00-18:00" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="简介描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入简介" />
        </el-form-item>
        <el-form-item label="首页背景图">
          <div v-if="form.image" class="bg-preview">
            <el-image
              :src="form.image"
              :key="form.image"
              style="width: 100%; max-width: 480px; max-height: 320px; border-radius: 12px; object-fit: contain; border: 1px solid #e5e5ea; background: #f5f5f7;"
            >
              <template #error>
                <div style="display:flex;align-items:center;justify-content:center;height:160px;background:#f5f5f7;color:#aeaeb2;font-size:12px;">图片加载失败</div>
              </template>
            </el-image>
            <div style="margin-top: 6px; display: flex; gap: 8px; align-items: center;">
              <span style="font-size: 12px; color: #86868b;">{{ form.image }}</span>
              <el-button size="small" @click="showUrlInput = true">修改地址</el-button>
              <el-button size="small" type="danger" link @click="form.image = ''; showUrlInput = false">移除图片</el-button>
            </div>
          </div>
          <template v-else>
            <el-upload
              action="/api/upload/pet-image"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
            >
              <el-button size="small" :loading="uploading">上传背景图</el-button>
              <template #tip>
                <p style="font-size: 12px; color: #86868b; margin: 4px 0 0;">建议分辨率 1920×1080，超过此尺寸也能上传但加载稍慢，最大 30MB</p>
              </template>
            </el-upload>
            <div style="margin-top: 8px;">
              <el-button size="small" text @click="showUrlInput = true">或输入图片地址</el-button>
            </div>
          </template>
          <div v-if="showUrlInput && !form.image" style="margin-top: 8px; display: flex; gap: 6px;">
            <el-input v-model="urlInput" placeholder="例如 /profile/pet/1.png" size="small" style="width: 300px;" />
            <el-button size="small" type="primary" @click="useUrl">确认</el-button>
            <el-button size="small" @click="showUrlInput = false; urlInput = ''">取消</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getShelter, updateShelter } from '@/api/shelter'
import { ElMessage } from 'element-plus'
import { compressImage } from '@/utils/compressImage'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const showUrlInput = ref(false)
const urlInput = ref('')

const form = reactive({
  name: '',
  address: '',
  phone: '',
  email: '',
  workHours: '',
  wechat: '',
  description: '',
  image: ''
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

function handleUploadSuccess(res) {
  uploading.value = false
  if (res && res.code === 200 && res.data) {
    form.image = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error((res && res.message) || '上传失败')
  }
}

function handleUploadError() {
  uploading.value = false
  ElMessage.error('上传失败，请检查后端服务')
}

function useUrl() {
  if (urlInput.value.trim()) {
    form.image = urlInput.value.trim()
    showUrlInput.value = false
    urlInput.value = ''
  }
}

async function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.error('只能上传图片格式'); return false }
  uploading.value = true
  const compressed = await compressImage(file)
  return compressed !== file ? compressed : true
}

async function loadData() {
  loading.value = true
  try {
    const res = await getShelter()
    if (res.data) {
      Object.assign(form, res.data)
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateShelter({ ...form })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.shelter-edit-page {
  padding: 0;
}
</style>
