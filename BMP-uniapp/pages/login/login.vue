<template>
  <view class="auth-page">
    <!-- WeChat Mini-Program Capsule Safe Area -->
    <view class="safe-area-capsule"></view>

    <!-- Top AppBar -->
    <view class="top-bar">
      <view class="brand-logo">
        <text class="icon-bolt">⚡</text>
        <text class="brand-text">Kinetic Logic</text>
      </view>
    </view>

    <!-- Main Content -->
    <view class="main-content">
      <!-- Hero Branding -->
      <view class="hero-section">
        <text class="main-title">羽   擎</text>
        <text class="sub-title">BMP PERFORMANCE GATEWAY</text>
      </view>

      <!-- Login Form -->
      <view class="login-form">
        <!-- Account Input -->
        <view class="field-group">
          <text class="field-label">账号/手机号 ACCOUNT / MOBILE NUMBER</text>
          <view class="input-wrapper">
            <view class="input-container">
              <text class="input-icon">👤</text>
              <input 
                class="input-field" 
                v-model="formData.username" 
                placeholder="输入您的账号" 
                placeholder-class="input-placeholder"
                type="text"
                @focus="focusAccount = true"
                @blur="focusAccount = false"
              />
            </view>
            <view class="input-underline" :class="{ 'active': focusAccount }"></view>
          </view>
        </view>

        <!-- Password Input -->
        <view class="field-group">
          <view class="field-header">
            <text class="field-label">密码 PASSWORD</text>
            <text class="link-forgot" @tap="handleForgotPassword">忘记密码?</text>
          </view>
          <view class="input-wrapper">
            <view class="input-container">
              <text class="input-icon">🔒</text>
              <input 
                class="input-field" 
                v-model="formData.password" 
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••" 
                placeholder-class="input-placeholder"
                @focus="focusPassword = true"
                @blur="focusPassword = false"
              />
              <text class="toggle-icon" @tap="showPassword = !showPassword">
                {{ showPassword ? '👁️' : '👁️‍🗨️' }}
              </text>
            </view>
            <view class="input-underline" :class="{ 'active': focusPassword }"></view>
          </view>
        </view>

        <!-- Primary Action -->
        <view class="action-primary">
          <button 
            class="btn-login" 
            :loading="loading" 
            :disabled="loading" 
            @tap="handleLogin"
          >
            <text v-if="!loading" class="btn-text">进入系统</text>
            <text v-if="!loading" class="btn-icon">→</text>
            <text v-else>登录中...</text>
          </button>
        </view>

        <!-- Secondary Action -->
        <view class="action-secondary">
          <view class="btn-register" @tap="handleRegister">
            <text class="register-text">立即注册</text>
            <text class="register-icon">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Footer Decoration -->
    <view class="footer-decoration">
      <view class="footer-content">
        <text class="footer-title">羽擎管理系统</text>
        <text class="footer-subtitle">动力精密架构</text>
      </view>
    </view>

    <!-- Abstract Shape Decoration -->
    <view class="abstract-shape"></view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import { login } from '@/api/auth'
import { isAllowedRole, isPresidentRole } from '@/utils/roleCheck'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeReLaunch } from '@/utils/safeRoute'

const userStore = useUserStore()
const loading = ref(false)
const showPassword = ref(false)
const focusAccount = ref(false)
const focusPassword = ref(false)
const formData = ref({
  username: '',
  password: ''
})

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
      const role = res.user?.role
      if (res.user && role && !isAllowedRole(role)) {
        uni.showToast({
          title: '该角色请使用网页端管理系统登录',
          icon: 'none',
          duration: 3000
        })
        loading.value = false
        return
      }

      uni.setStorageSync('token', res.token)
      if (res.refreshToken) {
        uni.setStorageSync('refreshToken', res.refreshToken)
      }

      const userInfo = {
        id: res.user.id,
        userId: res.user.id,
        username: res.user.username,
        role: res.user.role,
        status: res.user.status
      }
      uni.setStorageSync('userInfo', userInfo)

      const userStore = useUserStore()
      userStore.token = res.token
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
    title: '请联系管理员重置密码',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

view, text, scroll-view, input, button, textarea {
  box-sizing: border-box;
}

.auth-page {
  width: 100vw;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  background: #f9f9f9;
  display: flex;
  flex-direction: column;
  
  // 背景渐变效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle at 100% 0%, rgba(255, 102, 0, 0.05) 0%, transparent 40%),
                radial-gradient(circle at 0% 100%, rgba(255, 102, 0, 0.03) 0%, transparent 40%);
    pointer-events: none;
    z-index: 0;
  }
}

// WeChat Mini-Program Capsule Safe Area
.safe-area-capsule {
  height: 120rpx;
  width: 100%;
  flex-shrink: 0;
}

// Top AppBar
.top-bar {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 48rpx;
  height: 120rpx;
  width: 100%;
  box-sizing: border-box;
  background: transparent;

  .brand-logo {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .icon-bolt {
      font-size: 48rpx;
      color: #FF6600;
      line-height: 1;
    }

    .brand-text {
      font-size: 48rpx;
      font-weight: bold;
      font-style: italic;
      letter-spacing: -2rpx;
      color: #FF6600;
      font-family: 'Lexend', -apple-system, BlinkMacSystemFont, sans-serif;
      line-height: 1;
    }
  }
}

// Main Content
.main-content {
  position: relative;
  z-index: 1;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  padding: 80rpx 48rpx 240rpx;
  max-width: 100vw;
  margin: 0;
  width: 100%;
  box-sizing: border-box;
}

// Hero Branding
.hero-section {
  margin-bottom: 80rpx;

  .main-title {
    display: block;
    font-size: 88rpx;
    font-weight: bold;
    color: #1a1c1c;
    margin-bottom: 16rpx;
    letter-spacing: 16rpx;
    line-height: 1.2;
    white-space: nowrap;
    width: 100%;
    text-align: left;
  }

  .sub-title {
    display: block;
    font-size: 26rpx;
    color: #636262;
    letter-spacing: 8rpx;
    opacity: 0.7;
    text-transform: uppercase;
  }
}

// Login Form
.login-form {
  display: flex;
  flex-direction: column;
  gap: 48rpx;
}

.field-group {
  position: relative;

  .field-label {
    display: block;
    font-size: 20rpx;
    font-weight: bold;
    letter-spacing: 1rpx;
    text-transform: uppercase;
    color: #5f5e5e;
    margin-bottom: 16rpx;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .field-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 16rpx;

    .link-forgot {
      font-size: 24rpx;
      font-weight: bold;
      color: #a33e00;
      letter-spacing: -0.5rpx;
      transition: opacity 0.3s ease;

      &:active {
        opacity: 0.8;
      }
    }
  }

  .input-wrapper {
    position: relative;

    .input-container {
      display: flex;
      align-items: center;
      background: #ffffff;
      border-radius: 16rpx;
      overflow: hidden;
      transition: all 0.3s ease;
      padding: 0 32rpx;
      height: 112rpx;

      .input-icon {
        font-size: 40rpx;
        color: #8e7164;
        margin-right: 32rpx;
        flex-shrink: 0;
      }

      .input-field {
        flex: 1;
        width: 100%;
        background: transparent;
        border: none;
        font-size: 32rpx;
        color: #1a1c1c;
        height: 100%;
      }

      .input-placeholder {
        color: #e3bfb1;
      }

      .toggle-icon {
        font-size: 40rpx;
        color: #8e7164;
        margin-left: 32rpx;
        flex-shrink: 0;
        cursor: pointer;
        transition: color 0.3s ease;

        &:active {
          color: #a33e00;
        }
      }
    }

    .input-underline {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 0;
      height: 4rpx;
      background: #a33e00;
      transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 2rpx;

      &.active {
        width: 100%;
      }
    }
  }
}

// Primary Action
.action-primary {
  padding-top: 40rpx;

  .btn-login {
    width: 100%;
    height: 128rpx;
    background: linear-gradient(45deg, #a33e00 0%, #ff6600 100%);
    border-radius: 16rpx;
    color: #ffffff;
    font-weight: bold;
    border: none;
    box-shadow: 0 16rpx 32rpx rgba(163, 62, 0, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24rpx;
    transition: transform 0.3s ease;

    .btn-text {
      font-size: 32rpx;
      letter-spacing: 10rpx;
      font-weight: bold;
    }

    .btn-icon {
      font-size: 48rpx;
      line-height: 1;
    }

    &:active {
      transform: scale(0.98);
    }

    &::after {
      border: none;
    }
  }
}

// Secondary Action
.action-secondary {
  display: flex;
  justify-content: center;
  padding-top: 20rpx;

  .btn-register {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 20rpx 48rpx;
    border-radius: 16rpx;
    color: #a33e00;
    font-weight: bold;
    transition: all 0.3s ease;

    .register-text {
      font-size: 30rpx;
      letter-spacing: 10rpx;
    }

    .register-icon {
      font-size: 40rpx;
      line-height: 1;
    }

    &:active {
      background: rgba(163, 62, 0, 0.05);
      transform: scale(0.98);
    }
  }
}

// Footer Decoration
.footer-decoration {
  position: fixed;
  bottom: 80rpx;
  left: 0;
  width: 100%;
  padding: 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  z-index: 1;

  .footer-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0.4;

    .footer-title {
      font-size: 22rpx;
      font-weight: bold;
      letter-spacing: 14rpx;
      text-transform: uppercase;
      margin-bottom: 10rpx;
      color: #1a1c1c;
    }

    .footer-subtitle {
      font-size: 18rpx;
      letter-spacing: 5rpx;
      color: #1a1c1c;
    }
  }
}

// Abstract Shape Decoration
.abstract-shape {
  position: fixed;
  top: 0;
  right: 0;
  width: 400rpx;
  height: 400rpx;
  opacity: 0.05;
  z-index: 0;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath fill='%23a33e00' d='M44.7,-76.4C58.8,-69.2,71.8,-59.1,79.6,-46.2C87.4,-33.3,90.1,-17.7,89.5,-1.9C88.8,13.8,84.9,29.8,76.4,43.2C67.9,56.7,54.9,67.7,40.5,74.5C26.1,81.3,10.2,83.9,-4.9,81.9C-20,79.9,-34.3,73.4,-47.5,65.8C-60.7,58.2,-72.8,49.5,-79.8,38.1C-86.8,26.7,-88.7,12.6,-87.3,-1.2C-85.8,-15,-81,-28.4,-73.1,-39.9C-65.1,-51.4,-54,-61,-41.2,-68.8C-28.4,-76.6,-14.2,-82.6,0.5,-83.4C15.2,-84.3,30.5,-80,44.7,-76.4Z' transform='translate(100 100)'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
}
</style>
