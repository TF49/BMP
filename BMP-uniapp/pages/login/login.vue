<template>
  <view class="auth-page">
    <!-- 顶部艺术化背景 -->
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
          <text class="app-name">羽擎 BMP</text>
          <text class="app-motto">极简 · 运动 · 生活</text>
        </view>
      </view>

      <view class="auth-card animate-slide-up">
        <view class="card-tabs">
          <view class="tab-item active">
            <text class="tab-text">账号登录</text>
            <view class="tab-line"></view>
          </view>
          <view class="tab-item" @tap="handleRegister">
            <text class="tab-text">新用户注册</text>
          </view>
        </view>

        <view class="form-group">
          <view class="input-item">
            <text class="input-label">账号</text>
            <AnimatedInput
              v-model="formData.username"
              placeholder="手机号 / 用户名"
              icon-type="person"
              :maxlength="20"
            />
          </view>

          <view class="input-item">
            <text class="input-label">密码</text>
            <AnimatedInput
              v-model="formData.password"
              type="password"
              placeholder="请输入登录密码"
              icon-type="locked"
              :maxlength="32"
            />
          </view>

          <view class="form-actions">
            <label class="remember-me" @tap="rememberMe = !rememberMe">
              <checkbox :checked="rememberMe" color="#3cc51f" style="transform:scale(0.7)" />
              <text class="action-text">记住我</text>
            </label>
            <text class="forgot-pwd" @tap="handleForgotPassword">忘记密码？</text>
          </view>

          <view class="submit-section">
            <AnimatedButton
              :text="loading ? '验证中...' : '安全登录'"
              type="primary"
              size="large"
              :loading="loading"
              :disabled="loading"
              @click="handleLogin"
            />
          </view>
        </view>

        <view class="social-login">
          <view class="divider">
            <view class="line"></view>
            <text class="divider-text">其他登录方式</text>
            <view class="line"></view>
          </view>
          <view class="social-icons">
            <view class="icon-btn wechat" @tap="handleWechatLogin">
              <uni-icons type="weixin" size="28" color="#07C160"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="auth-footer animate-fade-in">
        <text class="footer-tip">登录即代表同意</text>
        <text class="footer-link" @tap="handleUserAgreement">用户协议</text>
        <text class="footer-sep">与</text>
        <text class="footer-link" @tap="handlePrivacyPolicy">隐私政策</text>
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
const rememberMe = ref(false)

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

const handleWechatLogin = () => {
  uni.showToast({
    title: '微信一键登录开发中',
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
  background-color: #f8fafc;
  position: relative;
  overflow: hidden;
}

/* 艺术化背景 */
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
  margin-top: calc(120rpx + env(safe-area-inset-top));
  margin-bottom: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;

  .logo-wrapper {
    position: relative;
    width: 160rpx;
    height: 160rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 32rpx;

    .logo-box {
      width: 120rpx;
      height: 120rpx;
      background: linear-gradient(135deg, #3cc51f, #4ade80);
      border-radius: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 20rpx 40rpx rgba(60, 197, 31, 0.3);
      z-index: 2;
    }

    .logo-ring {
      position: absolute;
      width: 160rpx;
      height: 160rpx;
      border: 2rpx solid rgba(60, 197, 31, 0.2);
      border-radius: 50%;
      animation: pulse 2s infinite;
    }
  }

  .header-content {
    text-align: center;
    
    .app-name {
      font-size: 44rpx;
      font-weight: 800;
      color: #1e293b;
      letter-spacing: 2rpx;
      margin-bottom: 8rpx;
      display: block;
    }

    .app-motto {
      font-size: 24rpx;
      color: #64748b;
      letter-spacing: 8rpx;
      text-transform: uppercase;
    }
  }
}

.auth-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 48rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 40rpx 100rpx rgba(15, 23, 42, 0.08);
  border: 1rpx solid rgba(255, 255, 255, 0.5);

  .card-tabs {
    display: flex;
    gap: 40rpx;
    margin-bottom: 60rpx;
    padding-left: 10rpx;

    .tab-item {
      position: relative;
      padding: 10rpx 0;

      .tab-text {
        font-size: 32rpx;
        color: #94a3b8;
        font-weight: 600;
        transition: all 0.3s;
      }

      &.active {
        .tab-text {
          color: #1e293b;
          font-size: 36rpx;
        }
        
        .tab-line {
          position: absolute;
          bottom: -4rpx;
          left: 0;
          width: 40rpx;
          height: 6rpx;
          background: #3cc51f;
          border-radius: 3rpx;
        }
      }
    }
  }
}

.form-group {
  .input-item {
    margin-bottom: 30rpx;

    .input-label {
      font-size: 24rpx;
      color: #64748b;
      margin-left: 10rpx;
      margin-bottom: 12rpx;
      display: block;
      font-weight: 500;
    }
  }

  .form-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 60rpx;
    padding: 0 10rpx;

    .remember-me {
      display: flex;
      align-items: center;
      gap: 4rpx;
    }

    .action-text, .forgot-pwd {
      font-size: 24rpx;
      color: #64748b;
    }

    .forgot-pwd:active {
      color: #3cc51f;
    }
  }
}

.social-login {
  margin-top: 60rpx;

  .divider {
    display: flex;
    align-items: center;
    gap: 20rpx;
    margin-bottom: 40rpx;

    .line {
      flex: 1;
      height: 1rpx;
      background: #e2e8f0;
    }

    .divider-text {
      font-size: 22rpx;
      color: #94a3b8;
    }
  }

  .social-icons {
    display: flex;
    justify-content: center;

    .icon-btn {
      width: 90rpx;
      height: 90rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1rpx solid #e2e8f0;
      transition: all 0.3s;

      &:active {
        background: #f1f5f9;
        transform: scale(0.9);
      }
    }
  }
}

.auth-footer {
  margin-top: auto;
  padding: 60rpx 0;
  text-align: center;
  font-size: 22rpx;
  color: #94a3b8;

  .footer-link {
    color: #64748b;
    font-weight: 600;
    margin: 0 8rpx;
  }
  
  .footer-sep {
    margin: 0 4rpx;
  }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.2; }
  100% { transform: scale(1); opacity: 0.5; }
}

@keyframes errorShake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10rpx); }
  75% { transform: translateX(10rpx); }
}

@keyframes inputFocus {
  from { transform: scale(0.98); }
  to { transform: scale(1); }
}
</style>
