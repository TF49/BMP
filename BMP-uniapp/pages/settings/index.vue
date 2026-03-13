<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">设置</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Account Section -->
      <view class="section account-section">
        <view class="account-header">
          <view class="avatar">
            <uni-icons type="person" size="28" color="#94a3b8" class="avatar-icon"></uni-icons>
          </view>
          <view class="account-info">
            <text class="account-name">{{ userInfo.username || '未登录' }}</text>
            <text class="account-level">{{ userInfo.level || '普通用户' }}</text>
          </view>
        </view>
      </view>

      <!-- Settings List -->
      <view class="section settings-list">
        <view class="setting-item" @click="goToAccountSettings">
          <view class="setting-left">
            <view class="setting-icon account-icon">
              <uni-icons type="person" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="setting-label">账户设置</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="setting-item" @click="goToNotificationSettings">
          <view class="setting-left">
            <view class="setting-icon notification-icon">
              <uni-icons type="chatbubble" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="setting-label">通知设置</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="setting-item" @click="goToPrivacySettings">
          <view class="setting-left">
            <view class="setting-icon privacy-icon">
              <uni-icons type="locked" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="setting-label">隐私设置</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="setting-item" @click="goToSecuritySettings">
          <view class="setting-left">
            <view class="setting-icon security-icon">
              <uni-icons type="auth" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="setting-label">安全设置</text>
          </view>
          <text class="chevron">›</text>
        </view>
      </view>

      <!-- Other Settings -->
      <view class="section other-settings">
        <view class="setting-item" @click="goToAbout">
          <view class="setting-left">
            <view class="setting-icon about-icon">
              <uni-icons type="info" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="setting-label">关于我们</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="setting-item" @click="goToHelp">
          <view class="setting-left">
            <view class="setting-icon help-icon">
              <uni-icons type="help" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="setting-label">帮助与反馈</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="setting-item" @click="goToThemeSettings">
          <view class="setting-left">
            <view class="setting-icon theme-icon">
              <uni-icons type="color" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="setting-label">主题设置</text>
          </view>
          <switch :checked="isDarkMode" @change="toggleTheme" />
        </view>
      </view>

      <!-- Logout Button -->
      <view class="logout-container">
        <button class="logout-btn" @click="handleLogout">
          <uni-icons type="redo" size="18" color="#ef4444" class="logout-icon"></uni-icons>
          <text>退出登录</text>
        </button>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { safeReLaunch } from '@/utils/safeRoute'
import { useThemeStore } from '@/store/modules/theme'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCurrentUser } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const userInfo = ref({
  username: '',
  level: '',
  avatar: ''
})

const userStore = useUserStore()
const themeStore = useThemeStore()
const isDarkMode = ref(false)

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const user = await getCurrentUser()
    userInfo.value = {
      username: user.username,
      level: user.role || '普通用户',
      avatar: user.avatar || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 切换主题
const toggleTheme = (e: any) => {
  if (e.detail.value) {
    themeStore.setMode('dark')
  } else {
    themeStore.setMode('light')
  }
  isDarkMode.value = e.detail.value
}

// 跳转到账户设置
const goToAccountSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/account'
  })
}

// 跳转到通知设置
const goToNotificationSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/notification'
  })
}

// 跳转到隐私设置
const goToPrivacySettings = () => {
  uni.navigateTo({
    url: '/pages/settings/privacy'
  })
}

// 跳转到安全设置
const goToSecuritySettings = () => {
  uni.navigateTo({
    url: '/pages/settings/security'
  })
}

// 跳转到关于页面
const goToAbout = () => {
  uni.navigateTo({
    url: '/pages/settings/about'
  })
}

// 跳转到帮助页面
const goToHelp = () => {
  uni.navigateTo({
    url: '/pages/settings/help'
  })
}

// 跳转到主题设置
const goToThemeSettings = () => {
  // 主题设置已在切换开关中处理
}

// 处理退出登录
const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        safeReLaunch('/pages/login/login', '/pages/login/login')
      }
    }
  })
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 页面加载时获取数据
onMounted(async () => {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    // 未登录用户重定向到登录页
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  await loadUserInfo()
  isDarkMode.value = themeStore.isDark
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.header {
  background-color: #ffffff;
  padding: 20rpx 28rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  border-bottom: 1rpx solid #e6e6e6;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-icon {
  font-size: 40rpx;
  color: #333333;
  font-weight: bold;
  width: 56rpx;
}

.header-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
  text-align: center;
}

.header-placeholder {
  width: 56rpx;
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
}

.section {
  background-color: #ffffff;
  margin-bottom: 20rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.account-section {
  padding: 40rpx 28rpx;
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  color: #ffffff;
  margin: 24rpx;
  border-radius: 24rpx;
}

.account-header {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
}

.avatar-icon {
  font-size: 48rpx;
  color: rgba(255, 255, 255, 0.8);
}

.account-info {
  flex: 1;
}

.account-name {
  font-size: 28rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.account-level {
  font-size: 22rpx;
  opacity: 0.8;
  display: block;
}

.settings-list, .other-settings {
  margin: 0 24rpx 20rpx;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  transition: background-color 0.2s;
  
  &:active {
    background-color: #f9fafb;
  }
}

.setting-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.setting-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  
  &.account-icon {
    background-color: #3cc51f;
  }
  
  &.notification-icon {
    background-color: #ff9800;
  }
  
  &.privacy-icon {
    background-color: #2196f3;
  }
  
  &.security-icon {
    background-color: #9c27b0;
  }
  
  &.about-icon {
    background-color: #673ab7;
  }
  
  &.help-icon {
    background-color: #ff5722;
  }
  
  &.theme-icon {
    background-color: #607d8b;
  }
}

.icon-text {
  font-size: 28rpx;
}

.setting-label {
  font-size: 24rpx;
  font-weight: 500;
  color: #333333;
}

.divider {
  height: 1rpx;
  background-color: #f3f4f6;
  margin: 0 28rpx;
}

.chevron {
  font-size: 26rpx;
  color: #999999;
}

.logout-container {
  padding: 0 24rpx 40rpx;
}

.logout-btn {
  width: 100%;
  background-color: #ffffff;
  color: #ef4444;
  font-size: 24rpx;
  font-weight: 500;
  padding: 20rpx;
  border-radius: 18rpx;
  border: none;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.logout-icon {
  font-size: 28rpx;
}
</style>