<template>
  <div class="admin-page">
    <el-card class="theme-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>小常识管理</span>
          <el-button type="primary" @click="openAddDialog">新增常识</el-button>
        </div>
      </template>

      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="发布日期" width="140">
          <template #default="{ row }">
            {{ formatDate(row.publishDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑常识' : '新增常识'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="常识标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="支持换行" style="font-family: monospace;" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTips, addTip, updateTip, deleteTip } from '@/api/tip'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({ id: null, title: '', content: '', sort: 0 })

function formatDate(t) {
  if (!t) return '-'
  return typeof t === 'string' ? t.substring(0, 10) : '-'
}

async function load() {
  loading.value = true
  try {
    const res = await listTips({ page: 1, size: 100 })
    const data = res.data || {}
    list.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  form.value = { id: null, title: '', content: '', sort: 0 }
  dialogVisible.value = true
}

function openEditDialog(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await updateTip(form.value)
      ElMessage.success('更新成功')
    } else {
      await addTip(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除这条常识？', '提示', { type: 'warning' })
    await deleteTip(id)
    ElMessage.success('删除成功')
    load()
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.admin-page { max-width: 1200px; margin: 0 auto; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
