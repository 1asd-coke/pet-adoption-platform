<template>
  <div class="category-manage-page admin-page">
    <!-- Add Toolbar -->
    <el-card class="toolbar-card" shadow="never">
      <el-button type="primary" :icon="'Plus'" @click="openAddDialog" :loading="adding">新增分类</el-button>
    </el-card>

    <!-- Table -->
    <el-card class="table-card" shadow="never">
      <div v-loading="loading" class="cat-table-wrap">
        <!-- Header -->
        <div class="cat-table-header">
          <span class="col-drag"></span>
          <span class="col-idx">#</span>
          <span class="col-name">名称</span>
          <span class="col-sort">排序</span>
          <span class="col-status">状态</span>
          <span class="col-ops">操作</span>
        </div>
        <!-- Draggable List -->
        <draggable
          v-model="categories"
          handle=".drag-handle"
          animation="250"
          ghost-class="cat-ghost"
          chosen-class="cat-chosen"
          drag-class="cat-dragging"
          @end="onDragEnd"
          item-key="id"
        >
          <template #item="{ element: row, index }">
            <div class="cat-table-row" :class="{ 'cat-row-disabled': row.status === 'disabled' }">
              <span class="col-drag drag-handle">⋮⋮</span>
              <span class="col-idx">{{ index + 1 }}</span>
              <span class="col-name" :class="row.status === 'disabled' ? 'cat-disabled' : ''">{{ row.name }}</span>
              <span class="col-sort">{{ row.sort }}</span>
              <span class="col-status">
                <el-tag :type="row.status !== 'disabled' ? 'success' : 'danger'" size="small">
                  {{ row.status !== 'disabled' ? '启用' : '禁用' }}
                </el-tag>
              </span>
              <span class="col-ops">
                <el-button text type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
                <el-button text type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
              </span>
            </div>
          </template>
        </draggable>
        <el-empty v-if="!loading && !categories.length" description="暂无分类数据" />
      </div>
    </el-card>

    <!-- Add Dialog -->
    <el-dialog v-model="addDialogVisible" width="500px" :show-close="false" align-center custom-class="cat-dialog">
      <template #header>
        <div class="cat-dialog-header">
          <div>
            <h3>新增分类</h3>
            <p>填写分类基本信息</p>
          </div>
          <button class="cat-dialog-close" @click="addDialogVisible = false">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </template>
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="addForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="addForm.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="cat-dialog-footer">
          <button class="cat-btn cat-btn-cancel" @click="addDialogVisible = false">取消</button>
          <button class="cat-btn cat-btn-confirm" :loading="adding" @click="handleAddFromDialog">保存</button>
        </div>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" title="编辑分类" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="editForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import draggable from 'vuedraggable'

const loading = ref(false)
const adding = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const addDialogVisible = ref(false)
const categories = ref([])
const newName = ref('')

const editForm = ref({ id: null, name: '', sort: 0, enabled: true })
const addForm = ref({ name: '', sort: 0, enabled: true })

function openAddDialog() {
  addForm.value = { name: '', sort: 0, enabled: true }
  addDialogVisible.value = true
}

async function handleAddFromDialog() {
  if (!addForm.value.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  adding.value = true
  try {
    await createCategory({ name: addForm.value.name })
    ElMessage.success('新增成功')
    addDialogVisible.value = false
    loadList()
  } finally {
    adding.value = false
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await listCategories()
    categories.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleAdd() {
  if (!newName.value.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  adding.value = true
  try {
    await createCategory({ name: newName.value })
    ElMessage.success('新增成功')
    newName.value = ''
    loadList()
  } finally {
    adding.value = false
  }
}

function openEditDialog(row) {
  editForm.value = {
    id: row.id,
    name: row.name,
    sort: row.sort ?? 0,
    enabled: row.status !== 'disabled'
  }
  dialogVisible.value = true
}

async function handleEdit() {
  if (!editForm.value.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  saving.value = true
  try {
    await updateCategory(editForm.value.id, {
      name: editForm.value.name,
      sort: editForm.value.sort,
      status: editForm.value.enabled ? 'active' : 'disabled'
    })
    ElMessage.success('更新成功')
    dialogVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', { type: 'warning' })
    await deleteCategory(id)
    ElMessage.success('删除成功')
    loadList()
  } catch { /* ignore cancel */ }
}

async function moveUp(row, index) {
  if (index === 0) return
  const prev = categories.value[index - 1]
  const tmp = prev.sort
  prev.sort = row.sort
  row.sort = tmp
  await Promise.all([updateCategory(prev.id, { sort: prev.sort }), updateCategory(row.id, { sort: row.sort })])
}
async function moveDown(row, index) {
  if (index === categories.value.length - 1) return
  await moveUp(categories.value[index + 1], index + 1)
}
async function onDragEnd() {
  // 更新所有分类的 sort 值为新的索引位置
  const updates = categories.value.map((c, i) => updateCategory(c.id, { sort: i + 1 }))
  await Promise.all(updates)
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.category-manage-page {
  padding: 0;
}
.toolbar-card {
  border-radius: 12px;
  margin-bottom: 16px;
}
.table-card {
  border-radius: 12px;
}

/* 自定义表格布局 */
.cat-table-wrap { position: relative; }
.cat-table-header,
.cat-table-row {
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-size: 13px;
  border-bottom: 1px solid var(--border-light);
  transition: background 0.15s;
}
.cat-table-header {
  background: var(--bg-page);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 12px;
  height: 48px;
  border-top: none;
  user-select: none;
}
.cat-table-row {
  min-height: 52px;
  cursor: default;
}
.cat-table-row:hover {
  background: var(--surface-primary);
}
.cat-row-disabled {
  opacity: 0.6;
}

/* 拖拽把手 */
.drag-handle {
  cursor: grab;
  color: var(--text-tertiary);
  letter-spacing: 2px;
  user-select: none;
  touch-action: none;
}
.drag-handle:active { cursor: grabbing; }

/* 列宽 */
.col-drag { width: 40px; flex-shrink: 0; text-align: center; }
.col-idx  { width: 50px; flex-shrink: 0; text-align: center; color: var(--text-tertiary); }
.col-name { flex: 1; min-width: 0; font-weight: 500; }
.col-sort { width: 80px; flex-shrink: 0; text-align: center; color: var(--text-tertiary); }
.col-status { width: 100px; flex-shrink: 0; text-align: center; }
.col-ops { width: 180px; flex-shrink: 0; text-align: right; }

/* 拖动过渡动画 */
.cat-table-row {
  transition: transform 0.25s ease, background 0.15s;
}
.cat-ghost {
  opacity: 0.3;
  background: var(--surface-primary-strong) !important;
  border: 2px dashed var(--brand-primary) !important;
  border-radius: 8px;
}
.cat-chosen {
  background: var(--surface-primary-strong) !important;
}
.cat-dragging {
  background: #fff !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  border-radius: 10px;
  z-index: 999;
  transform: scale(1.02);
}

.cat-disabled {
  color: #aeaeb2;
  text-decoration: line-through;
}
.cat-dialog {
  border-radius: 16px;
  overflow: hidden;
}
.cat-dialog :deep(.el-dialog__header),
.cat-dialog :deep(.el-dialog__body),
.cat-dialog :deep(.el-dialog__footer) {
  padding: 0;
  margin: 0;
}
.cat-dialog :deep(.el-dialog__body) { padding: 0 24px; }
.cat-dialog :deep(.el-dialog__footer) {
  padding: 12px 24px 20px;
  border-top: 1px solid var(--border-light);
}
.cat-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--border-light);
}
.cat-dialog-header h3 {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}
.cat-dialog-header p {
  margin: 0;
  font-size: 13px;
  color: var(--text-tertiary);
}
.cat-dialog-close {
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 4px;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
}
.cat-dialog-close:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}
.cat-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.cat-btn {
  padding: 10px 22px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  border: 1px solid var(--border-light);
  background: #fff;
  color: var(--text-primary);
  transition: all 0.2s;
}
.cat-btn:hover { background: var(--bg-tertiary); }
.cat-btn-confirm {
  background: var(--brand-primary);
  color: #fff;
  border-color: var(--brand-primary);
}
.cat-btn-confirm:hover {
  background: var(--brand-primary-active);
}
</style>
