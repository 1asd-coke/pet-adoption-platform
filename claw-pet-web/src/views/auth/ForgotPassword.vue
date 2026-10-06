<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <div class="forgot-back" @click="$router.push('/login')">
        <el-icon><ArrowLeft /></el-icon> 返回登录
      </div>

      <!-- 左侧：视觉与插画 -->
      <div class="forgot-visual">
        <div class="visual-decoration">
          <div class="deco-circle deco-1"></div>
          <div class="deco-circle deco-2"></div>
        </div>
        <div class="visual-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="32" height="32">
            <rect x="3" y="11" width="18" height="11" rx="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            <circle cx="12" cy="16" r="1.5" fill="currentColor"/>
          </svg>
        </div>
        <h2 class="visual-title">找回密码</h2>
        <p class="visual-desc">只需 3 步，重新设置您的密码</p>
        <ul class="visual-tips">
          <li>· 确保您的账号已设置密保</li>
          <li>· 新密码需 6-20 位字符</li>
          <li>· 建议使用字母数字组合</li>
        </ul>
      </div>

      <!-- 右侧：步骤 + 表单 -->
      <div class="forgot-form">

      <!-- Steps -->
      <div class="steps-bar">
        <div class="step" :class="{ active: step === 1, done: step > 1 }">
          <span class="step-num">{{ step > 1 ? '✓' : 1 }}</span>
          <span class="step-label">验证</span>
        </div>
        <div class="step-line" :class="{ active: step > 1 }" />
        <div class="step" :class="{ active: step === 2, done: step > 2 }">
          <span class="step-num">{{ step > 2 ? '✓' : 2 }}</span>
          <span class="step-label">密保</span>
        </div>
        <div class="step-line" :class="{ active: step > 2 }" />
        <div class="step" :class="{ active: step === 3 }">
          <span class="step-num">3</span>
          <span class="step-label">重置</span>
        </div>
      </div>

      <!-- Step 1: 输入用户名 -->
      <div v-if="step === 1" class="step-form">
        <el-form ref="step1Ref" :model="step1Form" :rules="step1Rules" label-width="0">
          <el-form-item prop="username">
            <el-input v-model="step1Form.username" placeholder="请输入用户名" size="large" clearable @keyup.enter="handleStep1">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button class="step-btn" type="primary" :loading="step1Loading" @click="handleStep1" size="large">
              下一步
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 2: 选择问题 + 回答 -->
        <div v-if="step === 2" class="step-form">
          <p class="step-hint">请选择一个密保问题并回答：</p>
          <div
            v-for="q in questionList"
            :key="q.id"
            class="question-card"
            :class="{ selected: selectedQuestionId === q.id }"
            @click="selectedQuestionId = q.id"
          >
            <span class="q-radio">{{ selectedQuestionId === q.id ? '◉' : '○' }}</span>
            <span class="q-text">{{ q.question }}</span>
          </div>
          <el-form ref="step2Ref" :model="step2Form" :rules="step2Rules" label-width="0" style="margin-top:16px">
            <el-form-item prop="answer">
              <el-input v-model="step2Form.answer" placeholder="请输入该问题的答案" size="large" :disabled="!selectedQuestionId" @keyup.enter="handleStep2">
                <template #prefix>
                  <el-icon><QuestionFilled /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button class="step-btn" type="primary" :loading="step2Loading" :disabled="!selectedQuestionId" @click="handleStep2" size="large">
                验证
              </el-button>
            </el-form-item>
          </el-form>
          <button class="step-back-btn" @click="step = 1">
            <el-icon><ArrowLeft /></el-icon> 返回上一步
          </button>
        </div>

      <!-- Step 3: 设置新密码 -->
      <div v-if="step === 3" class="step-form">
        <el-form ref="step3Ref" :model="step3Form" :rules="step3Rules" label-width="0">
          <el-form-item prop="newPassword">
            <el-input v-model="step3Form.newPassword" type="password" placeholder="请输入新密码（至少6位）" size="large" show-password>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="step3Form.confirmPassword" type="password" placeholder="请确认新密码" size="large" show-password @keyup.enter="handleStep3">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button class="step-btn" type="primary" :loading="step3Loading" @click="handleStep3" size="large">
              重置密码
            </el-button>
          </el-form-item>
        </el-form>
        <button class="step-back-btn" @click="step = 2">
          <el-icon><ArrowLeft /></el-icon> 返回上一步
        </button>
      </div>

      <!-- Success -->
      <div v-if="step === 4" class="step-form success-box">
        <div class="success-icon">✓</div>
        <h3>密码重置成功</h3>
        <p>请使用新密码登录</p>
        <el-button class="step-btn" type="primary" @click="$router.push('/login')" size="large">
          去登录
        </el-button>
      </div>
      </div>  <!-- forgot-form -->
    </div>  <!-- forgot-card -->
  </div>  <!-- forgot-page -->
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, QuestionFilled, ArrowLeft } from '@element-plus/icons-vue'
import { getSecurityQuestions, verifyAnswer, resetPassword } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()

const step = ref(1)
const questionList = ref([])
const selectedQuestionId = ref(null)
const resetToken = ref('')

// Step 1
const step1Ref = ref(null)
const step1Loading = ref(false)
const step1Form = reactive({ username: '' })
const step1Rules = { username: [{ required: true, message: '请输入用户名', trigger: 'blur' }] }

// Step 2
const step2Ref = ref(null)
const step2Loading = ref(false)
const step2Form = reactive({ answer: '' })
const step2Rules = { answer: [{ required: true, message: '请输入答案', trigger: 'blur' }] }

// Step 3
const step3Ref = ref(null)
const step3Loading = ref(false)
const step3Form = reactive({ newPassword: '', confirmPassword: '' })
const validateConfirm = (rule, value, callback) => {
  if (value !== step3Form.newPassword) callback(new Error('两次输入的密码不一致'))
  else callback()
}
const step3Rules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleStep1() {
  const valid = await step1Ref.value?.validate().catch(() => false)
  if (!valid) return
  step1Loading.value = true
  try {
    const res = await getSecurityQuestions(step1Form.username)
    // 后端返回 ["1:问题1", "3:问题3"] 格式
    questionList.value = (res.data || []).map(item => {
      const colon = item.indexOf(':')
      return { id: Number(item.substring(0, colon)), question: item.substring(colon + 1) }
    })
    if (questionList.value.length === 0) {
      ElMessage.warning('该用户未设置密保问题，请联系管理员重置密码')
      return
    }
    selectedQuestionId.value = questionList.value[0]?.id || null
    step.value = 2
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '用户不存在或未设置密保')
  } finally {
    step1Loading.value = false
  }
}

async function handleStep2() {
  const valid = await step2Ref.value?.validate().catch(() => false)
  if (!valid) return
  if (!selectedQuestionId.value) return ElMessage.warning('请选择一个密保问题')
  step2Loading.value = true
  try {
    const res = await verifyAnswer(step1Form.username, selectedQuestionId.value, step2Form.answer)
    resetToken.value = res.data
    step.value = 3
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '答案错误')
  } finally {
    step2Loading.value = false
  }
}

async function handleStep3() {
  const valid = await step3Ref.value?.validate().catch(() => false)
  if (!valid) return
  step3Loading.value = true
  try {
    await resetPassword(resetToken.value, step3Form.newPassword)
    step.value = 4
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '重置失败，请重试')
  } finally {
    step3Loading.value = false
  }
}
</script>

<style scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-page) 0%, var(--brand-primary-light) 50%, var(--bg-page) 100%);
  background-attachment: fixed;
}
.forgot-card {
  position: relative;
  width: 1000px;
  height: 560px;
  display: flex;
  background: var(--bg-card);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px var(--shadow-primary-hover);
}
.forgot-back {
  position: absolute;
  top: 20px;
  left: 20px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  z-index: 10;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  padding: 6px 14px;
  border-radius: 16px;
  transition: all 0.25s;
  font-family: inherit;
}
.forgot-back:hover {
  color: var(--brand-primary);
  border-color: var(--brand-primary);
  background: var(--brand-primary-light);
}

/* 左侧视觉区 */
.forgot-visual {
  width: 35%;
  background: var(--brand-primary-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
  text-align: center;
  color: #fff;
  position: relative;
  overflow: hidden;
}

/* 装饰圆 */
.visual-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}
.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
}
.deco-1 {
  width: 120px;
  height: 120px;
  top: -40px;
  right: -40px;
  background: rgba(255, 255, 255, 0.35);
}
.deco-2 {
  width: 80px;
  height: 80px;
  bottom: 30px;
  left: -30px;
  background: rgba(255, 255, 255, 0.25);
}

.visual-icon {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--brand-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 8px 24px var(--shadow-primary-30);
  z-index: 1;
}
.visual-icon svg {
  width: 36px;
  height: 36px;
}
.visual-title {
  position: relative;
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--text-primary);
  z-index: 1;
}
.visual-desc {
  position: relative;
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 20px;
  z-index: 1;
}
.visual-tips {
  position: relative;
  list-style: none;
  padding: 12px 16px;
  margin: 0;
  background: rgba(255, 255, 255, 0.55);
  border-radius: 10px;
  font-size: 12px;
  color: var(--text-secondary);
  text-align: left;
  line-height: 1.9;
  z-index: 1;
}

/* 右侧表单区 */
.forgot-form {
  flex: 1;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow-y: auto;
}
.forgot-form :deep(.el-form) {
  width: 100%;
  max-width: 420px;
}
.form-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}
.form-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0 0 28px;
}
.card-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0 0 30px;
  text-align: center;
}

/* Steps */
.steps-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
  max-width: 420px;
  margin-left: auto;
  margin-right: auto;
}
.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.step-num {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
  transition: all 0.3s;
}
.step.active .step-num {
  background: var(--brand-primary);
  color: #fff;
}
.step.done .step-num {
  background: var(--color-success);
  color: #fff;
}
.step-label {
  font-size: 13px;
  color: var(--text-tertiary);
  white-space: nowrap;
}
.step.active .step-label {
  color: var(--brand-primary);
  font-weight: 500;
}
.step.done .step-label {
  color: var(--color-success);
}
.step-line {
  width: 60px;
  height: 2px;
  background: var(--bg-tertiary);
  margin: 0 8px;
  margin-bottom: 28px;
  transition: background 0.3s;
}
.step-line.active {
  background: var(--color-success);
}

/* Step Form */
.step-form {
  max-width: 420px;
  margin: 0 auto;
}
.step-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 12px;
}
.step-btn {
  display: flex;
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 10px;
  margin-top: 8px;
  font-weight: 600;
  letter-spacing: 2px;
}
.step-back {
  text-align: center;
  margin-top: 12px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}
.step-back:hover {
  color: var(--brand-primary);
}
.step-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 16px;
  cursor: pointer;
  margin: 8px auto 0;
  display: flex;
  width: fit-content;
  transition: all 0.25s;
  font-family: inherit;
}
.step-back-btn:hover {
  color: var(--brand-primary);
  border-color: var(--brand-primary);
  background: var(--brand-primary-light);
}

/* Question Card */
.question-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.question-card:hover {
  border-color: var(--brand-primary);
  background: var(--surface-primary);
}
.question-card.selected {
  border-color: var(--brand-primary);
  background: var(--surface-primary);
}
.q-radio {
  font-size: 18px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}
.question-card.selected .q-radio {
  color: var(--brand-primary);
}
.q-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.4;
}

/* Success */
.success-box {
  text-align: center;
}
.success-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--color-success);
  color: #fff;
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}
.success-box h3 {
  font-size: 18px;
  color: var(--text-primary);
  margin: 0 0 8px;
}
.success-box p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 28px;
}
</style>
