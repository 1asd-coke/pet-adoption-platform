<template>
  <div class="guide-manage-page">
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">领养须知编辑</h1>
        <p class="page-subtitle">全部章节同属一篇须知，逐章编辑、自动编号，所见即所得</p>
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

    <!-- 章节切换 -->
    <div class="section-tabs" v-loading="loading">
      <button
        v-for="(sec, idx) in sections"
        :key="sec.key"
        class="tab-item"
        :class="{ active: idx === currentIndex }"
        @click="currentIndex = idx"
      >
        <span class="tab-index">{{ idx + 1 }}</span>
        <span class="tab-title">{{ sec.title || '未命名章节' }}</span>
        <span class="tab-count">{{ filledCount(sec) }}</span>
      </button>
      <button class="tab-item tab-add" @click="addSection">
        <el-icon><Plus /></el-icon>
        <span>新建章节</span>
      </button>
    </div>

    <div class="editor-card" v-if="current">
      <div class="meta-section">
        <div class="meta-row">
          <span class="meta-label">章节标题</span>
          <el-input v-model="current.title" placeholder="领养须知" class="meta-input" />
          <el-button
            v-if="sections.length > 1"
            type="danger"
            plain
            @click="removeSection(currentIndex)"
          >
            <el-icon><Delete /></el-icon>删除本章节
          </el-button>
        </div>
        <div class="meta-stats">
          <span class="stat-item">
            <el-icon><DocumentChecked /></el-icon>
            共 <strong>{{ filledCount(current) }}</strong> 条规则
          </span>
          <span class="stat-item">
            <el-icon><EditPen /></el-icon>
            总字数 <strong>{{ totalChars }}</strong>
          </span>
        </div>
      </div>

      <div class="rules-section">
        <div
          v-for="(rule, idx) in current.rules"
          :key="rule.key"
          class="rule-card"
        >
          <div class="rule-index">{{ idx + 1 }}</div>
          <el-input
            v-model="rule.text"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 6 }"
            :placeholder="`请输入第 ${idx + 1} 条规则`"
            class="rule-input"
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
                v-if="idx < current.rules.length - 1"
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
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <el-dialog v-model="showPreview" title="前台展示预览" width="640px" align-center>
      <div class="preview-frame">
        <template v-for="sec in previewSections" :key="sec.key">
          <div class="preview-section">
            <h2 class="preview-title">{{ sec.title || '领养须知' }}</h2>
            <div class="preview-list">
              <div v-for="(rule, idx) in sec.rules" :key="rule.key" class="preview-line">
                <span class="preview-index">{{ idx + 1 }}.</span>
                <span class="preview-text">{{ rule.text }}</span>
              </div>
            </div>
          </div>
        </template>
        <p v-if="previewSections.length === 0" class="preview-empty">暂无可预览内容</p>
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
import { getGuide, updateGuide, deleteGuide } from '@/api/guide'

const sections = ref([])
const currentIndex = ref(0)
const loading = ref(false)
const saving = ref(false)
const showPreview = ref(false)
const lastSavedAt = ref(null)  // 最近保存时间

let uid = 1

const current = computed(() => sections.value[currentIndex.value] || null)

const totalChars = computed(() =>
  (current.value?.rules || []).reduce((sum, r) => sum + r.text.length, 0)
)

/** 预览：去掉空规则后的全部章节 */
const previewSections = computed(() =>
  sections.value
    .map(sec => ({ ...sec, rules: sec.rules.filter(r => r.text.trim()) }))
    .filter(sec => sec.rules.length > 0)
)

function makeRule(text) {
  return { key: uid++, text: text || '' }
}

/** 去掉行首已有的编号（"1、" / "一、" / "1." …） */
function stripNumber(line) {
  return line.replace(/^(?:\d+|[一二三四五六七八九十]+)\s*[、,，.．:：)）]\s*/, '').trim()
}

/** 把 content 文本拆成规则数组 */
function parseContent(content) {
  const lines = (content || '').split('\n').map(s => s.trim()).filter(Boolean)
  return lines.length ? lines.map(line => makeRule(stripNumber(line))) : [makeRule('')]
}

function makeSection(row) {
  return {
    key: uid++,
    id: row?.id ?? null,
    title: row?.title || '领养须知',
    rules: parseContent(row?.content)
  }
}

function filledCount(sec) {
  return sec.rules.filter(r => r.text.trim()).length
}

async function load() {
  loading.value = true
  try {
    const res = await getGuide()
    const data = res.data
    // 后端返回的是数组（每行一个章节），兼容单对象返回
    const rows = Array.isArray(data) ? data : (data ? [data] : [])
    sections.value = rows.length ? rows.map(makeSection) : [makeSection(null)]
    currentIndex.value = 0
    const latest = rows.map(r => r.updatedAt).filter(Boolean).sort().pop()
    if (latest) lastSavedAt.value = String(latest).replace('T', ' ').substring(0, 19)
  } catch (e) {
    console.error('[须知加载] 失败:', e)
    sections.value = [makeSection(null)]
    currentIndex.value = 0
  } finally {
    loading.value = false
  }
}

/* ---------- 章节操作 ---------- */
function addSection() {
  sections.value.push({ key: uid++, id: null, title: '', rules: [makeRule('')] })
  currentIndex.value = sections.value.length - 1
}

async function removeSection(idx) {
  const sec = sections.value[idx]
  if (!sec) return
  try {
    await ElMessageBox.confirm(
      `确定删除章节「${sec.title || '未命名章节'}」？前台对应内容会一并移除。`,
      '提示',
      { type: 'warning' }
    )
  } catch {
    return
  }
  if (sec.id != null) {
    try {
      await deleteGuide(sec.id)
    } catch (e) {
      ElMessage.error('删除失败：' + (e?.message || '未知错误'))
      return
    }
  }
  sections.value.splice(idx, 1)
  if (currentIndex.value >= sections.value.length) {
    currentIndex.value = sections.value.length - 1
  }
  ElMessage.success('已删除该章节')
}

/* ---------- 规则操作 ---------- */
function addRule() {
  current.value.rules.push(makeRule(''))
  setTimeout(() => {
    const cards = document.querySelectorAll('.rule-card')
    cards[cards.length - 1]?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }, 50)
}

function removeRule(idx) {
  const rule = current.value.rules[idx]
  if (rule.text.trim()) {
    ElMessageBox.confirm('确定删除这条规则？', '提示', { type: 'warning' })
      .then(() => { current.value.rules.splice(idx, 1) })
      .catch(() => {})
  } else {
    current.value.rules.splice(idx, 1)
  }
}

function moveUp(idx) {
  const rules = current.value.rules
  if (idx === 0) return
  const item = rules.splice(idx, 1)[0]
  rules.splice(idx - 1, 0, item)
}

function moveDown(idx) {
  const rules = current.value.rules
  if (idx === rules.length - 1) return
  const item = rules.splice(idx, 1)[0]
  rules.splice(idx + 1, 0, item)
}

/* ---------- 保存 ---------- */
async function handleSave() {
  const emptySection = sections.value.find(sec => filledCount(sec) === 0)
  if (emptySection) {
    ElMessage.warning(`章节「${emptySection.title || '未命名章节'}」还没有内容，请填写或删除它`)
    return
  }
  saving.value = true
  const prevId = current.value?.id
  const prevIndex = currentIndex.value
  try {
    // 逐一保存每个章节：id 为空走新增，否则更新
    for (const sec of sections.value) {
      const content = sec.rules
        .filter(r => r.text.trim())
        .map((r, i) => `${i + 1}、${r.text.trim()}`)
        .join('\n')
      await updateGuide({ id: sec.id ?? null, title: sec.title?.trim() || '领养须知', content })
    }
    ElMessage.success('保存成功')
    lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
    await load()
    // 尽量停留在刚才编辑的章节
    if (prevId != null) {
      const i = sections.value.findIndex(s => s.id === prevId)
      currentIndex.value = i >= 0 ? i : 0
    } else {
      currentIndex.value = Math.min(prevIndex, sections.value.length - 1)
    }
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

/* 章节切换 */
.section-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
  padding: 0 4px;
  min-height: 38px;
}
.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border: 1px solid var(--border-light);
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.18s;
}
.tab-item:hover {
  border-color: var(--brand-primary);
  color: var(--brand-primary);
}
.tab-item.active {
  background: var(--brand-primary);
  border-color: var(--brand-primary);
  color: #fff;
  box-shadow: 0 4px 14px var(--shadow-primary-20);
}
.tab-index {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--brand-primary-light);
  color: var(--brand-primary);
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.tab-item.active .tab-index { background: rgba(255, 255, 255, 0.25); color: #fff; }
.tab-title {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tab-count {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 8px;
  background: var(--brand-primary-light);
  color: var(--brand-primary);
  font-family: 'SF Mono', 'Cascadia Code', monospace;
}
.tab-item.active .tab-count { background: rgba(255, 255, 255, 0.25); color: #fff; }
.tab-add { border-style: dashed; background: transparent; }

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
.rule-input { flex: 1; }
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
.preview-section + .preview-section {
  margin-top: 22px;
  padding-top: 20px;
  border-top: 1px dashed var(--border-light);
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
.preview-empty {
  text-align: center;
  font-size: 13px;
  color: var(--text-tertiary);
  margin: 0;
}
</style>
