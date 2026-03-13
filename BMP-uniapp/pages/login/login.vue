<template>
  <view class="auth-page">
    <!-- 顶部渐变头部，风格与首页一致 -->
    <view class="auth-header">
      <view class="auth-header-content">
        <view class="logo-circle">
          <uni-icons type="medal" size="38" color="#ffffff" class="logo-icon"></uni-icons>
        </view>
        <view class="header-texts">
          <text class="app-name">羽擎</text>
          <text class="app-desc">登录后预约场地与课程</text>
        </view>
      </view>
    </view>

    <!-- 表单卡片 -->
    <view class="auth-content">
      <!-- 卖点/功能点：填充头部与表单间留白 -->
      <view class="auth-highlights animate-fade-in">
        <view class="highlights-inner">
          <view class="highlight-item">
            <view class="highlight-icon highlight-icon--primary">
              <uni-icons type="calendar" size="20" color="#ffffff"></uni-icons>
            </view>
            <view class="highlight-texts">
              <text class="highlight-title">快捷预约</text>
              <text class="highlight-desc">场地/课程一键下单</text>
            </view>
          </view>

          <view class="highlight-item">
            <view class="highlight-icon highlight-icon--success">
              <uni-icons type="reload" size="20" color="#ffffff"></uni-icons>
            </view>
            <view class="highlight-texts">
              <text class="highlight-title">实时可用</text>
              <text class="highlight-desc">余量与时间段实时更新</text>
            </view>
          </view>

          <view class="highlight-item">
            <view class="highlight-icon highlight-icon--indigo">
              <uni-icons type="list" size="20" color="#ffffff"></uni-icons>
            </view>
            <view class="highlight-texts">
              <text class="highlight-title">订单管理</text>
              <text class="highlight-desc">记录、退款与通知可追踪</text>
            </view>
          </view>
        </view>
      </view>

      <view class="auth-card animate-slide-up">
        <view class="card-handle" />
        <view class="card-title-row">
          <text class="card-title">登录</text>
          <text class="card-subtitle">欢迎回来</text>
        </view>

        <view class="form-body">
          <view class="field">
            <text class="field-label">账号</text>
            <AnimatedInput
              v-model="formData.username"
              type="text"
              placeholder="请输入用户名或手机号"
              icon-type="person"
              :maxlength="20"
            />
          </view>

          <view class="field">
            <text class="field-label">密码</text>
            <AnimatedInput
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              icon-type="locked"
              :maxlength="32"
            />
          </view>

          <view class="form-options">
            <text class="option-text option-tap" @tap="handleForgotPassword">忘记密码?</text>
            <text class="option-link option-tap" @tap="handleRegister">注册新账号</text>
          </view>

          <AnimatedButton
            :text="loading ? '登录中...' : '立即登录'"
            type="primary"
            size="large"
            :loading="loading"
            :disabled="loading"
            @click="handleLogin"
          />
        </view>

        <view class="form-footer">
          <text class="footer-tip">登录即表示同意</text>
          <text class="footer-link" @tap="handleUserAgreement">《用户协议》</text>
          <text class="footer-tip">和</text>
          <text class="footer-link" @tap="handlePrivacyPolicy">《隐私政策》</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import AnimatedInput from '@/components/common/AnimatedInput.vue'
import AnimatedButton from '@/components/common/AnimatedButton.vue'
import { login } from '@/api/auth'
import { isAllowedRole, isPresidentRole } from '@/utils/roleCheck'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeReLaunch } from '@/utils/safeRoute'

const userStore = useUserStore()

onShow(async () => {
  if (!userStore.token) {
    const savedToken = uni.getStorageSync('token')
    if (savedToken) {
      userStore.token = savedToken
      userStore.userInfo = uni.getStorageSync('userInfo')
    }
  }

  if (userStore.token) {
    await userStore.checkLogin()
  }

  if (!userStore.token) return

  const role = userStore.userInfo?.role
  if (!role || !isAllowedRole(role)) {
    userStore.logout()
    return
  }

  setTimeout(() => {
    if (isPresidentRole(role)) {
      safeReLaunch(PRESIDENT_PAGES.DASHBOARD, PRESIDENT_PAGES.DASHBOARD)
    } else {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }, 50)
})

const loading = ref(false)
const formData = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!formData.value.username || !formData.value.password) {
    uni.showToast({
      title: '请填写完整信息',
      icon: 'none'
    })
    return
  }

  loading.value = true

  login({
    username: formData.value.username,
    password: formData.value.password
  })
    .then((res: any) => {
      if (res.code === 200) {
        const role = res.data.user?.role
        if (res.data.user && role && !isAllowedRole(role)) {
          uni.showToast({
            title: '该角色请使用网页端管理系统登录',
            icon: 'none',
            duration: 3000
          })
          loading.value = false
          return
        }

        uni.setStorageSync('token', res.data.token)
        if (res.data.refreshToken) {
          uni.setStorageSync('refreshToken', res.data.refreshToken)
        }

        const userInfo = {
          id: res.data.user.id,
          userId: res.data.user.id,
          username: res.data.user.username,
          role: res.data.user.role,
          status: res.data.user.status
        }
        uni.setStorageSync('userInfo', userInfo)

        const userStore = useUserStore()
        userStore.token = res.data.token
        userStore.userInfo = userInfo as any

        uni.showToast({
          title: '登录成功',
          icon: 'success',
          duration: 1500
        })

        setTimeout(() => {
          if (isPresidentRole(role)) {
            safeReLaunch(PRESIDENT_PAGES.DASHBOARD, PRESIDENT_PAGES.DASHBOARD)
          } else {
            uni.switchTab({ url: '/pages/index/index' })
          }
        }, 1500)
      } else {
        uni.showToast({
          title: res.msg || res.message || '登录失败',
          icon: 'none'
        })
        loading.value = false
      }
    })
    .catch((err: any) => {
      const errorMessage = err.message || '登录失败，请稍后重试'
      uni.showToast({
        title: errorMessage,
        icon: 'none',
        duration: 2000
      })
      console.error('登录错误:', err)
      loading.value = false
    })
}

const handleRegister = () => {
  uni.navigateTo({
    url: '/pages/register/register'
  })
}

const handleForgotPassword = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const handleUserAgreement = () => {
  // 可后续替换为实际协议页路径
  uni.showToast({ title: '用户协议', icon: 'none' })
}

const handlePrivacyPolicy = () => {
  // 可后续替换为实际隐私政策页路径
  uni.showToast({ title: '隐私政策', icon: 'none' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.auth-page {
  min-height: 100vh;
  min-width: 0;
  overflow-x: hidden;
  background:
    linear-gradient(180deg, rgba(60, 197, 31, 0.08) 0%, rgba(248, 250, 252, 1) 56%),
    $bg-color;
  display: flex;
  flex-direction: column;
  position: relative;

  /* 柔和背景光斑，提升质感（不拦截触摸） */
  &::before,
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 0;
  }

  &::before {
    background:
      radial-gradient(900rpx 520rpx at 12% 10%, rgba(60, 197, 31, 0.20), transparent 56%),
      radial-gradient(820rpx 520rpx at 92% 26%, rgba(74, 222, 128, 0.16), transparent 52%),
      radial-gradient(900rpx 520rpx at 50% 110%, rgba(99, 102, 241, 0.07), transparent 60%);
  }

  &::after {
    background: linear-gradient(180deg, rgba(248, 250, 252, 0) 0%, rgba(248, 250, 252, 1) 62%);
  }
}

.auth-header {
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  padding: calc(72rpx + env(safe-area-inset-top)) 40rpx 56rpx;
  border-radius: 0 0 44rpx 44rpx;
  box-shadow: 0 10rpx 26rpx rgba(60, 197, 31, 0.26), inset 0 2rpx 0 rgba(255, 255, 255, 0.18);
  position: relative;
  z-index: 1;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    pointer-events: none;
    background:
      radial-gradient(520rpx 320rpx at 18% 30%, rgba(255, 255, 255, 0.22), transparent 60%),
      radial-gradient(560rpx 360rpx at 86% 18%, rgba(255, 255, 255, 0.18), transparent 62%),
      radial-gradient(760rpx 420rpx at 50% 115%, rgba(0, 0, 0, 0.16), transparent 60%);
    opacity: 0.9;
  }
}

.auth-header-content {
  display: flex;
  align-items: center;
  gap: 24rpx;
  max-width: 720rpx;
  margin: 0 auto;
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
  min-width: 0;
  color: #ffffff;
}

.app-name {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-desc {
  font-size: 22rpx;
  opacity: 0.9;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
}

.auth-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24rpx 32rpx calc(40rpx + env(safe-area-inset-bottom));
  position: relative;
  z-index: 1;

  &::before {
    content: '';
    position: absolute;
    left: 20rpx;
    right: 20rpx;
    bottom: calc(16rpx + env(safe-area-inset-bottom));
    height: 260rpx;
    border-radius: 28rpx;
    background:
      radial-gradient(520rpx 220rpx at 15% 30%, rgba(60, 197, 31, 0.10), transparent 62%),
      radial-gradient(520rpx 220rpx at 88% 40%, rgba(99, 102, 241, 0.06), transparent 62%),
      linear-gradient(180deg, rgba(255, 255, 255, 0.68) 0%, rgba(255, 255, 255, 0.92) 100%);
    box-shadow: 0 20rpx 60rpx rgba(15, 23, 42, 0.10);
    pointer-events: none;
    z-index: 0;
  }
}

.auth-highlights {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  padding: 10rpx 0 18rpx;
}

.highlights-inner {
  width: 100%;
  max-width: 720rpx;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx;
}

.highlight-item {
  flex: 1 1 210rpx;
  min-width: 210rpx;
  padding: 18rpx 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.74);
  border: 1rpx solid rgba(15, 23, 42, 0.06);
  box-shadow:
    0 10rpx 30rpx rgba(15, 23, 42, 0.08),
    0 4rpx 10rpx rgba(15, 23, 42, 0.05);
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.highlight-item:active {
  transform: translateY(1rpx);
  opacity: 0.92;
}

@supports (backdrop-filter: blur(10px)) or (-webkit-backdrop-filter: blur(10px)) {
  .highlight-item {
    background: rgba(255, 255, 255, 0.64);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
  }
}

.highlight-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 10rpx 20rpx rgba(15, 23, 42, 0.10);
}

.highlight-icon--primary {
  background: linear-gradient(135deg, rgba(60, 197, 31, 1) 0%, rgba(74, 222, 128, 1) 100%);
}

.highlight-icon--success {
  background: linear-gradient(135deg, rgba(34, 197, 94, 1) 0%, rgba(132, 204, 22, 1) 100%);
}

.highlight-icon--indigo {
  background: linear-gradient(135deg, rgba(99, 102, 241, 1) 0%, rgba(129, 140, 248, 1) 100%);
}

.highlight-texts {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.highlight-title {
  font-size: 26rpx;
  font-weight: 700;
  color: rgba(15, 23, 42, 0.92);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.highlight-desc {
  font-size: 20rpx;
  color: rgba(71, 85, 105, 0.95);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media screen and (max-width: 375px) {
  .auth-content {
    padding-left: 24rpx;
    padding-right: 24rpx;
  }

  .highlights-inner {
    gap: 14rpx;
  }

  .highlight-item {
    flex-basis: 240rpx;
    min-width: 240rpx;
    padding: 16rpx 14rpx;
  }
}

@media screen and (max-height: 720px) {
  .auth-highlights {
    padding-top: 0;
    padding-bottom: 10rpx;
  }

  .highlight-item {
    padding: 14rpx 14rpx;
  }
}

.auth-card {
  margin-top: 0;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 28rpx;
  padding: 32rpx 28rpx 28rpx;
  box-shadow:
    0 18rpx 58rpx rgba(15, 23, 42, 0.16),
    0 6rpx 18rpx rgba(15, 23, 42, 0.08);
  border: 1rpx solid rgba(15, 23, 42, 0.06);
  position: relative;
  z-index: 2;
}

.card-handle {
  width: 96rpx;
  height: 10rpx;
  border-radius: 999rpx;
  margin: -6rpx auto 18rpx;
  background: rgba(15, 23, 42, 0.10);
}

.card-title-row {
  margin-bottom: 26rpx;
}

.card-title {
  font-size: 38rpx;
  font-weight: 700;
  color: $text-color;
  display: block;
  margin-bottom: 8rpx;
  letter-spacing: 0.5rpx;
}

.card-subtitle {
  font-size: 22rpx;
  color: $text-color-secondary;
  font-weight: 400;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.form-body :deep(.animated-input-wrapper) {
  margin-bottom: 28rpx;
}

.form-body :deep(.animated-button) {
  margin-top: 6rpx;
}

.field {
  display: flex;
  flex-direction: column;
}

.field-label {
  font-size: 22rpx;
  color: rgba(15, 23, 42, 0.62);
  margin: 0 0 10rpx 4rpx;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  margin-bottom: 22rpx;
  margin-top: 8rpx;
  min-height: 72rpx;
}

.option-text {
  color: $text-color-secondary;
}

.option-link {
  color: #3cc51f;
  font-weight: 500;
}

.option-tap {
  padding: 12rpx 8rpx;
  cursor: pointer;

  &:active {
    opacity: 0.75;
  }
}

.form-footer {
  margin-top: 24rpx;
  padding-top: 16rpx;
  text-align: center;
  font-size: 20rpx;
  color: $text-color-secondary;
}

.footer-tip {
  font-size: 20rpx;
  color: $text-color-secondary;
}

.footer-link {
  font-size: 20rpx;
  color: #3cc51f;
  font-weight: 500;
  text-decoration: underline;
  padding: 0 4rpx;
  cursor: pointer;

  &:active {
    opacity: 0.75;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-highlights {
    animation: none !important;
  }

  :deep(.animate-slide-up) {
    animation: none !important;
  }
}
</style>
