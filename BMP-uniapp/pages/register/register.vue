<template>
  <view class="auth-page">
    <!-- 顶部渐变头部，风格与首页一致 -->
    <view class="auth-header">
      <view class="auth-header-content">
        <view class="logo-circle">
          <uni-icons type="medal" size="38" color="#ffffff" class="logo-icon"></uni-icons>
        </view>
        <view class="header-texts">
          <text class="app-name">羽毛球管理系统</text>
          <text class="app-desc">注册新账号，开始预约与报名</text>
        </view>
      </view>
    </view>

    <view class="auth-content">
      <view class="auth-card">
        <!-- 头部 -->
        <view class="card-title-row">
          <text class="card-title">注册</text>
          <text class="card-subtitle">创建您的账户</text>
        </view>

        <!-- 步骤指示器 -->
        <view class="step-indicator">
          <view class="step-dot" :class="currentStep === 1 ? 'active' : 'done'">
            <text v-if="currentStep > 1" class="step-check">✓</text>
            <text v-else class="step-num">1</text>
          </view>
          <view class="step-line"></view>
          <view class="step-dot" :class="currentStep === 2 ? 'active' : 'inactive'">
            <text class="step-num">2</text>
          </view>
        </view>
        <view class="step-labels">
          <text class="step-label" :class="currentStep >= 1 ? 'active' : ''">账号信息</text>
          <text class="step-label" :class="currentStep >= 2 ? 'active' : ''">身份信息</text>
        </view>

        <!-- 表单 -->
        <view class="form-body">
          <!-- Step 1 -->
          <view class="step-panel" v-show="currentStep === 1">
            <AnimatedInput
              v-model="form.username"
              type="text"
              placeholder="请输入用户名（4-20个字符）"
              icon-type="person"
              :error-message="errors.username"
              :hint="usernameHint"
              @focus="clearError('username')"
              @input="validateUsername"
            />

            <AnimatedInput
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              icon-type="locked"
              :error-message="errors.password"
              @focus="clearError('password')"
              @input="validatePassword"
            />

            <AnimatedInput
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认密码"
              icon-type="locked"
              :error-message="errors.confirmPassword"
              @focus="clearError('confirmPassword')"
              @input="validateConfirmPassword"
            />

            <!-- 密码强度指示器（4段） -->
            <view v-if="form.password" class="password-strength">
              <view class="strength-header">
                <text class="strength-icon">{{ strengthIcon }}</text>
                <text class="strength-label" :class="`level-${passwordStrength}`">{{ passwordStrengthText }}</text>
              </view>
              <view class="strength-bar">
                <view
                  v-for="(item, index) in strengthBars"
                  :key="index"
                  class="bar"
                  :class="item.class"
                ></view>
              </view>
              <view class="strength-tags">
                <text class="tag" :class="passwordStrength >= 0 ? `tag-${passwordStrength}` : ''">弱</text>
                <text class="tag" :class="passwordStrength >= 1 ? `tag-${passwordStrength}` : ''">中</text>
                <text class="tag" :class="passwordStrength >= 2 ? `tag-${passwordStrength}` : ''">强</text>
                <text class="tag" :class="passwordStrength >= 3 ? `tag-${passwordStrength}` : ''">极强</text>
              </view>
            </view>

            <view class="step-next-btn" @click="goToStep2">
              <text class="step-btn-text">下一步</text>
              <uni-icons type="right" size="18" color="#ffffff"></uni-icons>
            </view>
          </view>

          <!-- Step 2 -->
          <view class="step-panel" v-show="currentStep === 2">
            <AnimatedInput
              v-model="form.idCard"
              type="text"
              placeholder="请输入身份证号"
              icon-type="contact"
              :error-message="errors.idCard"
              :maxlength="18"
              @focus="clearError('idCard')"
              @input="validateIdCard"
            />

            <AnimatedButton
              :text="loading ? '注册中...' : '完成注册'"
              type="primary"
              size="large"
              :loading="loading"
              :disabled="!canSubmit"
              @click="handleRegister"
            />

            <view class="step-back-btn" @click="goToStep1">
              <uni-icons type="left" size="16" color="#94a3b8"></uni-icons>
              <text class="back-btn-text">返回上一步</text>
            </view>
          </view>
        </view>

        <!-- 底部 -->
        <view class="form-footer">
          <view class="footer-divider"></view>
          <view class="footer-login">
            <text class="footer-text">已有账号？</text>
            <text class="footer-link" @click="navigateToLogin">立即登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { register } from '@/api/auth'
import { validateIdCard as validateIdCardRule } from '@/utils/validate'
import { safeNavigateBack } from '@/utils/navigation'
import AnimatedInput from '@/components/common/AnimatedInput.vue'
import AnimatedButton from '@/components/common/AnimatedButton.vue'

const currentStep = ref(1)

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  idCard: '',
  role: 'USER'
})

const errors = ref({
  username: '',
  password: '',
  confirmPassword: '',
  idCard: ''
})

const loading = ref(false)
const passwordStrength = ref(0) // 0-弱, 1-中, 2-强, 3-极强

const usernameHint = computed(() => {
  if (!form.value.username) return ''
  const len = form.value.username.length
  if (len < 4) return `还需${4 - len}个字符`
  if (len > 20) return '用户名过长'
  return '✓'
})

const passwordStrengthText = computed(() => {
  const texts = ['弱', '中', '强', '极强']
  return texts[passwordStrength.value]
})

const strengthIcon = computed(() => {
  return passwordStrength.value >= 2 ? '🔒' : '🔓'
})

const strengthBars = computed(() => {
  const bars = []
  for (let i = 0; i < 4; i++) {
    bars.push({
      class: i <= passwordStrength.value ? `active level-${passwordStrength.value}` : ''
    })
  }
  return bars
})

const canSubmit = computed(() => {
  return (
    form.value.idCard.trim() &&
    !errors.value.idCard &&
    !loading.value
  )
})

const clearError = (field: keyof typeof errors.value) => {
  errors.value[field] = ''
}

const validateUsername = () => {
  const username = form.value.username.trim()
  if (!username) { errors.value.username = ''; return }
  if (username.length < 4) {
    errors.value.username = '用户名至少4个字符'
  } else if (username.length > 20) {
    errors.value.username = '用户名不能超过20个字符'
  } else if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    errors.value.username = '用户名只能包含字母、数字和下划线'
  } else {
    errors.value.username = ''
  }
}

const validatePassword = () => {
  const password = form.value.password
  if (!password) { passwordStrength.value = 0; errors.value.password = ''; return }
  if (password.length < 6) {
    errors.value.password = '密码至少6位'
    passwordStrength.value = 0
    return
  }
  errors.value.password = ''
  let score = 0
  if (password.length >= 8) score++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++
  if (/\d/.test(password)) score++
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score++
  passwordStrength.value = Math.min(3, score)
  if (form.value.confirmPassword) validateConfirmPassword()
}

const validateConfirmPassword = () => {
  if (!form.value.confirmPassword) { errors.value.confirmPassword = ''; return }
  errors.value.confirmPassword = form.value.password !== form.value.confirmPassword
    ? '两次输入的密码不一致' : ''
}

const validateIdCard = () => {
  const idCard = form.value.idCard.trim()
  if (!idCard) { errors.value.idCard = ''; return }
  errors.value.idCard = !validateIdCardRule(idCard) ? '请输入正确的身份证号' : ''
}

const validateStep1 = (): boolean => {
  validateUsername()
  validatePassword()
  validateConfirmPassword()
  return !errors.value.username && !errors.value.password && !errors.value.confirmPassword
    && !!form.value.username.trim() && !!form.value.password && !!form.value.confirmPassword
}

const goToStep2 = () => {
  if (!validateStep1()) {
    const firstError = errors.value.username || errors.value.password || errors.value.confirmPassword
      || (!form.value.username.trim() ? '请输入用户名' : '')
      || (!form.value.password ? '请输入密码' : '')
      || (!form.value.confirmPassword ? '请确认密码' : '')
    uni.showToast({ title: firstError || '请完善账号信息', icon: 'none', duration: 2000 })
    return
  }
  currentStep.value = 2
}

const goToStep1 = () => {
  currentStep.value = 1
}

const handleRegister = async () => {
  validateIdCard()
  if (errors.value.idCard || !form.value.idCard.trim()) {
    uni.showToast({ title: errors.value.idCard || '请输入身份证号', icon: 'none', duration: 2000 })
    return
  }

  loading.value = true
  try {
    await register({
      username: form.value.username.trim(),
      password: form.value.password,
      confirmPassword: form.value.confirmPassword,
      idCard: form.value.idCard.trim(),
      role: form.value.role
    })
    uni.showToast({ title: '注册成功', icon: 'success', duration: 1500 })
    setTimeout(() => { safeNavigateBack() }, 1500)
  } catch (error: any) {
    const msg = error.message || '注册失败，请稍后重试'
    if (msg.includes('用户名') || msg.includes('已存在')) {
      errors.value.username = msg
      currentStep.value = 1
    } else if (msg.includes('密码')) {
      errors.value.password = msg
      currentStep.value = 1
    } else if (msg.includes('身份证')) {
      errors.value.idCard = msg
    } else {
      uni.showToast({ title: msg, icon: 'none', duration: 2000 })
    }
  } finally {
    loading.value = false
  }
}

const navigateToLogin = () => {
  safeNavigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.auth-page {
  min-height: 100vh;
  background-color: $bg-color;
  display: flex;
  flex-direction: column;
}

.auth-header {
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  padding: 80rpx 40rpx 60rpx;
  border-radius: 0 0 40rpx 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(60, 197, 31, 0.3);
}

.auth-header-content {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.logo-circle {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.18);
  border: 2rpx solid rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.logo-icon {
  flex-shrink: 0;
}

.header-texts {
  flex: 1;
  color: #ffffff;
}

.app-name {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
}

.app-desc {
  font-size: 22rpx;
  opacity: 0.9;
}

.auth-content {
  flex: 1;
  padding: 32rpx 32rpx 40rpx;
}

.auth-card {
  margin-top: -80rpx;
  background-color: $bg-white;
  border-radius: 24rpx;
  padding: 32rpx 28rpx 28rpx;
  box-shadow: 0 8rpx 20rpx rgba(15, 23, 42, 0.12);
  border: 1rpx solid $border-color;
}

.card-title-row {
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $text-color;
  display: block;
  margin-bottom: 4rpx;
}

.card-subtitle {
  font-size: 24rpx;
  color: $text-color-secondary;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 0 0;
}

.step-dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;

  &.active {
    background: linear-gradient(135deg, #3cc51f, #4ade80);
    box-shadow: 0 0 16rpx rgba(60, 197, 31, 0.3);
  }

  &.done {
    background: #3cc51f;
    box-shadow: 0 0 12rpx rgba(60, 197, 31, 0.25);
  }

  &.inactive {
    background: #e5e7eb;
    border: 1rpx solid #d1d5db;
  }
}

.step-num {
  font-size: 20rpx;
  font-weight: 600;
  color: #ffffff;
}

.step-check {
  font-size: 20rpx;
  color: #ffffff;
}

.step-line {
  width: 70rpx;
  height: 2rpx;
  background: linear-gradient(90deg, #3cc51f, rgba(148, 163, 184, 0.5));
  margin: 0 10rpx;
}

.step-labels {
  display: flex;
  justify-content: center;
  gap: 70rpx;
  padding-top: 8rpx;
}

.step-label {
  font-size: 22rpx;
  color: $text-color-secondary;
  transition: color 0.3s ease;

  &.active {
    color: $text-color;
  }
}

/* 表单 */
.form-body {
  padding-top: 20rpx;
}

.step-panel {
}

/* 密码强度（4段） */
.password-strength {
  margin-top: 8rpx;
  margin-bottom: 24rpx;
}

.strength-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 10rpx;
}

.strength-icon {
  font-size: 24rpx;
}

.strength-label {
  font-size: 22rpx;
  font-weight: 500;

  &.level-0 { color: #ef4444; }
  &.level-1 { color: #f97316; }
  &.level-2 { color: #eab308; }
  &.level-3 { color: #22c55e; }
}

.strength-bar {
  display: flex;
  gap: 6rpx;
  margin-bottom: 8rpx;
}

.bar {
  flex: 1;
  height: 6rpx;
  border-radius: 3rpx;
  background: #e5e7eb;
  transition: all 0.3s ease;

  &.active {
    &.level-0 { background: #fee2e2; }
    &.level-1 { background: #fed7aa; }
    &.level-2 { background: #fef08a; }
    &.level-3 { background: #bbf7d0; }
  }
}

.strength-tags {
  display: flex;
  gap: 8rpx;
}

.tag {
  font-size: 18rpx;
  color: $text-color-secondary;
  flex: 1;
  text-align: center;

  &.tag-0 { color: #ef4444; }
  &.tag-1 { color: #f97316; }
  &.tag-2 { color: #eab308; }
  &.tag-3 { color: #22c55e; }
}

/* 下一步按钮 */
.step-next-btn {
  margin-top: 8rpx;
  height: 88rpx;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #3cc51f, #4ade80);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  box-shadow: 0 8rpx 24rpx rgba(60, 197, 31, 0.3);
}

.step-btn-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #ffffff;
}

/* 返回按钮 */
.step-back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  margin-top: 24rpx;
  padding: 12rpx;
}

.back-btn-text {
  font-size: 24rpx;
  color: $text-color-secondary;
}

/* 底部 */
.form-footer {
  padding-top: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.footer-divider {
  width: 100%;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, rgba(148, 163, 184, 0.4), transparent);
}

.footer-login {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.footer-text {
  font-size: 24rpx;
  color: $text-color-secondary;
}

.footer-link {
  font-size: 24rpx;
  color: #3cc51f;
  font-weight: 500;
}
</style>
