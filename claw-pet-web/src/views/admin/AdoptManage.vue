<template>
  <div class="adopt-manage-page admin-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-mini stat-total" @click="resetFilter">
        <div class="mini-num">{{ appList.length }}</div>
        <div class="mini-label">全部申请</div>
      </div>
      <div class="stat-mini stat-pending" @click="statusFilter = 'pending'">
        <div class="mini-num">{{ listByStatus('pending').length }}</div>
        <div class="mini-label">待审核</div>
      </div>
      <div class="stat-mini stat-approved" @click="statusFilter = 'approved'">
        <div class="mini-num">{{ listByStatus('approved').length }}</div>
        <div class="mini-label">已通过</div>
      </div>
      <div class="stat-mini stat-rejected" @click="statusFilter = 'rejected'">
        <div class="mini-num">{{ listByStatus('rejected').length }}</div>
        <div class="mini-label">已拒绝</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索申请人、宠物名、手机号" clearable style="width:300px" size="default" @keyup.enter="loadList" />
      <div class="filter-right">
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:130px" @change="loadList">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
      </div>
    </div>

    <!-- 主表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="filteredList" v-loading="loading" stripe style="width:100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="userName" label="申请人" min-width="100" />
        <el-table-column prop="petName" label="宠物名称" min-width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/pet/detail/${row.petId}`)">{{ row.petName }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ row.createdAt?.replace('T', ' ').substring(0, 16) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="领养理由" min-width="160">
          <template #default="{ row }">
            <span class="reason-text">{{ row.reason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="plain">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              size="small" link type="primary"
              @click="openReviewDialog(row)"
            >审核</el-button>
            <el-tag v-else-if="row.status === 'approved'" type="success" size="small">已通过</el-tag>
            <el-button v-else-if="row.status === 'rejected'" size="small" link type="info" @click="showRejectReason(row)">原因</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="inline-empty">暂无数据</div>
        </template>
      </el-table>
    </el-card>

    <!-- Review Dialog -->
    <el-dialog v-model="reviewVisible" width="640px" :show-close="false" align-center custom-class="review-dialog">
      <template #header>
        <div class="review-header">
          <div class="review-header-left">
            <h3>审核领养申请</h3>
            <p>仔细核对申请人信息后选择审核结果</p>
          </div>
          <button class="review-close" @click="reviewVisible = false">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </template>

      <!-- 申请人卡片 -->
      <div class="applicant-card">
        <div class="applicant-avatar">
          <img v-if="reviewData.userAvatar" :src="reviewData.userAvatar" />
          <span v-else>{{ reviewData.userName?.charAt(0) || '?' }}</span>
        </div>
        <div class="applicant-info">
          <div class="applicant-name">{{ reviewData.userName }}</div>
          <div class="applicant-pet">申请领养 <b>{{ reviewData.petName }}</b></div>
        </div>
        <div class="applicant-time">{{ reviewData.createdAt?.replace('T', ' ').substring(0, 16) }}</div>
      </div>

      <!-- 信息列表 -->
      <div class="review-fields">
        <div class="field">
          <div class="field-label">联系电话</div>
          <div class="field-value">{{ reviewData.phone || '-' }}</div>
        </div>
        <div class="field">
          <div class="field-label">联系地址</div>
          <div class="field-value">{{ reviewData.address || '-' }}</div>
        </div>
        <div class="field field-full">
          <div class="field-label">申请理由</div>
          <div class="field-value field-text">{{ reviewData.reason || '-' }}</div>
        </div>
      </div>

      <!-- 拒绝输入区 -->
      <div v-if="showRejectInput" class="reject-input-area">
        <div class="reject-label">拒绝原因（必填）</div>
        <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请说明拒绝原因" maxlength="200" show-word-limit />
      </div>

      <template #footer>
        <div class="review-footer">
          <button class="review-btn review-btn-reject" :loading="reviewLoading && !showRejectInput" @click="showRejectInput = true" v-if="!showRejectInput">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            拒绝
          </button>
          <button class="review-btn review-btn-confirm-reject" :loading="reviewLoading" @click="handleReview('rejected')" v-if="showRejectInput">
            确认拒绝
          </button>
          <button class="review-btn review-btn-approve" :loading="reviewLoading && !showRejectInput" @click="handleReview('approved')" v-if="!showRejectInput">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><polyline points="20 6 9 17 4 12"/></svg>
            通过
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listApplications, reviewApplication, getApplication } from '@/api/adopt'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const reviewLoading = ref(false)
const appList = ref([])
const reviewVisible = ref(false)
const showRejectInput = ref(false)
const reviewData = ref({})
const rejectReason = ref('')
const keyword = ref('')
const statusFilter = ref('')

function listByStatus(status) {
  return appList.value.filter(a => a.status === status)
}

const filteredList = computed(() => {
  let list = appList.value
  if (statusFilter.value) list = list.filter(a => a.status === statusFilter.value)
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(a =>
      (a.userName || '').toLowerCase().includes(kw) ||
      (a.petName || '').toLowerCase().includes(kw) ||
      (a.phone || '').toLowerCase().includes(kw)
    )
  }
  return list
})

function statusTagType(status) {
  return { pending: 'warning', approved: 'success', rejected: 'info' }[status] || 'info'
}
function statusLabel(status) {
  return { pending: '待审核', approved: '已通过', rejected: '已拒绝' }[status] || status
}
function resetFilter() {
  keyword.value = ''
  statusFilter.value = ''
}

async function loadList() {
  loading.value = true
  try {
    const res = await listApplications({})
    appList.value = res.data?.records || res.data || []
  } finally {
    loading.value = false
  }
}

async function openReviewDialog(row) {
  try {
    const res = await getApplication(row.id)
    reviewData.value = res.data || row
    showRejectInput.value = false
    rejectReason.value = ''
    reviewVisible.value = true
  } catch { /* ignore */ }
}

async function handleReview(status) {
  reviewLoading.value = true
  try {
    await reviewApplication({
      id: reviewData.value.id,
      status,
      rejectReason: status === 'rejected' ? rejectReason.value : ''
    })
    ElMessage.success(status === 'approved' ? '已通过' : '已拒绝')
    reviewVisible.value = false
    loadList()
  } finally {
    reviewLoading.value = false
  }
}

function viewAdopter(row) {
  ElMessageBox.alert(
    `姓名：${row.realName || '-'}\n电话：${row.phone || '-'}\n地址：${row.address || '-'}`,
    '领养人信息',
    { confirmButtonText: '确定' }
  )
}

function showRejectReason(row) {
  ElMessageBox.alert(
    row.rejectReason || row.remark || '无',
    '拒绝原因',
    { confirmButtonText: '知道了' }
  )
}

onMounted(loadList)
</script>

<style scoped>
/* 审核弹窗样式 */
.review-dialog {
  border-radius: 16px;
  overflow: hidden;
}
.review-dialog :deep(.el-dialog__header),
.review-dialog :deep(.el-dialog__body),
.review-dialog :deep(.el-dialog__footer) {
  padding: 0;
  margin: 0;
}
.review-dialog :deep(.el-dialog__body) {
  padding: 0 24px 8px;
}
.review-dialog :deep(.el-dialog__footer) {
  padding: 12px 24px 20px;
  border-top: 1px solid var(--border-light);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--border-light);
}
.review-header h3 {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}
.review-header p {
  margin: 0;
  font-size: 13px;
  color: var(--text-tertiary);
}
.review-close {
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 4px;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
}
.review-close:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.applicant-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--surface-primary);
  border-radius: 12px;
  margin: 16px 0;
}
.applicant-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--brand-primary-light);
  color: var(--brand-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  overflow: hidden;
  flex-shrink: 0;
}
.applicant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.applicant-info { flex: 1; min-width: 0; }
.applicant-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.applicant-pet {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.applicant-pet b {
  color: var(--brand-primary);
  font-weight: 600;
}
.applicant-time {
  font-size: 11px;
  color: var(--text-tertiary);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.review-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding-bottom: 8px;
}
.field {
  background: var(--bg-tertiary);
  border-radius: 10px;
  padding: 12px 14px;
}
.field-full { grid-column: span 2; }
.field-label {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-bottom: 4px;
  font-weight: 500;
}
.field-value {
  font-size: 14px;
  color: var(--text-primary);
  word-break: break-all;
}
.field-text {
  line-height: 1.6;
  font-size: 13px;
}

.reject-input-area {
  padding: 12px 0;
}
.reject-label {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 600;
  margin-bottom: 6px;
}

.review-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.review-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border: 1px solid var(--border-light);
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  color: var(--text-primary);
  font-family: inherit;
  transition: all 0.2s;
}
.review-btn:hover {
  background: var(--bg-tertiary);
}
.review-btn-approve {
  background: var(--brand-primary);
  color: #fff;
  border-color: var(--brand-primary);
}
.review-btn-approve:hover {
  background: var(--brand-primary-active);
}
.review-btn-reject {
  background: rgba(245, 108, 108, 0.06);
  color: #f56c6c;
  border-color: rgba(245, 108, 108, 0.3);
}
.review-btn-reject:hover {
  background: #f56c6c;
  color: #fff;
}
.review-btn-confirm-reject {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
}
.review-btn-confirm-reject:hover {
  background: #d45a5a;
}
.adopt-manage-page {
  padding: 0;
}
.table-card {
  border-radius: 12px;
  margin-bottom: 12px;
}
.table-card:last-child { margin-bottom: 0; }
.section-head {
  margin-bottom: 12px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.count {
  background: var(--surface-primary);
  color: var(--brand-primary);
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
}
.inline-empty {
  padding: 24px 0;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
}
.review-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
.reject-input-area {
  margin-top: 12px;
  padding: 12px;
  background: #fef0f0;
  border-radius: 8px;
}
.reject-btn {
  margin-top: 8px;
}

/* 统计卡片 */
.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
.stat-mini {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--border-light);
}
.stat-mini:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.stat-total { background: var(--bg-page); }
.stat-pending { background: rgba(255, 149, 0, 0.06); border-color: rgba(255, 149, 0, 0.2); }
.stat-approved { background: rgba(52, 199, 89, 0.06); border-color: rgba(52, 199, 89, 0.2); }
.stat-rejected { background: rgba(174, 174, 178, 0.06); border-color: rgba(174, 174, 178, 0.2); }
.mini-num { font-size: 28px; font-weight: 800; color: var(--text-primary); line-height: 1; }
.stat-pending .mini-num { color: #ff9500; }
.stat-approved .mini-num { color: #34c759; }
.stat-rejected .mini-num { color: var(--text-tertiary); }
.mini-label { font-size: 12px; color: var(--text-secondary); margin-top: 4px; }
.stat-clear {
  flex-shrink: 0;
  border: 1px solid var(--border-light);
  background: #fff;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  color: var(--text-secondary);
  font-family: inherit;
}
.stat-clear:hover { color: var(--brand-primary); border-color: var(--brand-primary); }

/* 搜索栏 */.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  background: #fff;
  padding: 12px 16px;
  border-radius: 12px;
  border: 0.5px solid var(--border-light);
}
.filter-right {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

/* 表格 */
.reason-text {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--text-secondary);
  font-size: 12px;
}
.inline-empty {
  padding: 24px 0;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
}
</style>
