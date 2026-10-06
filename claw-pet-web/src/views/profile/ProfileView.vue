<template>
  <div class="profile-page page-wrapper">
    <!-- 顶部 Hero -->
    <section class="profile-hero">
      <div class="hero-inner">
        <div class="hero-avatar-wrap">
          <el-avatar :size="88" :src="form.avatar" class="hero-avatar">
            {{ (userStore.userInfo?.nickname || userStore.username)?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <el-upload
            class="avatar-upload"
            action="/api/upload/avatar"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <button class="change-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="12" height="12"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
              换
            </button>
          </el-upload>
        </div>
        <div class="hero-info">
          <h1 class="hero-name">{{ userStore.userInfo?.nickname || userStore.username }}</h1>
          <div class="hero-username">@{{ userStore.username }}</div>
          <div class="hero-tags">
            <span class="hero-tag tag-role" :class="`role-${userStore.userInfo?.role}`">
              <span class="tag-dot"></span>
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </span>
            <span class="hero-tag tag-auth" :class="`auth-${authStatus}`">
              <span class="tag-dot"></span>
              实名 {{ authStatusLabel }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- Tabs -->
    <div class="tab-bar">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="tab-btn"
        :class="{ active: activeTab === t.key }"
        @click="activeTab = t.key"
      >
        <span class="tab-icon" v-html="t.icon"></span>
        <span class="tab-label">{{ t.label }}</span>
      </button>
    </div>

    <!-- 个人信息 -->
    <div v-show="activeTab === 'profile'" class="content-card">
      <div class="card-head">
        <div>
          <h3>个人信息</h3>
          <p>完善资料后申请领养更顺利</p>
        </div>
        <div class="card-deco">
          <svg viewBox="0 0 100 100" width="80" height="80" fill="none" opacity="0.18">
            <circle cx="50" cy="35" r="14" stroke="var(--brand-primary)" stroke-width="1.5"/>
            <path d="M25 80 Q 25 60 50 60 Q 75 60 75 80" stroke="var(--brand-primary)" stroke-width="1.5" fill="none"/>
          </svg>
        </div>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="84px" v-loading="loading" class="profile-form">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" size="large" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" size="large" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" size="large" />
        </el-form-item>
        <el-form-item label="居住地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="3" placeholder="请输入居住地址" />
        </el-form-item>
        <el-form-item>
          <button class="primary-btn" :disabled="saving" @click="handleSave">
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
          <button class="ghost-btn" @click="resetForm">重置</button>
          <button class="ghost-btn" @click="showSampleDialog = true" style="margin-left:8px">示例数据</button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 密保问题 -->
    <div v-show="activeTab === 'security'" class="content-card">
      <div class="card-head">
        <div>
          <h3>密保问题</h3>
          <p>忘记密码时可用于验证身份</p>
        </div>
        <div class="card-deco">
          <svg viewBox="0 0 100 100" width="80" height="80" fill="none" opacity="0.18">
            <circle cx="50" cy="50" r="30" stroke="var(--brand-primary)" stroke-width="1.5"/>
            <path d="M50 30 L 50 50 L 65 60" stroke="var(--brand-primary)" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="50" cy="50" r="2" fill="var(--brand-primary)"/>
          </svg>
        </div>
      </div>

      <div v-if="securityList.length" class="sec-list">
        <div v-for="(item, i) in securityList" :key="item.id" class="sec-item">
          <div class="sec-num">{{ i + 1 }}</div>
          <div class="sec-content">
            <div class="sec-q">{{ item.question }}</div>
            <div class="sec-meta">已加密保存</div>
          </div>
          <button class="text-danger" @click="handleDeleteSecurity(item.id)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-2 14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2L5 6"/></svg>
            删除
          </button>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 60 60" width="50" height="50" fill="none" opacity="0.4">
            <circle cx="30" cy="30" r="22" stroke="var(--brand-primary)" stroke-width="2"/>
            <path d="M22 28 a 8 8 0 0 1 16 0 c 0 6 -8 8 -8 8" stroke="var(--brand-primary)" stroke-width="2" fill="none" stroke-linecap="round"/>
            <circle cx="30" cy="44" r="1.5" fill="var(--brand-primary)"/>
          </svg>
        </div>
        <p>暂未设置密保问题</p>
        <span>设置后可在忘记密码时验证身份</span>
      </div>

      <div class="add-section">
        <div class="add-title">添加新问题</div>
        <el-select v-model="newSecForm.question" placeholder="选择问题" size="large" style="width:100%">
          <el-option v-for="opt in availableSecurityOptions" :key="opt.value" :label="opt.label" :value="opt.value" :disabled="!!opt.disabled" />
        </el-select>
        <el-input v-model="newSecForm.answer" type="password" placeholder="请输入答案" show-password size="large" style="margin-top:12px" />
        <button class="primary-btn" style="width:100%; margin-top:16px" :disabled="secSaving" @click="handleAddSecurity">
          {{ secSaving ? '添加中...' : '添加密保' }}
        </button>
      </div>
    </div>

    <!-- 示例数据弹窗 -->
    <el-dialog v-model="showSampleDialog" title="示例个人信息（点击填入或复制）" width="680px">
      <div class="sample-list">
        <div v-for="(s, i) in sampleData" :key="i" class="sample-item">
          <div class="sample-head">第 {{ i + 1 }} 组</div>
          <textarea readonly class="sample-text" :value="formatSample(s)" rows="5"></textarea>
          <div class="sample-actions">
            <button class="primary-btn" @click="fillSample(s)">填入此组</button>
            <button class="ghost-btn" @click="copySample(s)">复制</button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 修改密码 -->
    <div v-show="activeTab === 'password'" class="content-card">
      <div class="card-head">
        <div>
          <h3>修改密码</h3>
          <p>用当前密码或密保问题验证身份</p>
        </div>
        <div class="card-deco">
          <svg viewBox="0 0 100 100" width="80" height="80" fill="none" opacity="0.18">
            <rect x="30" y="45" width="40" height="32" rx="4" stroke="var(--brand-primary)" stroke-width="1.5"/>
            <path d="M37 45 V 35 a 13 13 0 0 1 26 0 V 45" stroke="var(--brand-primary)" stroke-width="1.5" fill="none"/>
            <circle cx="50" cy="60" r="3" fill="var(--brand-primary)"/>
          </svg>
        </div>
      </div>

      <div class="verify-mode">
        <button class="mode-btn" :class="{ active: pwdVerifyMode === 'password' }" @click="pwdVerifyMode = 'password'">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
          <span>当前密码</span>
        </button>
        <button class="mode-btn" :class="{ active: pwdVerifyMode === 'security' }" :disabled="!securityList.length" @click="pwdVerifyMode = 'security'">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/></svg>
          <span>密保验证</span>
        </button>
      </div>

      <div v-if="pwdVerifyMode === 'password'" class="form-group">
        <label>当前密码</label>
        <el-input v-model="pwdForm.currentPassword" type="password" placeholder="请输入当前密码" show-password size="large" />
      </div>
      <div v-else>
        <div class="form-group">
          <label>选择密保问题</label>
          <el-select v-model="pwdSecQuestionId" placeholder="选择问题" size="large" style="width:100%">
            <el-option v-for="q in securityList" :key="q.id" :label="q.question" :value="q.id" />
          </el-select>
        </div>
        <div class="form-group">
          <label>密保答案</label>
          <el-input v-model="pwdSecAnswer" type="password" placeholder="请输入答案" show-password size="large" @keyup.enter="handleChangePwd" />
        </div>
      </div>

      <div class="form-group">
        <label>新密码（至少6位）</label>
        <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password size="large" />
      </div>
      <div class="form-group">
        <label>确认新密码</label>
        <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password size="large" @keyup.enter="handleChangePwd" />
      </div>

      <button class="primary-btn" style="width:100%; margin-top:8px" :disabled="pwdLoading" @click="handleChangePwd">
        {{ pwdLoading ? '修改中...' : '修改密码' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateProfile, addSecurityQuestion, deleteSecurityQuestion, getSecurityQuestions, login } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const tabs = [
  { key: 'profile', label: '个人信息', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>' },
  { key: 'security', label: '密保问题', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>' },
  { key: 'password', label: '修改密码', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/></svg>' }
]
const activeTab = ref('profile')

const loading = ref(false)
const saving = ref(false)
const authStatus = ref('unauth')
const authStatusLabel = computed(() => ({
  unauth: '未认证', pending: '审核中', verified: '已认证'
})[authStatus.value] || '未认证')

const form = reactive({ avatar: '', nickname: '', phone: '', realName: '', idCard: '', address: '' })
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))
const rules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  idCard: [{ pattern: /^\d{17}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }]
}

function initForm() {
  const u = userStore.userInfo || {}
  form.avatar = u.avatar || ''
  form.nickname = u.nickname || ''
  form.phone = u.phone || ''
  form.realName = u.realName || ''
  form.idCard = u.idCard || ''
  form.address = u.address || ''
  authStatus.value = u.authStatus || 'unauth'
}

function handleAvatarSuccess(res) {
  if (res.code === 200 && res.data) { form.avatar = res.data; ElMessage.success('头像更新成功') }
}
function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt2M) ElMessage.error('头像大小不能超过 2MB')
  return isImage && isLt2M
}

const formRef = ref(null)
async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateProfile({ nickname: form.nickname, phone: form.phone, realName: form.realName, idCard: form.idCard, address: form.address, avatar: form.avatar })
    ElMessage.success('保存成功')
    await userStore.fetchUser()
    authStatus.value = userStore.userInfo?.authStatus || 'unauth'
  } finally { saving.value = false }
}
function resetForm() { initForm() }

// ====== 多密保 ======
const securityList = ref([])
const secSaving = ref(false)
const newSecForm = reactive({ question: '', answer: '' })
const allSecOptions = [
  { label: '你母亲的名字？', value: '你母亲的名字？' },
  { label: '你父亲的名字？', value: '你父亲的名字？' },
  { label: '你的出生地是？', value: '你的出生地是？' },
  { label: '你第一只宠物的名字？', value: '你第一只宠物的名字？' },
  { label: '你最喜欢的颜色？', value: '你最喜欢的颜色？' },
  { label: '你就读的第一所学校？', value: '你就读的第一所学校？' }
]
const usedQuestions = computed(() => new Set(securityList.value.map(q => q.question)))
const availableSecurityOptions = computed(() => allSecOptions.map(opt => ({ ...opt, disabled: usedQuestions.value.has(opt.value) })))

async function loadSecurityQuestions() {
  try {
    const res = await getSecurityQuestions(userStore.username)
    securityList.value = (res.data || []).map(item => {
      const colon = item.indexOf(':')
      return { id: Number(item.substring(0, colon)), question: item.substring(colon + 1) }
    })
  } catch {}
}

async function handleAddSecurity() {
  if (!newSecForm.question) return ElMessage.warning('请选择一个密保问题')
  if (!newSecForm.answer) return ElMessage.warning('请输入密保答案')
  if (usedQuestions.value.has(newSecForm.question)) return ElMessage.warning('该问题已添加，请选择其他问题')
  secSaving.value = true
  try {
    await addSecurityQuestion(newSecForm.question, newSecForm.answer)
    ElMessage.success('添加成功')
    newSecForm.question = ''
    newSecForm.answer = ''
    await loadSecurityQuestions()
  } finally { secSaving.value = false }
}

async function handleDeleteSecurity(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个密保问题吗？', '确认', { type: 'warning' })
    await deleteSecurityQuestion(id)
    ElMessage.success('已删除')
    await loadSecurityQuestions()
  } catch {} finally { /* */ }
}

// ====== 修改密码 ======
const pwdVerifyMode = ref('password')
const pwdLoading = ref(false)
const pwdSecQuestionId = ref(null)
const pwdSecAnswer = ref('')
const pwdForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

async function handleChangePwd() {
  if (!pwdForm.newPassword || pwdForm.newPassword.length < 6) {
    return ElMessage.warning('新密码不能少于6位')
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    return ElMessage.warning('两次输入的密码不一致')
  }
  pwdLoading.value = true
  try {
    const { changePassword } = await import('@/api/auth')
    if (pwdVerifyMode.value === 'password') {
      if (!pwdForm.currentPassword) {
        return ElMessage.warning('请输入当前密码')
      }
      // 直接用 token 修改
      await changePassword({ username: userStore.username, oldPassword: pwdForm.currentPassword, newPassword: pwdForm.newPassword })
    } else {
      if (!pwdSecQuestionId.value) return ElMessage.warning('请选择密保问题')
      if (!pwdSecAnswer.value) return ElMessage.warning('请输入密保答案')
      // 用密保问题验证身份
      const { verifyAnswer } = await import('@/api/auth')
      const res = await verifyAnswer(userStore.username, pwdSecQuestionId.value, pwdSecAnswer.value)
      if (res.code !== 200) throw new Error(res.message || '密保验证失败')
      await changePassword({ username: userStore.username, oldPassword: '', newPassword: pwdForm.newPassword })
    }
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '密码修改失败')
  } finally {
    pwdLoading.value = false
  }
}

onMounted(async () => {
  // 未登录先跳转登录
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  loading.value = true
  await userStore.fetchUser()
  initForm()
  await loadSecurityQuestions()
  loading.value = false
})

// ====== 示例数据 ======
const showSampleDialog = ref(false)
const sampleData = [
  { nickname: '微风', phone: '13812345678', realName: '张伟', idCard: '110101199003078917', address: '北京市东城区王府井大街88号' },
  { nickname: '小溪', phone: '13987654321', realName: '李娜', idCard: '31011519920515432X', address: '上海市浦东新区世纪大道100号' },
  { nickname: '向阳', phone: '15611112222', realName: '王强', idCard: '440305198807120035', address: '广东省深圳市南山区科技园路3号' },
  { nickname: '木棉', phone: '18899998888', realName: '刘洋', idCard: '330106199412096218', address: '浙江省杭州市西湖区文三路200号' },
  { nickname: '暖阳', phone: '17755667788', realName: '陈敏', idCard: '510104198611233324', address: '四川省成都市锦江区春熙路50号' }
]
function formatSample(s) {
  return '昵称：' + s.nickname + '\n手机号：' + s.phone + '\n真实姓名：' + s.realName + '\n身份证号：' + s.idCard + '\n居住地址：' + s.address
}
function fillSample(s) {
  Object.assign(form, s)
  showSampleDialog.value = false
  ElMessage.success('已填入示例数据')
}
async function copySample(s) {
  const text = formatSample(s)
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动选中复制')
  }
}
</script>

<style scoped>
.profile-page { padding-top: 8px; padding-bottom: 60px; }

/* ===== Hero ===== */
.profile-hero {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 24px 32px;
  margin-bottom: 16px;
  box-shadow: 0 8px 32px var(--surface-primary);
  animation: slideDown 0.5s ease;
}
.hero-inner { display: flex; align-items: center; gap: 24px; }
.hero-avatar-wrap { position: relative; }
.hero-avatar {
  background: linear-gradient(135deg, var(--brand-primary-light), var(--brand-primary)) !important;
  color: #fff !important;
  font-size: 32px;
  font-weight: 700;
  border: 3px solid #fff;
  box-shadow: 0 8px 24px var(--shadow-primary-25);
}
.avatar-upload { position: absolute; bottom: 0; right: 0; }
.change-btn {
  display: flex; align-items: center; gap: 3px;
  background: #fff;
  border: 1px solid var(--border-light);
  padding: 3px 8px;
  border-radius: 14px;
  font-size: 11px;
  color: var(--brand-primary);
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.change-btn:hover { background: var(--brand-primary-light); border-color: var(--brand-primary); }

.hero-info { flex: 1; }
.hero-name {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 4px;
  letter-spacing: -0.02em;
}
.hero-username { font-size: 13px; color: var(--text-secondary); margin-bottom: 10px; }
.hero-tags { display: flex; gap: 8px; }
.hero-tag {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}
.tag-role { background: rgba(212, 120, 158, 0.1); color: var(--brand-primary); }
.tag-role.role-admin { background: rgba(255, 94, 138, 0.1); color: #ff5e8a; }
.tag-auth { background: rgba(120, 120, 120, 0.08); color: var(--text-secondary); }
.tag-auth.auth-pending { background: rgba(255, 149, 0, 0.1); color: #ff9500; }
.tag-auth.auth-verified { background: rgba(52, 199, 89, 0.1); color: #34c759; }
.tag-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

/* ===== Tabs ===== */
.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  border-radius: 14px;
  padding: 4px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  animation: slideDown 0.5s ease 0.1s both;
}
.tab-btn {
  flex: 1;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px 16px;
  background: transparent;
  border: none;
  border-radius: 10px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;
}
.tab-btn:hover { color: var(--text-primary); }
.tab-btn.active {
  background: #fff;
  color: var(--brand-primary);
  font-weight: 600;
  box-shadow: 0 2px 8px var(--shadow-primary-hover);
}
.tab-icon { display: inline-flex; align-items: center; }

/* ===== Content Card ===== */
.content-card {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px var(--surface-primary);
  animation: slideDown 0.5s ease 0.2s both;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px dashed var(--border-light);
}
.card-head h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
  letter-spacing: -0.01em;
}
.card-head p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}
.card-deco { flex-shrink: 0; }

/* ===== Profile Form ===== */
.profile-form :deep(.el-form-item__label) {
  color: var(--text-primary);
  font-weight: 500;
}

/* ===== Security List ===== */
.sec-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 24px; }
.sec-item {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 18px;
  background: #fff;
  border-radius: 14px;
  border: 0.5px solid var(--border-light);
  transition: all 0.25s ease;
}
.sec-item:hover { border-color: var(--shadow-primary-30); box-shadow: 0 4px 12px var(--surface-primary); }
.sec-num {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--brand-primary-light), var(--brand-primary));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.sec-content { flex: 1; }
.sec-q { font-size: 14px; color: var(--text-primary); font-weight: 500; }
.sec-meta { font-size: 11px; color: #b0a8c0; margin-top: 2px; }
.text-danger {
  background: none; border: none;
  color: #ff5e8a; font-size: 12px;
  cursor: pointer;
  display: flex; align-items: center; gap: 4px;
  padding: 4px 10px;
  border-radius: 8px;
  transition: all 0.2s;
  font-family: inherit;
}
.text-danger:hover { background: rgba(255, 94, 138, 0.1); }

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #aeaeb2;
  background: rgba(120, 120, 120, 0.04);
  border-radius: 14px;
  margin-bottom: 24px;
}
.empty-state p { font-size: 14px; color: var(--text-secondary); margin: 8px 0 4px; }
.empty-state span { font-size: 12px; }

.add-section {
  padding: 20px;
  background: var(--surface-primary);
  border-radius: 16px;
  border: 1px dashed #e8a0c8;
}
.add-title {
  font-size: 13px;
  color: var(--brand-primary);
  font-weight: 600;
  margin-bottom: 12px;
}

/* ===== Verify Mode ===== */
.verify-mode {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  background: var(--surface-primary);
  padding: 4px;
  border-radius: 12px;
}
.mode-btn {
  flex: 1;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;
}
.mode-btn:hover:not(:disabled) { color: var(--brand-primary); }
.mode-btn.active {
  background: #fff;
  color: var(--brand-primary);
  box-shadow: 0 2px 8px var(--shadow-primary-hover);
}
.mode-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.form-group { margin-bottom: 16px; }
.form-group label {
  display: block;
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 6px;
}

/* ===== Buttons ===== */
.primary-btn {
  display: inline-flex; align-items: center; justify-content: center;
  background: var(--brand-primary);
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  letter-spacing: 0.05em;
  transition: all 0.25s ease;
  box-shadow: 0 4px 14px var(--shadow-primary-30);
}
.primary-btn:hover:not(:disabled) {
  background: var(--brand-primary-active);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px var(--shadow-primary-40);
}
.primary-btn:disabled { background: #c4b8d8; cursor: not-allowed; box-shadow: none; }
.ghost-btn {
  background: transparent;
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  padding: 10px 20px;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  margin-left: 8px;
  font-family: inherit;
  transition: all 0.2s;
}
.ghost-btn:hover { background: var(--brand-primary-light); color: var(--text-primary); border-color: #e8a0c8; }

/* ===== Sample Data Dialog ===== */
.sample-list { display: flex; flex-direction: column; gap: 14px; max-height: 60vh; overflow-y: auto; padding-right: 4px; }
.sample-item { background: #f8f6fb; border-radius: 12px; padding: 14px 16px; border: 1px solid #efe9f4; }
.sample-head { font-size: 13px; font-weight: 600; color: var(--brand-primary); margin-bottom: 8px; }
.sample-text {
  width: 100%; font-family: 'Consolas', 'Monaco', monospace; font-size: 12px;
  border: 1px solid #e8e0ef; border-radius: 8px; padding: 10px 12px;
  background: #fff; resize: none; line-height: 1.7;
  color: var(--text-primary);
}
.sample-actions { margin-top: 10px; display: flex; gap: 8px; }

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .hero-inner { flex-direction: column; text-align: center; gap: 16px; }
  .hero-tags { justify-content: center; }
  .content-card { padding: 20px; }
  .card-head { flex-direction: column; gap: 12px; }
}
</style>
