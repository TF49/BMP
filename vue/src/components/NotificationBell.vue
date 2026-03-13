<template>
  <div class="notification-bell">
    <div
      class="bell-trigger"
      role="button"
      tabindex="0"
      aria-label="系统通知"
      @click="$emit('open-popup')"
      @keydown.enter.prevent="$emit('open-popup')"
      @keydown.space.prevent="$emit('open-popup')"
    >
      <el-icon :size="20" class="bell-icon">
        <Bell />
      </el-icon>
    </div>

    <!-- 发布通知弹窗：与通知弹窗风格统一 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布通知"
      width="480px"
      align-center
      :show-close="true"
      :close-on-click-modal="false"
      class="publish-notification-dialog"
      overlay-class-name="publish-notification-overlay"
      @closed="resetPublishForm"
    >
      <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="80px" class="publish-form">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="publishForm.title"
            placeholder="请输入通知标题"
            maxlength="200"
            show-word-limit
            clearable
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入通知内容"
            resize="vertical"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="publish-dialog-footer">
          <button type="button" class="publish-btn publish-btn-cancel" @click="publishDialogVisible = false">
            取消
          </button>
          <el-button type="primary" :loading="publishLoading" class="publish-btn-submit" @click="submitPublish">
            发布
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { createNotification } from '@/api/notification'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

defineEmits<{ 'open-popup': [] }>()

const publishDialogVisible = ref(false)
const publishLoading = ref(false)
const publishFormRef = ref<FormInstance>()

const publishForm = ref({
  title: '',
  content: ''
})

const publishRules: FormRules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

const resetPublishForm = () => {
  publishForm.value = { title: '', content: '' }
  publishFormRef.value?.clearValidate()
}

const submitPublish = async () => {
  if (!publishFormRef.value) return
  await publishFormRef.value.validate(async (valid) => {
    if (!valid) return
    publishLoading.value = true
    try {
      await createNotification({
        title: publishForm.value.title.trim(),
        content: publishForm.value.content.trim()
      })
      ElMessage.success('发布成功')
      publishDialogVisible.value = false
    } catch (e: any) {
      ElMessage.error(e?.response?.data?.message || e?.message || '发布失败')
    } finally {
      publishLoading.value = false
    }
  })
}

const openPublishDialog = () => {
  publishDialogVisible.value = true
}

defineExpose({
  openPublishDialog
})
</script>

<style scoped>
/* 铃铛触发按钮：Galaxy 风格 + 焦点环（a11y） */
.notification-bell {
  position: relative;
}

.bell-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--color-background, #f8fafc);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--color-text-secondary, #64748b);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.bell-trigger:hover {
  background: #fff;
  border-color: #f97316;
  color: #f97316;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.2);
  transform: translateY(-1px);
}

.bell-trigger:active {
  transform: translateY(0);
  box-shadow: 0 1px 4px rgba(249, 115, 22, 0.15);
}

.bell-trigger:focus-visible {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.25);
}

.bell-icon {
  display: block;
}
</style>

<style>
/* 发布弹窗：与通知弹窗统一的圆角、阴影、按钮风格 */
.publish-notification-dialog .el-dialog {
  border-radius: 16px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.12), 0 8px 24px rgba(0, 0, 0, 0.08);
}

.publish-notification-dialog .el-dialog__header {
  padding: 18px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  margin-right: 0;
}

.publish-notification-dialog .el-dialog__title {
  font-size: 17px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
}

.publish-notification-dialog .el-dialog__body {
  padding: 20px 24px;
}

.publish-notification-dialog .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.publish-form .el-form-item__label {
  font-weight: 500;
  color: var(--el-text-color-regular, #606266);
}

.publish-form .el-input__wrapper,
.publish-form .el-textarea__inner {
  border-radius: 10px;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.publish-form .el-input__wrapper:hover,
.publish-form .el-textarea__inner:hover {
  box-shadow: 0 0 0 1px var(--el-border-color-darker, #c0c4cc);
}

.publish-form .el-input__wrapper.is-focus,
.publish-form .el-textarea__inner:focus {
  box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.35);
}

.publish-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.publish-btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.publish-btn:active {
  transform: scale(0.98);
}

.publish-btn-cancel {
  background: #fff;
  border: 1px solid var(--el-border-color, #dcdfe6);
  color: var(--el-text-color-regular, #606266);
}

.publish-btn-cancel:hover {
  background: var(--el-fill-color-light, #f5f7fa);
  border-color: var(--el-border-color-darker, #c0c4cc);
  color: var(--el-text-color-primary, #303133);
}

.publish-notification-dialog .publish-btn-submit {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%) !important;
  border: none !important;
  box-shadow: 0 4px 14px rgba(249, 115, 22, 0.4);
}

.publish-notification-dialog .publish-btn-submit:hover {
  background: linear-gradient(135deg, #ea580c 0%, #c2410c 100%) !important;
  box-shadow: 0 6px 20px rgba(249, 115, 22, 0.45);
  transform: translateY(-1px);
}

.publish-notification-overlay {
  backdrop-filter: blur(4px);
}
</style>
