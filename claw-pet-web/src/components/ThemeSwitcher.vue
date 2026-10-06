<template>
  <!-- 弹窗模式 -->
  <el-dialog v-if="mode === 'dialog'" v-model="visible" title="选择主题" width="440px" align-center>
    <div class="theme-grid">
      <div v-for="(t, key) in themes" :key="key" class="theme-card" :class="{ active: selectedKey === key, 'is-default': key === defaultKey }" @click="selectedKey = key">
        <div class="theme-preview">
          <div class="preview-bar" :style="{ background: t.colors['--brand-primary'] }"></div>
          <div class="preview-body" :style="{ background: t.colors['--brand-primary-light'] }">
            <div class="preview-tag" :style="{ background: t.colors['--brand-primary'] }"></div>
            <div class="preview-line" :style="{ background: t.colors['--text-secondary'] }"></div>
          </div>
        </div>
        <div class="theme-name">{{ t.name }}</div>
        <span v-if="key === defaultKey" class="default-badge">默认</span>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <button class="ghost-btn" @click="close">取消</button>
        <button class="primary-btn" @click="confirm">应用主题</button>
      </div>
    </template>
  </el-dialog>

  <!-- 面板模式 -->
  <Teleport to="body" v-if="mode === 'panel'">
    <div v-if="visible" class="panel-overlay" @click.self="close"></div>
    <div v-if="visible" class="theme-panel">
      <div class="panel-header">
        <span class="panel-title">选择主题</span>
        <button class="panel-close" @click="close" aria-label="关闭">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <div class="panel-grid">
        <div v-for="(t, key) in themes" :key="key" class="theme-item" :class="{ active: selectedKey === key }" @click="selectedKey = key">
          <div class="item-preview">
            <div class="item-bar" :style="{ background: t.colors['--brand-primary'] }"></div>
            <div class="item-body" :style="{ background: t.colors['--brand-primary-light'] }">
              <div class="item-tag" :style="{ background: t.colors['--brand-primary'] }"></div>
            </div>
          </div>
          <div class="item-name">{{ t.name }}</div>
          <span v-if="key === defaultKey" class="item-badge">默认</span>
        </div>
      </div>
      <div class="panel-actions">
        <button class="ghost-btn" @click="close">取消</button>
        <button class="primary-btn" @click="confirm">应用主题</button>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import themes, { applyTheme, getSavedTheme } from '@/utils/themes'

const props = defineProps({
  mode: { type: String, default: 'dialog' }
})

const visible = ref(false)
const selectedKey = ref(getSavedTheme())
const defaultKey = 'white'

watch(visible, (v) => {
  if (v) selectedKey.value = getSavedTheme()
})

function confirm() {
  applyTheme(selectedKey.value)
  close()
}

function show() {
  visible.value = true
}

function close() {
  visible.value = false
}

defineExpose({ show })
</script>

<style scoped>
/* ===== 弹窗模式 ===== */
.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 8px 0 4px;
}
.theme-card {
  position: relative;
  border: 2px solid var(--border-light);
  border-radius: 14px;
  padding: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
  text-align: center;
  background: #fff;
}
.theme-card:hover {
  border-color: var(--text-secondary);
  transform: translateY(-2px);
}
.theme-card.active {
  border-color: var(--brand-primary);
  border-width: 2.5px;
  box-shadow: 0 4px 12px var(--shadow-primary-20);
  background: var(--brand-primary-light);
}
.theme-preview {
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
  border: 1px solid rgba(0,0,0,0.04);
}
.preview-bar { height: 14px; }
.preview-body {
  height: 46px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
}
.preview-tag {
  width: 16px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
}
.preview-line {
  height: 4px;
  border-radius: 2px;
  flex: 1;
  opacity: 0.25;
}
.theme-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}
.default-badge {
  position: absolute;
  top: -6px; left: -6px;
  background: var(--brand-primary);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 8px 0 8px 0;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 6px var(--shadow-primary-30);
  pointer-events: none;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.primary-btn {
  background: var(--brand-primary);
  color: #fff;
  border: none;
  padding: 8px 22px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s ease;
}
.primary-btn:hover {
  background: var(--brand-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--shadow-primary-30);
}
.ghost-btn {
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
  padding: 8px 18px;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s ease;
}
.ghost-btn:hover {
  color: var(--brand-primary);
  border-color: var(--brand-primary);
}

/* ===== 面板模式 ===== */
.panel-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.08);
  z-index: 998;
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.theme-panel {
  position: fixed;
  top: 60px;
  right: 16px;
  width: 320px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 16px;
  box-shadow: -4px 8px 32px rgba(0, 0, 0, 0.08);
  z-index: 999;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
  max-height: calc(100vh - 80px);
}
@keyframes slideIn {
  from { transform: translateX(120%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 8px;
}
.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}
.panel-close {
  width: 32px; height: 32px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.panel-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: var(--text-primary);
}
.panel-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  padding: 4px 16px 12px;
}
.theme-item {
  position: relative;
  border: 1.5px solid var(--border-light);
  border-radius: 10px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 8px;
}
.theme-item:hover {
  border-color: var(--text-secondary);
  transform: translateY(-1px);
}
.theme-item.active {
  border-color: var(--brand-primary);
  box-shadow: 0 3px 10px var(--shadow-primary-20);
  background: var(--brand-primary-light);
}
.item-preview {
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid rgba(0,0,0,0.04);
  flex-shrink: 0;
  width: 36px;
}
.item-bar { height: 8px; }
.item-body {
  height: 22px;
  display: flex;
  align-items: center;
  padding: 0 5px;
  gap: 4px;
}
.item-tag {
  width: 9px;
  height: 7px;
  border-radius: 2px;
  flex-shrink: 0;
}
.item-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
}
.item-badge {
  position: absolute;
  top: -4px; right: -4px;
  background: var(--brand-primary);
  color: #fff;
  font-size: 9px;
  font-weight: 600;
  padding: 1px 5px;
  border-radius: 0 6px;
  pointer-events: none;
}
.panel-actions {
  display: flex;
  gap: 8px;
  padding: 10px 16px 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}
.panel-actions .primary-btn,
.panel-actions .ghost-btn {
  flex: 1;
  text-align: center;
  font-size: 13px;
  padding: 8px 0;
}
</style>
