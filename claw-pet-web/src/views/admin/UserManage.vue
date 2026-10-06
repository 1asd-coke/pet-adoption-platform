<template>
  <div class="user-manage-page admin-page">
    <el-card class="table-card" shadow="never">
      <el-table :data="userList" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="认证状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="!row.authStatus || row.authStatus === 'unauth'" type="info" size="small">未认证</el-tag>
            <el-tag v-else-if="row.authStatus === 'pending'" type="warning" size="small">审核中</el-tag>
            <el-tag v-else-if="row.authStatus === 'verified'" type="success" size="small">已认证</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="160">
          <template #default="{ row }">
            {{ row.createTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'admin'"
              text
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >删除</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !userList.length" description="暂无用户数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listUsers, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])

async function loadList() {
  loading.value = true
  try {
    const res = await listUsers()
    userList.value = res.data?.records || res.data || []
  } finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复。', '警告', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadList()
  } catch { /* ignore cancel */ }
}

onMounted(loadList)
</script>

<style scoped>
.user-manage-page {
  padding: 0;
}
.table-card {
  border-radius: 12px;
}
</style>
