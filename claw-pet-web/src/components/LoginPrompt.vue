<template>
  <el-dialog
    :model-value="visible"
    width="400px"
    align-center
    :show-close="false"
    :modal-class="'login-prompt-modal'"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="login-prompt">
      <div class="prompt-icon" :class="`is-${variant}`">
        <el-icon :size="32"><Warning /></el-icon>
      </div>
      <h3 class="prompt-title">{{ title }}</h3>
      <p class="prompt-message">{{ message }}</p>
      <div class="prompt-actions">
        <button class="action-btn secondary" @click="handleCancel">{{ cancelText }}</button>
        <button class="action-btn primary" @click="handleConfirm">{{ confirmText }}</button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '请先登录' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '去登录' },
  cancelText: { type: String, default: '稍后' },
  variant: { type: String, default: 'warning' }  // warning | info
})
const emit = defineEmits(['update:visible', 'confirm', 'cancel'])

function handleConfirm() {
  emit('confirm')
  emit('update:visible', false)
}
function handleCancel() {
  emit('cancel')
  emit('update:visible', false)
}
</script>

<style>
.login-prompt-modal {
  /* 圆角放大 */
}
.login-prompt-modal .el-dialog {
  border-radius: 20px !important;
  background: rgba(255, 255, 255, 0.96) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12) !important;
  overflow: hidden;
}
.login-prompt-modal .el-dialog__body {
  padding: 0 !important;
}
.login-prompt-modal .el-dialog__header {
  display: none;
}
</style>

<style scoped>
.login-prompt {
  text-align: center;
  padding: 32px 28px 24px;
}
.prompt-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.prompt-icon.is-warning {
  background: linear-gradient(135deg, #fff5e6, #ffe1b3);
  color: #d97706;
}
.prompt-icon.is-info {
  background: linear-gradient(135deg, #e6f0ff, #cce0ff);
  color: #2563eb;
}
.prompt-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}
.prompt-message {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 24px;
  min-height: 22px;
}
.prompt-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}
.action-btn {
  flex: 1;
  height: 38px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.action-btn.secondary {
  background: var(--brand-primary-light, #f5f5f7);
  color: var(--text-primary);
}
.action-btn.secondary:hover {
  background: rgba(0, 0, 0, 0.06);
}
.action-btn.primary {
  background: var(--brand-primary);
  color: #fff;
  box-shadow: 0 2px 8px var(--shadow-primary-20);
}
.action-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--shadow-primary-30);
}
</style>
