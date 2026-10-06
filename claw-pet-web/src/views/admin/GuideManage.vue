<template>
  <div class="guide-manage-page">
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">领养须知编辑</h1>
        <p class="page-subtitle">为每一行规则独立编辑，自动编号，所见即所得</p>
      </div>
      <div class="header-actions">
        <span v-if="lastSavedAt" class="saved-tip">
          <el-icon><CircleCheckFilled /></el-icon>
          已保存于 {{ lastSavedAt }}
        </span>
        <el-button @click="showPreview = true">
          <el-icon><View /></el-icon>预览效果
        </el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Check /></el-icon>保存修改
        </el-button>
      </div>
    </header>

    <div class="editor-card">
      <div class="meta-section">
        <div class="meta-row">
          <span class="meta-label">文档标题</span>
          <el-input v-model="form.title" placeholder="领养须知" class="meta-input" />
        </div>
        <div class="meta-stats">
          <span class="stat-item">
            <el-icon><DocumentChecked /></el-icon>
            共 <strong>{{ rules.length }}</strong> 条规则
          </span>
          <span class="stat-item">
            <el-icon><EditPen /></el-icon>
            总字数 <strong>{{ totalChars }}</strong>
          </span>
        </div>
      </div>

      <div class="rules-section">
        <div
          v-for="(rule, idx) in rules"
          :key="rule.id"
          class="rule-card"
        >
          <div class="rule-index">{{ idx + 1 }}</div>
          <el-input
            v-model="rule.text"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 6 }"
            :placeholder="`请输入第 ${idx + 1} 条规则`"
            class="rule-input"
            @input="onRuleChange"
          />
          <div class="rule-meta">
            <span class="char-count" :class="{ warn: rule.text.length > 80 }">
              {{ rule.text.length }} 字
            </span>
            <div class="rule-actions">
              <button
                v-if="idx > 0"
                class="icon-btn"
                title="上移"
                @click="moveUp(idx)"
              >
                <el-icon><ArrowUp /></el-icon>
              </button>
              <button
                v-if="idx < rules.length - 1"
                class="icon-btn"
                title="下移"
                @click="moveDown(idx)"
              >
                <el-icon><ArrowDown /></el-icon>
              </button>
              <button
                class="icon-btn danger"
                title="删除"
                @click="removeRule(idx)"
              >
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </div>
        </div>

        <div class="add-rule-row">
          <button class="add-btn" @click="addRule">
            <el-icon><Plus /></el-icon>
            <span>添加新规则</span>
          </button>
          <button class="add-btn" @click="addAfterLast" v-if="rules.length === 0">
            <el-icon><EditPen /></el-icon>
            <span>写第一条规则</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <el-dialog v-model="showPreview" title="前台展示预览" width="600px" align-center>
      <div class="preview-frame">
        <h2 class="preview-title">{{ form.title || '领养须知' }}</h2>
        <div class="preview-list">
          <div v-for="(rule, idx) in nonEmptyRules" :key="rule.id" class="preview-line">
            <span class="preview-index">{{ idx + 1 }}.</span>
            <span class="preview-text">{{ rule.text }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showPreview = false">关闭预览</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGuide, updateGuide } from '@/api/guide'

const form = ref({ id: 1, title: '领养须知', content: '' })
const rules = ref([])
const saving = ref(false)
const showPreview = ref(false)
const lastSavedAt = ref(null)  // 最近保存时间
let nextId = 1

const totalChars = computed(() => rules.value.reduce((sum, r) => sum + r.text.length, 0))

const nonEmptyRules = computed(() => rules.value.filter(r => r.text.trim()))

function makeRule(text) {
  return { id: nextId++, text: text || '' }
}

async function load() {
  const res = await getGuide().catch(() => ({ data: { title: '领养须知', content: '' } }))
  form.value = res.data || { id: 1, title: '领养须知', content: '' }
  // 解析 content 为规则数组
  const lines = (form.value.content || '').split('\n').map(s => s.trim()).filter(Boolean)
  rules.value = lines.length
    ? lines.map((line, i) => {
        // 去掉已有的数字编号（"1、xxx" → "xxx"）
        const cleaned = line.replace(/^(\d+)[\s,、.．]+/, '').trim()
        return makeRule(cleaned)
      })
    : [makeRule('')]
  if (res.data?.updatedAt) {
    lastSavedAt.value = res.data.updatedAt.replace('T', ' ').substring(0, 19)
  }
}

function onRuleChange() {
  // 触发响应式更新
}

function addRule() {
  rules.value.push(makeRule(''))
  // 自动滚动到底部
  setTimeout(() => {
    const cards = document.querySelectorAll('.rule-card')
    cards[cards.length - 1]?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }, 50)
}

function addAfterLast() {
  rules.value = [makeRule('')]
}

function removeRule(idx) {
  const rule = rules.value[idx]
  if (rule.text.trim()) {
    ElMessageBox.confirm('确定删除这条规则？', '提示', { type: 'warning' })
      .then(() => { rules.value.splice(idx, 1) })
      .catch(() => {})
  } else {
    rules.value.splice(idx, 1)
  }
}

function moveUp(idx) {
  if (idx === 0) return
  const item = rules.value.splice(idx, 1)[0]
  rules.value.splice(idx - 1, 0, item)
}

function moveDown(idx) {
  if (idx === rules.value.length - 1) return
  const item = rules.value.splice(idx, 1)[0]
  rules.value.splice(idx + 1, 0, item)
}

async function handleSave() {
  const validRules = nonEmptyRules.value
  console.log('[须知保存] 有效规则数:', validRules.length)
  if (validRules.length === 0) {
    ElMessage.warning('请至少填写一条规则')
    return
  }
  saving.value = true
  try {
    // 重新序列化为带编号的文本
    const content = validRules.map((r, i) => `${i + 1}、${r.text.trim()}`).join('\n')
    console.log('[须知保存] 发送内容长度:', content.length, '字符')
    const res = await updateGuide({ id: 1, title: form.value.title, content })
    console.log('[须知保存] 响应:', res)
    ElMessage.success('保存成功')
    lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
    // 重新加载以确保数据一致
    await load()
  } catch (e) {
    console.error('[须知保存] 失败:', e)
    ElMessage.error('保存失败：' + (e?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.guide-manage-page { max-width: 960px; margin: 0 auto; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
  padding: 0 4px;
}
.header-left { flex: 1; }
.page-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 4px;
  letter-spacing: -0.01em;
}
.page-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}
.header-actions { display: flex; gap: 10px; align-items: center; }
.saved-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-success);
  background: var(--brand-primary-light);
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 500;
}
.saved-tip .el-icon { font-size: 14px; }

.editor-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  padding: 28px 32px;
  box-shadow: 0 8px 32px var(--surface-primary);
}

.meta-section {
  padding-bottom: 20px;
  border-bottom: 1px dashed var(--border-light);
  margin-bottom: 20px;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 14px;
}
.meta-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  min-width: 72px;
  text-align: right;
}
.meta-input { flex: 1; max-width: 360px; }
.meta-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-tertiary);
}
.meta-stats strong { color: var(--brand-primary); font-weight: 700; margin: 0 2px; }
.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--brand-primary-light);
  border-radius: 8px;
}

.rules-section { display: flex; flex-direction: column; gap: 10px; }

.rule-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid var(--border-light);
  border-radius: 14px;
  transition: all 0.2s;
}
.rule-card:hover {
  background: #fff;
  border-color: var(--brand-primary);
  box-shadow: 0 4px 16px var(--shadow-primary-20);
}
.rule-index {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: var(--brand-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
}
.rule-input {
  flex: 1;
}
.rule-input :deep(.el-textarea__inner) {
  border: none;
  background: transparent;
  box-shadow: none;
  padding: 4px 0;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
}
.rule-input :deep(.el-textarea__inner:focus) { box-shadow: none; }

.rule-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
  min-width: 80px;
}
.char-count {
  font-size: 11px;
  color: var(--text-tertiary);
  font-family: 'SF Mono', 'Cascadia Code', monospace;
}
.char-count.warn { color: #e6a23c; }
.rule-actions { display: flex; gap: 4px; }
.icon-btn {
  width: 26px;
  height: 26px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all 0.15s;
}
.icon-btn:hover {
  background: var(--brand-primary-light);
  color: var(--brand-primary);
}
.icon-btn.danger:hover {
  background: #fef0f0;
  color: #f56c6c;
}
.icon-btn .el-icon { font-size: 14px; }

.add-rule-row {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 12px 0 0;
}
.add-btn {
  background: transparent;
  border: 2px dashed var(--border-light);
  color: var(--text-secondary);
  padding: 10px 24px;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  font-family: inherit;
}
.add-btn:hover {
  border-color: var(--brand-primary);
  color: var(--brand-primary);
  background: var(--brand-primary-light);
}

/* 预览弹窗 */
.preview-frame {
  background: linear-gradient(135deg, var(--bg-page) 0%, var(--brand-primary-light) 50%, var(--bg-page) 100%);
  border-radius: 14px;
  padding: 28px 32px;
  max-height: 60vh;
  overflow-y: auto;
}
.preview-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 20px;
  text-align: center;
}
.preview-list { display: flex; flex-direction: column; gap: 14px; }
.preview-line {
  display: flex;
  gap: 10px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary);
}
.preview-index { font-weight: 700; color: var(--brand-primary); flex-shrink: 0; }
.preview-text { white-space: pre-wrap; word-break: break-word; }
</style>
