<template>
  <div class="auth-page">
    <AuroraBg />
    <div class="theme-toggle" @click="showThemeSwitcher" title="切换主题">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="18" height="18"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
    </div>
    <DeveloperCard />
    <div class="auth-card" :class="{ 'slide-right': isRegister }">
      <!-- 紫色面板（左右滑动） -->
      <div class="sliding-panel">
        <div class="panel-content">
          <h2 class="panel-title">{{ isRegister ? '注册' : '登录' }}</h2>
          <div class="form-wrap">
            <transition name="form-fade" mode="out-in">
              <!-- Login Form -->
              <div v-if="!isRegister" class="form-pane active" key="login">
                <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0" class="auth-form" @submit.prevent>
                  <el-form-item prop="username">
                    <el-input v-model="loginForm.username" placeholder="用户名" size="large" />
                  </el-form-item>
                  <el-form-item prop="password">
                    <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="handleLogin" />
                  </el-form-item>
                  <div class="forgot-row">
                    <span class="forgot-link" @click="$router.push('/forgot-password')">忘记密码？</span>
                  </div>
                  <el-form-item>
                    <el-button class="submit-btn" :loading="loginLoading" @click="handleLogin">登 录</el-button>
                  </el-form-item>
                </el-form>
              </div>
              <!-- Register Form -->
              <div v-else class="form-pane active" key="register">
                <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="0" class="auth-form" @submit.prevent>
                  <el-form-item prop="username">
                    <el-input v-model="registerForm.username" placeholder="用户名" size="large" />
                  </el-form-item>
                  <el-form-item prop="password">
                    <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" show-password />
                  </el-form-item>
                  <el-form-item prop="confirmPassword">
                    <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
                  </el-form-item>
                  <el-form-item v-if="showSecurity">
                    <el-select v-model="registerForm.securityQuestion" placeholder="选择密保问题（可选）" size="large" style="width:100%">
                      <el-option v-for="q in securityQuestions" :key="q" :label="q" :value="q" />
                    </el-select>
                  </el-form-item>
                  <el-form-item v-if="showSecurity && registerForm.securityQuestion">
                    <el-input v-model="registerForm.securityAnswer" placeholder="密保答案" size="large" show-password />
                  </el-form-item>
                  <div class="security-toggle" @click="showSecurity = !showSecurity">
                    {{ showSecurity ? '收起密保' : '设置密保问题（忘记密码时使用）' }}
                  </div>
                  <el-form-item>
                    <el-button class="submit-btn" :loading="registerLoading" @click="handleRegister">注 册</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </transition>
          </div>
        </div>
      </div>

      <!-- 白色内容面板（也镜像滑动） -->
      <div class="content-panel">
        <div class="content-inner">
          <!-- 两栏：左文右图。铺满整屏后横向有空间了，插画就不必再和下面的宠物墙上下堆叠 -->
          <div class="brand-head">
            <div class="brand-head-text">
              <div class="brand-mark">
                <span>Claw Pet</span>
                <span class="dot">·</span>
                <span class="cn">宠物领养系统</span>
              </div>
              <h2 class="content-title">
                欢迎回到<span class="brand">宠物之家</span>
              </h2>
              <p class="content-desc">{{ isRegister ? '加入我们，给毛孩子一个温暖的家' : '每一次陪伴，都是温暖的遇见' }}</p>
            </div>
            <div class="pet-illustration">
              <div class="pet-illust-inner">
                <img src="@/assets/images/cat-family.png" alt="猫妈妈与小猫" class="pet-illust" />
              </div>
            </div>
          </div>

          <!-- 平台真实数据（/api/stats/home 无需登录）—— 页面"讲事实"的那一层 -->
          <div v-if="homeStats" class="brand-stats">
            <div class="brand-stat">
              <b>{{ homeStats.availableCount }}</b><span>只待领养</span>
            </div>
            <div class="brand-stat">
              <b>{{ homeStats.adoptedCount }}</b><span>只已回家</span>
            </div>
            <div class="brand-stat">
              <b>{{ homeStats.userCount }}</b><span>位爱心用户</span>
            </div>
          </div>

          <!-- 正在等待领养：拿真实数据填满面板（接口不可用时整块隐藏，插画还在） -->
          <div v-if="waitingPets.length" class="pet-wall">
            <p class="pet-wall-label">正在等待领养</p>
            <div class="pet-wall-row">
              <div
                v-for="(p, i) in waitingPets"
                :key="p.id"
                class="pet-wall-item"
                :style="{ '--i': i }"
              >
                <img :src="p.imageUrls[0]" :alt="p.name" loading="lazy" />
                <span class="pet-wall-name">{{ p.name }}</span>
              </div>
            </div>
          </div>

          <button class="switch-btn" @click="toggleMode">{{ isRegister ? '去登录 →' : '← 去注册' }}</button>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="captchaVisible" :show-close="false" width="400px" :modal-append-to-body="true" class="captcha-dialog">
    <div class="captcha-dialog-body">
      <p class="captcha-hint">{{ captchaMode === 'login' ? '请完成验证后登录' : '请完成验证后注册' }}</p>
      <div class="captcha-image-row">
        <img :src="captchaImage" class="captcha-dialog-img" @click="loadCaptcha" title="点击刷新" />
      </div>
      <el-input v-model="captchaCode" placeholder="请输入验证码" @keyup.enter="submitWithCaptcha" class="captcha-input" />
      <div class="captcha-buttons">
        <el-button @click="captchaVisible = false; captchaLoading = false">取消</el-button>
        <el-button type="primary" :loading="captchaLoading" @click="submitWithCaptcha">确认</el-button>
      </div>
    </div>
  </el-dialog>
  <ThemeSwitcher ref="themeSwitcherRef" mode="panel" />
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, getCaptcha } from '@/api/auth'
import { listPets } from '@/api/pet'
import { getHomeStats } from '@/api/stats'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import AuroraBg from '@/components/AuroraBg.vue'
import DeveloperCard from '@/components/DeveloperCard.vue'

const route = useRoute()
const router = useRouter()
const isRegister = ref(route.path === '/register')
const themeSwitcherRef = ref(null)
function showThemeSwitcher() {
  themeSwitcherRef.value?.show()
}
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

function toggleMode() {
  isRegister.value = !isRegister.value
}

const captchaVisible = ref(false)
const captchaMode = ref('login')
const captchaKey = ref('')
const captchaImage = ref('')
const captchaCode = ref('')
const captchaLoading = ref(false)

async function loadCaptcha() {
  try {
    const res = await getCaptcha()
    captchaKey.value = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
    captchaCode.value = ''
  } catch (e) {}
}

function openCaptcha(mode) {
  captchaMode.value = mode
  captchaVisible.value = true
  captchaLoading.value = false
  loadCaptcha()
}

async function submitWithCaptcha() {
  if (!captchaCode.value) { ElMessage.warning('请输入验证码'); return }
  captchaLoading.value = true
  try {
    if (captchaMode.value === 'login') {
      await userStore.login({
        username: loginForm.username,
        password: loginForm.password,
        captchaKey: captchaKey.value,
        captchaCode: captchaCode.value
      })
      captchaVisible.value = false
      router.push('/home')
    } else {
      await register({
        username: registerForm.username,
        password: registerForm.password,
        captchaKey: captchaKey.value,
        captchaCode: captchaCode.value,
        securityQuestion: registerForm.securityQuestion || undefined,
        securityAnswer: registerForm.securityAnswer || undefined
      })
      captchaVisible.value = false
      ElMessage.success('注册成功，请登录')
      toggleMode()
    }
  } catch {
    // 拦截器已显示过错误提示，这里只刷新验证码
    loadCaptcha()
  } finally {
    captchaLoading.value = false
  }
}

const loginFormRef = ref(null)
const loginLoading = ref(false)
// ⚠️ 不要在这里预填账号密码。以前填的是 admin/admin123，等于任何人打开站点
// 点一下「登录」就直接拿到管理员身份。演示账号写在 README 里即可。
const loginForm = reactive({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerFormRef = ref(null)
const registerLoading = ref(false)
const registerForm = reactive({ username: '', password: '', confirmPassword: '', securityQuestion: '', securityAnswer: '' })
const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }]
}
const showSecurity = ref(false)

/**
 * 正在等待领养的宠物 —— 登录页用它把「插画面板」从"只有一张插画"变成"真的有人在等"。
 * 说明：
 *  - /api/pet/list 在 SecurityConfig 里是 permitAll()，未登录可访问
 *  - 失败一律静默：整块隐藏即可，插画还在，左栏不会空白
 *  - 只取有图的，避免出现空头像
 *  - 注意 axios 拦截器返回的是整个 Result（{code,message,data}），所以走 res.data.records
 */
const waitingPets = ref([])
const homeStats = ref(null)
onMounted(async () => {
  try {
    const res = await listPets({ page: 1, size: 4, status: 'available' })
    waitingPets.value = (res.data?.records || []).filter(p => p.imageUrls?.length)
  } catch {
    /* 接口不可用，静默降级 */
  }
  try {
    const res = await getHomeStats()
    homeStats.value = res.data || null
  } catch {
    /* 同上 */
  }
})

const securityQuestions = [
  '你的小学名字是什么？',
  '你的宠物名字是什么？',
  '你的母亲姓名是什么？',
  '你最喜欢的一本书是什么？',
  '你最难忘的一个城市是哪里？'
]

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  openCaptcha('login')
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  openCaptcha('register')
}
</script>

<style scoped>
* { box-sizing: border-box; }

.theme-toggle {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  z-index: 100;
  transition: all 0.25s ease;
}
.theme-toggle:hover {
  background: rgba(255,255,255,0.8);
  color: var(--brand-primary);
  transform: translateY(-2px);
}

.auth-page {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-page) 0%, var(--brand-primary-light) 50%, var(--bg-page) 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'PingFang SC', sans-serif;
  overflow: hidden;
}

/* ===== Card =====
   不再是一张居中小卡片，而是满屏左右分栏 —— 卡片外面那圈空白才是页面「空」的根源。
   面板宽度比例仍是 45% / 55%，所以 translateX(122.22%) 一行都不用改，滑动动画原样保留。
   背景透明，让背后的光晕能透上来（面板各自带半透明底色）。 */
.auth-card {
  position: relative;
  width: 100%;
  height: 100vh;
  background: transparent;
  overflow: hidden;
}

/* ===== Purple Sliding Panel ===== */
.sliding-panel {
  position: absolute;
  top: 0;
  left: 0;
  width: 45%;
  height: 100%;
  background: var(--brand-primary-light);
  /* 表单这一侧要「实」：输入框必须清晰，所以几乎不透明。
     （注意：不懂 color-mix 的浏览器会整条丢弃、沿用上面那行纯色，
     这正是「保留两条声明」在 color-mix 上可行的原因 —— 它不带 var() 那种计算值阶段失效的坑。） */
  background: color-mix(in srgb, var(--brand-primary-light) 92%, transparent);
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-right .sliding-panel {
  transform: translateX(122.22%);
}

/* 紫色面板右侧的白色三角形 -->
.sliding-panel::after {
  content: '';
  position: absolute;
  top: 50%; right: -20px;
  transform: translateY(-50%);
  width: 0; height: 0;
  border-top: 20px solid transparent;
  border-bottom: 20px solid transparent;
  border-left: 20px solid var(--brand-primary);
}

.slide-right .sliding-panel::after {
  right: auto; left: -20px;
  border-left: none;
  border-right: 20px solid var(--brand-primary);
}

.panel-content {
  width: 100%;
  height: 100%;
  padding: 0 32px;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  text-align: center;
}

/* ===== 表单切换动画 ===== */
.form-wrap {
  width: 100%;
  min-height: 280px;
}
.form-pane {
  width: 100%;
}

/* 进入/离开过渡 */
.form-fade-enter-active,
.form-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.form-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.form-fade-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

.panel-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--brand-primary);
  letter-spacing: 4px;
  text-align: center;
  margin: 0 0 28px;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.5);
}

/* ===== Input Style (filled background) ===== */
.auth-form,
.auth-form :deep(.el-form) {
  display: block;
  width: 100%;
}
.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
  display: block;
  width: 100%;
}
.auth-form :deep(.el-form-item__content) {
  display: block;
  width: 100%;
}
.auth-form :deep(.el-input) {
  display: block;
  width: 100%;
}
.auth-form :deep(.el-input__wrapper) {
  display: flex;
  align-items: center;
  width: 100%;
  background: rgba(255, 255, 255, 0.55) !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  padding: 2px 14px;
  height: 48px;
  transition: all 0.25s ease;
}
.auth-form :deep(.el-input__wrapper:hover),
.auth-form :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.78) !important;
  border-color: var(--brand-primary) !important;
}
.auth-form :deep(.el-input__inner) {
  flex: 1;
  width: 100%;
  color: #2a1f3d !important;
  font-size: 15px;
  caret-color: var(--brand-primary);
}
.auth-form :deep(.el-input__inner::placeholder) {
  color: #8b7aad !important;
}
.auth-form :deep(.el-input__wrapper input:-webkit-autofill) {
  -webkit-text-fill-color: #2a1f3d !important;
  -webkit-box-shadow: 0 0 0 1000px rgba(255, 255, 255, 0.7) inset !important;
}

/* el-select 风格：与 input 一致 */
.auth-form :deep(.el-select) {
  display: block;
  width: 100%;
}
.auth-form :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.55) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  border-radius: 10px !important;
  padding: 2px 14px;
  min-height: 48px;
  transition: all 0.25s ease;
}
.auth-form :deep(.el-select__wrapper.is-focused) {
  background: rgba(255, 255, 255, 0.78) !important;
  border-color: var(--brand-primary) !important;
  box-shadow: none !important;
}
.auth-form :deep(.el-select__placeholder) {
  color: #8b7aad !important;
}

.submit-btn {
  display: flex;
  width: 100%;
  height: 48px;
  background: var(--brand-primary) !important;
  border: none !important;
  border-radius: 10px !important;
  color: #fff !important;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  transition: all 0.25s ease !important;
  margin-top: 12px;
}

.submit-btn:hover {
  background: var(--brand-primary-hover) !important;
  transform: translateY(-1px);
  box-shadow: 0 8px 20px var(--shadow-primary-30);
}

/* ===== White Content Panel (mirror) ===== */
.content-panel {
  position: absolute;
  top: 0;
  right: 0;
  width: 55%;
  height: 100%;
  /* 品牌这一侧完全不铺底色 —— 光晕直接当它的背景。
     试过 0.68 / 0.30 两种半透明白：只要铺色，两侧就会一样淡、分栏读不出来，整页发灰。
     不铺色反而让「左虚右实」的对比成立。 */
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  transition: right 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-right .content-panel {
  right: 45%;
}

.content-inner {
  width: 100%;
  max-width: 680px;
  padding: 0 6%;
  /* 满屏之后横向有余量，左对齐比居中更稳（居中会让 7 层内容晃来晃去） */
  text-align: left;
}

/* 左文右图两栏 */
.brand-head {
  display: flex;
  align-items: center;
  gap: 36px;
  margin-bottom: 30px;
}

.brand-head-text {
  flex: 1;
  min-width: 0;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 14px;
  font-size: 11px;
  letter-spacing: 2.5px;
  color: var(--brand-primary);
  opacity: 0.8;
  white-space: nowrap;
}

.brand-mark .dot {
  margin: 0 2px;
  letter-spacing: 0;
  opacity: 0.55;
}

.brand-mark .cn {
  letter-spacing: 3px;
}

.content-title {
  font-size: 30px;
  font-weight: 600;
  color: var(--brand-primary);
  margin: 0 0 10px;
}

.content-title .brand {
  color: var(--brand-primary);
  font-weight: 700;
  margin: 0 4px;
}

.content-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 18px;
  line-height: 1.5;
}

.pet-illustration {
  /* 两栏布局里的右栏：不再独占一整行垂直空间 */
  flex: none;
  margin: 0;
  display: block;
}
.pet-illust-inner {
  display: inline-block;
  animation: petFloat 4s ease-in-out infinite;
}
.pet-illust {
  width: 240px;
  max-width: 100%;
  height: auto;
  filter: drop-shadow(0 8px 20px var(--shadow-primary-20));
  transition: transform 0.5s ease;
}

/* ===== 平台数据条 ===== */
.brand-stats {
  display: flex;
  gap: 40px;
  margin: 0 0 26px;
  padding: 16px 0;
  border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
}

.brand-stat {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.brand-stat b {
  font-size: 26px;
  font-weight: 700;
  color: var(--brand-primary);
  line-height: 1;
}

.brand-stat span {
  font-size: 12px;
  color: var(--text-secondary);
}
.slide-right .pet-illust {
  transform: scaleX(-1);
}
@keyframes petFloat {
  0%, 100% { transform: translateY(0) rotate(0); }
  50% { transform: translateY(-8px) rotate(-2deg); }
}

/* ===== 正在等待领养 ===== */
.pet-wall {
  margin: 0 0 28px;
}

.pet-wall-label {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--text-secondary);
  letter-spacing: 1px;
}

.pet-wall-row {
  display: flex;
  justify-content: flex-start;
  gap: 24px;
}

.pet-wall-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  /* 错开相位，否则四只整齐划一地晃，反而假 */
  animation: petBob 4s ease-in-out infinite;
  animation-delay: calc(var(--i) * -0.7s);
}

/* 单独写一组 keyframes，不复用 petFloat：那是带 rotate 的，
   转在圆形头像上会让下面的名字跟着歪，看着像排版没对齐 */
@keyframes petBob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.pet-wall-item img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;            /* 宠物图是方图，不裁会拉成椭圆 */
  background: var(--brand-primary-light);
  border: 2px solid #fff;
  box-shadow: 0 4px 12px var(--shadow-primary);
  display: block;
}

.pet-wall-name {
  font-size: 12px;
  color: var(--text-primary);
  opacity: 0.75;
  white-space: nowrap;
}

.switch-btn {
  background: transparent;
  border: 1.5px solid var(--brand-primary);
  color: var(--brand-primary);
  font-size: 13px;
  font-weight: 500;
  padding: 8px 26px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;
  letter-spacing: 1px;
}

.switch-btn:hover {
  background: var(--brand-primary);
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(176, 136, 201, 0.4);
}

.security-toggle {
  text-align: center;
  font-size: 12px;
  font-weight: 500;
  color: var(--brand-primary);
  cursor: pointer;
  margin: -2px 0 10px;
  padding: 6px 12px;
  white-space: nowrap;
  background: rgba(255, 255, 255, 0.6);
  border: 1px dashed var(--brand-primary);
  border-radius: 8px;
  transition: all 0.2s;
}
.security-toggle:hover {
  background: rgba(255, 255, 255, 0.85);
  transform: translateY(-1px);
}

/* ===== 忘记密码链接 ===== */
.forgot-row {
  text-align: right;
  margin: -8px 0 12px;
}
.forgot-link {
  font-size: 12px;
  color: var(--brand-primary);
  cursor: pointer;
  transition: opacity 0.2s;
  text-decoration: none;
}
.forgot-link:hover {
  opacity: 0.7;
  text-decoration: underline;
}

/* ===== Captcha Dialog ===== */
.captcha-dialog :deep(.el-dialog__body) {
  padding: 24px;
}
.captcha-dialog-body { text-align: center; }
.captcha-hint { color: var(--brand-primary); font-size: 14px; margin-bottom: 16px; }
.captcha-image-row { display: flex; justify-content: center; margin-bottom: 16px; }
.captcha-dialog-img {
  height: 44px; border-radius: 6px; cursor: pointer;
  border: 1px solid var(--border-light);
}
.captcha-input { margin-bottom: 16px; }
.captcha-buttons { display: flex; gap: 12px; justify-content: center; }
</style>
