<template>
  <div class="floating-contact">
    <!-- 悬浮按钮 -->
    <button
      class="floating-contact__btn"
      :class="{ 'is-active': isPanelOpen }"
      @click="togglePanel"
      aria-label="联系我们"
      type="button"
    >
      <svg v-if="!isPanelOpen" class="floating-contact__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
      <svg v-else class="floating-contact__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="18" y1="6" x2="6" y2="18"/>
        <line x1="6" y1="6" x2="18" y2="18"/>
      </svg>
      <span class="floating-contact__label">联系我们</span>
    </button>

    <!-- 展开面板 -->
    <transition name="panel-slide">
      <div v-if="isPanelOpen" class="floating-contact__panel glass-card">
        <div class="panel__header">
          <h3 class="panel__title">联系我们</h3>
          <p class="panel__subtitle">我们随时为您服务</p>
        </div>

        <div class="panel__content">
          <!-- 联系方式 -->
          <div class="contact-methods">
            <div class="contact-item">
              <div class="contact-item__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
                </svg>
              </div>
              <div class="contact-item__content">
                <span class="contact-item__label">电话咨询</span>
                <a :href="`tel:${contactInfo.phone}`" class="contact-item__value">{{ contactInfo.phoneDisplay }}</a>
              </div>
            </div>

            <div class="contact-item">
              <div class="contact-item__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                  <polyline points="22,6 12,13 2,6"/>
                </svg>
              </div>
              <div class="contact-item__content">
                <span class="contact-item__label">邮箱联系</span>
                <a :href="`mailto:${contactInfo.email}`" class="contact-item__value">{{ contactInfo.email }}</a>
              </div>
            </div>

            <div class="contact-item contact-item--wechat">
              <div class="contact-item__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
              <div class="contact-item__content">
                <span class="contact-item__label">微信客服</span>
                <span class="contact-item__value">{{ contactInfo.wechat }}</span>
              </div>
            </div>
          </div>

          <!-- 微信二维码 -->
          <div class="qrcode-section">
            <div class="qrcode-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7"/>
                <rect x="14" y="3" width="7" height="7"/>
                <rect x="14" y="14" width="7" height="7"/>
                <rect x="3" y="14" width="7" height="7"/>
              </svg>
              <span>微信二维码</span>
            </div>
            <p class="qrcode-tip">扫码添加客服微信</p>
          </div>

          <!-- 快速留言表单 -->
          <div class="quick-form">
            <h4 class="quick-form__title">快速留言</h4>
            <el-form :model="formData" size="default">
              <el-form-item>
                <el-input v-model="formData.name" placeholder="您的姓名" />
              </el-form-item>
              <el-form-item>
                <el-input v-model="formData.contact" placeholder="联系方式（手机/微信）" />
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="formData.message"
                  type="textarea"
                  :rows="3"
                  placeholder="请简要描述您的需求..."
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="submitting">
                  提交留言
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { contactInfo } from '@/config/contact'
import request from '@/utils/request'

const isPanelOpen = ref(false)
const submitting = ref(false)

const formData = reactive({
  name: '',
  contact: '',
  message: ''
})

const togglePanel = () => {
  isPanelOpen.value = !isPanelOpen.value
}

const handleSubmit = async () => {
  if (!formData.name || !formData.contact || !formData.message) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    await request.post('/contact/submit', {
      name: formData.name,
      contact: formData.contact,
      message: formData.message
    })
    ElMessage.success('留言已提交，我们会尽快联系您！')
    formData.name = ''
    formData.contact = ''
    formData.message = ''
    isPanelOpen.value = false
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.floating-contact {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 100;
}

/* 悬浮按钮 */
.floating-contact__btn {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: var(--site-gradient);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.4);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s ease;
  animation: floatingPulse 2.5s ease-in-out infinite;
}

.floating-contact__btn::before {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  background: var(--site-gradient);
  opacity: 0.3;
  animation: pulseRing 2.5s ease-in-out infinite;
}

@keyframes floatingPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes pulseRing {
  0% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.15);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

.floating-contact__btn:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.5);
}

.floating-contact__btn:hover .floating-contact__label {
  opacity: 1;
  transform: translateX(-8px);
}

.floating-contact__btn.is-active {
  background: var(--site-bg-elevated);
  border: 1px solid var(--site-border-strong);
  color: var(--site-text);
  animation: none;
}

.floating-contact__btn.is-active::before {
  display: none;
}

.floating-contact__icon {
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.floating-contact__label {
  position: absolute;
  right: 100%;
  margin-right: 12px;
  padding: 8px 14px;
  background: var(--site-bg-elevated);
  border: 1px solid var(--site-border);
  border-radius: 12px;
  color: var(--site-text);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  opacity: 0;
  transform: translateX(8px);
  transition: opacity 0.3s ease, transform 0.3s ease;
  pointer-events: none;
  box-shadow: var(--site-shadow);
}

/* 展开面板 */
.floating-contact__panel {
  position: absolute;
  bottom: 72px;
  right: 0;
  width: 360px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  padding: 24px;
  border: 1px solid var(--site-border-strong);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.panel__header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--site-border);
}

.panel__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 4px;
}

.panel__subtitle {
  font-size: 13px;
  color: var(--site-text-secondary);
  margin: 0;
}

.panel__content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 联系方式 */
.contact-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: 12px;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.contact-item:hover {
  background: var(--site-surface-hover);
  border-color: var(--site-border-strong);
}

.contact-item__icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--site-accent-muted);
  color: var(--site-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.contact-item__icon svg {
  width: 18px;
  height: 18px;
}

.contact-item__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.contact-item__label {
  font-size: 12px;
  color: var(--site-text-muted);
}

.contact-item__value {
  font-size: 14px;
  font-weight: 600;
  color: var(--site-text);
  text-decoration: none;
  transition: color 0.2s ease;
}

.contact-item__value:hover {
  color: var(--site-accent);
}

/* 微信二维码 */
.qrcode-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: 12px;
}

.qrcode-placeholder {
  width: 140px;
  height: 140px;
  border-radius: 12px;
  background: var(--site-bg-elevated);
  border: 2px dashed var(--site-border-strong);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--site-text-muted);
}

.qrcode-placeholder svg {
  width: 48px;
  height: 48px;
}

.qrcode-placeholder span {
  font-size: 12px;
}

.qrcode-tip {
  font-size: 12px;
  color: var(--site-text-secondary);
  margin: 0;
}

/* 快速留言表单 */
.quick-form {
  padding-top: 16px;
  border-top: 1px solid var(--site-border);
}

.quick-form__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--site-text);
  margin: 0 0 12px;
}

.quick-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.quick-form :deep(.el-input__wrapper) {
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  box-shadow: none;
}

.quick-form :deep(.el-input__wrapper:hover) {
  border-color: var(--site-border-strong);
}

.quick-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--site-accent);
}

.quick-form :deep(.el-textarea__inner) {
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  color: var(--site-text);
}

.submit-btn {
  width: 100%;
  background: var(--site-gradient);
  border: none;
  font-weight: 600;
}

/* 面板动画 */
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.panel-slide-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.95);
}

.panel-slide-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.98);
}

/* 移动端适配 */
@media (max-width: 640px) {
  .floating-contact {
    bottom: 20px;
    right: 20px;
  }

  .floating-contact__btn {
    width: 48px;
    height: 48px;
  }

  .floating-contact__icon {
    width: 20px;
    height: 20px;
  }

  .floating-contact__label {
    display: none;
  }

  .floating-contact__panel {
    width: calc(100vw - 40px);
    right: -20px;
    bottom: 64px;
  }
}

/* 无障碍支持 */
@media (prefers-reduced-motion: reduce) {
  .floating-contact__btn,
  .floating-contact__btn::before {
    animation: none !important;
  }

  .panel-slide-enter-active,
  .panel-slide-leave-active {
    transition-duration: 0.01ms !important;
  }
}
</style>
