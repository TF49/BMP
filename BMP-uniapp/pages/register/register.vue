<template>
  <view class="auth-page">
    <!-- 顶部艺术化背景（与登录页保持一致） -->
    <view class="auth-visual-bg">
      <view class="blob-1"></view>
      <view class="blob-2"></view>
      <view class="blob-3"></view>
    </view>

    <!-- 主体内容 -->
    <view class="auth-container">
      <view class="auth-header animate-fade-in">
        <view class="logo-wrapper">
          <view class="logo-box">
            <uni-icons type="medal-filled" size="44" color="#ffffff"></uni-icons>
          </view>
          <view class="logo-ring"></view>
        </view>
        <view class="header-content">
          <text class="app-name">加入羽擎</text>
          <text class="app-motto">开启您的运动新篇章</text>
        </view>
      </view>

      <view class="auth-card animate-slide-up">
        <!-- 步骤指示器重构 -->
        <view class="modern-steps">
          <view class="step-item" :class="{ 'active': currentStep >= 1, 'done': currentStep > 1 }">
            <view class="step-icon">
              <text v-if="currentStep > 1">✓</text>
              <text v-else>1</text>
            </view>
            <text class="step-text">账号设置</text>
          </view>
          <view class="step-connector" :class="{ 'active': currentStep > 1 }"></view>
          <view class="step-item" :class="{ 'active': currentStep >= 2, 'done': currentStep > 2 }">
            <view class="step-icon">
              <text v-if="currentStep > 2">✓</text>
              <text v-else>2</text>
            </view>
            <text class="step-text">实名认证</text>
          </view>
        </view>

        <view class="form-group">
          <!-- Step 1: 账号信息 -->
          <view class="step-panel" v-if="currentStep === 1">
            <view class="input-item">
              <text class="input-label">用户名</text>
              <AnimatedInput
                v-model="form.username"
                placeholder="4-20位字母/数字/下划线"
                icon-type="person"
                :error-message="errors.username"
                :hint="usernameHint"
                @focus="clearError('username')"
                @input="validateUsername"
              />
            </view>

            <view class="input-item">
              <text class="input-label">登录密码</text>
              <AnimatedInput
                v-model="form.password"
                type="password"
                placeholder="至少6位安全密码"
                icon-type="locked"
                :error-message="errors.password"
                @focus="clearError('password')"
                @input="validatePassword"
              />
            </view>

            <view class="input-item">
              <text class="input-label">确认密码</text>
              <AnimatedInput
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                icon-type="locked"
                :error-message="errors.confirmPassword"
                @focus="clearError('confirmPassword')"
                @input="validateConfirmPassword"
              />
            </view>

            <!-- 密码强度指示器 -->
            <view v-if="form.password" class="strength-meter">
              <view class="strength-label-row">
                <text class="label">安全等级：</text>
                <text class="value" :class="'level-' + passwordStrength">{{ passwordStrengthText }}</text>
              </view>
              <view class="strength-bars">
                <view v-for="i in 4" :key="i" class="bar" :class="{ 'active': i <= passwordStrength + 1, ['level-' + passwordStrength]: i <= passwordStrength + 1 }"></view>
              </view>
            </view>

            <view class="submit-section">
              <AnimatedButton
                text="下一步"
                type="primary"
                size="large"
                @click="goToStep2"
              />
            </view>
          </view>

          <!-- Step 2: 身份信息 -->
          <view class="step-panel" v-if="currentStep === 2">
            <view class="tips-box">
              <uni-icons type="info" size="16" color="#3cc51f"></uni-icons>
              <text class="tips-text">根据相关规定，预订场馆需进行实名认证</text>
            </view>

            <view class="input-item">
              <text class="input-label">身份证号</text>
              <AnimatedInput
                v-model="form.idCard"
                placeholder="请输入18位有效身份证号"
                icon-type="contact"
                :error-message="errors.idCard"
                :maxlength="18"
                @focus="clearError('idCard')"
                @input="validateIdCard"
              />
            </view>

            <view class="submit-section">
              <AnimatedButton
                :text="loading ? '提交中...' : '完成注册并登录'"
                type="primary"
                size="large"
                :loading="loading"
                :disabled="!canSubmit"
                @click="handleRegister"
              />
              <view class="back-link" @tap="goToStep1">
                <text>返回修改账号信息</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="auth-footer animate-fade-in">
        <text class="footer-tip">已有账号？</text>
        <text class="footer-link" @tap="navigateToLogin">立即登录</text>
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
  background-color: #f8fafc;
  position: relative;
  overflow: hidden;
}

/* 艺术化背景 - 与登录页保持高度一致 */
.auth-visual-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 600rpx;
  z-index: 0;
  background: linear-gradient(135deg, #f0fdf4 0%, #ffffff 100%);

  .blob-1, .blob-2, .blob-3 {
    position: absolute;
    filter: blur(80rpx);
    border-radius: 50%;
    opacity: 0.6;
  }

  .blob-1 {
    width: 400rpx;
    height: 400rpx;
    background: rgba(60, 197, 31, 0.15);
    top: -100rpx;
    right: -100rpx;
  }

  .blob-2 {
    width: 300rpx;
    height: 300rpx;
    background: rgba(74, 222, 128, 0.1);
    top: 200rpx;
    left: -100rpx;
  }

  .blob-3 {
    width: 250rpx;
    height: 250rpx;
    background: rgba(99, 102, 241, 0.08);
    top: 100rpx;
    left: 40%;
  }
}

.auth-container {
  position: relative;
  z-index: 1;
  padding: 0 40rpx;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.auth-header {
  margin-top: calc(80rpx + env(safe-area-inset-top));
  margin-bottom: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;

  .logo-wrapper {
    position: relative;
    width: 140rpx;
    height: 140rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24rpx;

    .logo-box {
      width: 100rpx;
      height: 100rpx;
      background: linear-gradient(135deg, #3cc51f, #4ade80);
      border-radius: 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 16rpx 32rpx rgba(60, 197, 31, 0.25);
      z-index: 2;
    }

    .logo-ring {
      position: absolute;
      width: 140rpx;
      height: 140rpx;
      border: 2rpx solid rgba(60, 197, 31, 0.2);
      border-radius: 50%;
      animation: pulse 2s infinite;
    }
  }

  .header-content {
    text-align: center;
    
    .app-name {
      font-size: 40rpx;
      font-weight: 800;
      color: #1e293b;
      letter-spacing: 2rpx;
      margin-bottom: 4rpx;
      display: block;
    }

    .app-motto {
      font-size: 22rpx;
      color: #64748b;
      letter-spacing: 4rpx;
    }
  }
}

.auth-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 48rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 40rpx 100rpx rgba(15, 23, 42, 0.08);
  border: 1rpx solid rgba(255, 255, 255, 0.5);
  margin-bottom: 40rpx;
}

/* 步骤指示器重构 */
.modern-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 50rpx;
  padding: 0 20rpx;

  .step-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    z-index: 2;

    .step-icon {
      width: 56rpx;
      height: 56rpx;
      border-radius: 50%;
      background: #f1f5f9;
      color: #94a3b8;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      font-weight: 700;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      border: 4rpx solid transparent;
    }

    .step-text {
      font-size: 22rpx;
      color: #94a3b8;
      font-weight: 500;
      transition: all 0.4s;
    }

    &.active {
      .step-icon {
        background: #ffffff;
        color: #3cc51f;
        border-color: #3cc51f;
        box-shadow: 0 0 20rpx rgba(60, 197, 31, 0.2);
        transform: scale(1.1);
      }
      .step-text {
        color: #1e293b;
        font-weight: 600;
      }
    }

    &.done {
      .step-icon {
        background: #3cc51f;
        color: #ffffff;
        border-color: #3cc51f;
      }
    }
  }

  .step-connector {
    flex: 1;
    height: 4rpx;
    background: #f1f5f9;
    margin: 0 -10rpx 34rpx;
    position: relative;
    z-index: 1;

    &::after {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      height: 100%;
      width: 0;
      background: #3cc51f;
      transition: width 0.6s ease;
    }

    &.active::after {
      width: 100%;
    }
  }
}

.form-group {
  .input-item {
    margin-bottom: 24rpx;

    .input-label {
      font-size: 24rpx;
      color: #64748b;
      margin-left: 10rpx;
      margin-bottom: 10rpx;
      display: block;
      font-weight: 500;
    }
  }
}

/* 密码强度指示器 */
.strength-meter {
  margin: 20rpx 10rpx 40rpx;

  .strength-label-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12rpx;
    
    .label {
      font-size: 22rpx;
      color: #94a3b8;
    }
    
    .value {
      font-size: 22rpx;
      font-weight: 600;
      
      &.level-0 { color: #ef4444; }
      &.level-1 { color: #f97316; }
      &.level-2 { color: #eab308; }
      &.level-3 { color: #22c55e; }
    }
  }

  .strength-bars {
    display: flex;
    gap: 8rpx;
    
    .bar {
      flex: 1;
      height: 6rpx;
      background: #f1f5f9;
      border-radius: 3rpx;
      transition: all 0.4s;
      
      &.active {
        &.level-0 { background: #ef4444; }
        &.level-1 { background: #f97316; }
        &.level-2 { background: #eab308; }
        &.level-3 { background: #22c55e; }
      }
    }
  }
}

.tips-box {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: rgba(60, 197, 31, 0.05);
  padding: 24rpx;
  border-radius: 16rpx;
  margin-bottom: 40rpx;
  border: 1rpx solid rgba(60, 197, 31, 0.1);

  .tips-text {
    font-size: 22rpx;
    color: #166534;
    flex: 1;
  }
}

.submit-section {
  margin-top: 40rpx;

  .back-link {
    text-align: center;
    margin-top: 30rpx;
    padding: 10rpx;
    
    text {
      font-size: 24rpx;
      color: #94a3b8;
      text-decoration: underline;
    }
    
    &:active text {
      color: #64748b;
    }
  }
}

.auth-footer {
  margin-top: auto;
  padding: 40rpx 0 60rpx;
  text-align: center;
  font-size: 24rpx;
  color: #94a3b8;

  .footer-link {
    color: #3cc51f;
    font-weight: 600;
    margin-left: 10rpx;
  }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.2; }
  100% { transform: scale(1); opacity: 0.5; }
}
</style>
