<template>
  <el-dialog :model-value="visible" width="640px" :show-close="false" align-center custom-class="adopt-dialog" @update:model-value="$emit('update:visible', $event)">
    <template #header>
      <div class="adopt-header">
        <div class="adopt-header-left">
          <h3>申请领养</h3>
          <p>填写以下信息，我们会尽快与您联系</p>
        </div>
        <button class="adopt-close" @click="$emit('update:visible', false)">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
    </template>

    <div class="adopt-body">
      <div class="adopt-pet-card">
        <div class="adopt-pet-img">
          <slot name="pet-image">
            <div class="no-img">🐾</div>
          </slot>
        </div>
        <div class="adopt-pet-info">
          <div class="adopt-pet-name">{{ petName }}</div>
          <div class="adopt-pet-tag">待领养</div>
        </div>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="adopt-form">
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" clearable>
            <template #prefix><span class="input-icon">📱</span></template>
          </el-input>
        </el-form-item>
        <el-form-item label="联系地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入您的居住地址" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="领养理由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请说明您的领养理由、住房条件、养宠经验等" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="adopt-footer">
        <el-button class="adopt-cancel" @click="$emit('update:visible', false)">取消</el-button>
        <el-button class="adopt-submit" :loading="loading" @click="handleSubmit">提交申请</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, required: true },
  petId: { type: [Number, String], default: null },
  petName: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  initialForm: { type: Object, default: () => ({ phone: '', address: '', reason: '' }) }
})

const emit = defineEmits(['update:visible', 'submit'])

const formRef = ref(null)
const form = reactive({
  phone: '',
  address: '',
  reason: ''
})

// 弹窗打开时，用 initialForm 预填表单
watch(() => props.visible, (val) => {
  if (val) {
    form.phone = props.initialForm.phone || ''
    form.address = props.initialForm.address || ''
    form.reason = props.initialForm.reason || ''
  }
})

const rules = {
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确（11位手机号）', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入联系地址', trigger: 'blur' }],
  reason: [{ required: true, message: '请说明申请理由', trigger: 'blur' }]
}

function handleSubmit() {
  formRef.value.validate((valid) => {
    if (valid) {
      emit('submit', { ...form })
    }
  })
}

function resetForm() {
  form.phone = ''
  form.address = ''
  form.reason = ''
  formRef.value?.clearValidate()
}

defineExpose({ resetForm })
</script>

<style scoped>
.adopt-dialog {
  border-radius: 16px;
  overflow: hidden;
}
:deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}
:deep(.el-dialog__body) {
  padding: 0 24px;
}
:deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
}

.adopt-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--border-light);
}
.adopt-header h3 {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}
.adopt-header p {
  margin: 0;
  font-size: 13px;
  color: var(--text-tertiary);
}
.adopt-close {
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 4px;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
}
.adopt-close:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.adopt-body {
  display: flex;
  gap: 20px;
  padding: 20px 0;
}
.adopt-pet-card {
  flex-shrink: 0;
  width: 200px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}
.adopt-pet-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.adopt-pet-img img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.adopt-pet-img .no-img {
  font-size: 56px;
  opacity: 0.3;
}
.adopt-pet-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.adopt-pet-tag {
  display: inline-block;
  background: var(--surface-primary-strong);
  color: var(--brand-primary);
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
}

.adopt-form {
  flex: 1;
}
:deep(.el-form-item) {
  margin-bottom: 16px;
}
:deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  padding-bottom: 4px;
}
.input-icon {
  margin-left: 8px;
  font-size: 16px;
}

.adopt-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.adopt-cancel {
  border-radius: 8px;
  padding: 10px 22px;
}
.adopt-submit {
  background: var(--brand-primary);
  border: none;
  border-radius: 8px;
  padding: 10px 28px;
  font-weight: 600;
  color: #fff;
}
.adopt-submit:hover {
  background: var(--brand-primary-active);
}
.adopt-submit:disabled {
  opacity: 0.5;
}
</style>
