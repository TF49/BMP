<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">安全设置</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Security Status -->
      <view class="section status-section">
        <view class="status-header">
          <view class="status-icon" :class="securityLevel">
            <uni-icons :type="securityIconType" size="28" :color="securityProgress >= 80 ? '#22c55e' : (securityProgress >= 50 ? '#f59e0b' : '#ef4444')"></uni-icons>
          </view>
          <view class="status-info">
            <text class="status-title">账户安全等级</text>
            <text class="status-level" :class="securityLevel">{{ securityLevelText }}</text>
          </view>
        </view>
        <view class="status-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: securityProgress + '%' }"></view>
          </view>
          <text class="progress-text">{{ securityProgress }}%</text>
        </view>
      </view>

      <!-- Security Options -->
      <view class="section security-section">
        <text class="section-title">账户安全</text>
        
        <view class="security-item" @click="handleChangePassword">
          <view class="item-left">
            <view class="item-icon password-icon">
              <uni-icons type="locked" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="item-info">
              <text class="item-title">修改密码</text>
              <text class="item-desc">定期更换密码更安全</text>
            </view>
          </view>
          <view class="item-right">
            <text class="item-status" :class="{ verified: passwordVerified }">
              {{ passwordVerified ? '已设置' : '未设置' }}
            </text>
            <text class="chevron">›</text>
          </view>
        </view>

        <view class="divider"></view>

        <view class="security-item" @click="handleBindPhone">
          <view class="item-left">
            <view class="item-icon phone-icon">
              <uni-icons type="phone" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="item-info">
              <text class="item-title">绑定手机</text>
              <text class="item-desc">{{ userInfo.phone || '绑定手机号可找回密码' }}</text>
            </view>
          </view>
          <view class="item-right">
            <text class="item-status" :class="{ verified: !!userInfo.phone }">
              {{ userInfo.phone ? '已绑定' : '未绑定' }}
            </text>
            <text class="chevron">›</text>
          </view>
        </view>

        <view class="divider"></view>

        <view class="security-item" @click="handleBindEmail">
          <view class="item-left">
            <view class="item-icon email-icon">
              <uni-icons type="email" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="item-info">
              <text class="item-title">绑定邮箱</text>
              <text class="item-desc">{{ userInfo.email || '绑定邮箱可接收重要通知' }}</text>
            </view>
          </view>
          <view class="item-right">
            <text class="item-status" :class="{ verified: !!userInfo.email }">
              {{ userInfo.email ? '已绑定' : '未绑定' }}
            </text>
            <text class="chevron">›</text>
          </view>
        </view>
      </view>

      <!-- Advanced Security -->
      <view class="section advanced-section">
        <text class="section-title">高级安全</text>
        
        <view class="security-item">
          <view class="item-left">
            <view class="item-icon fingerprint-icon">
              <uni-icons type="person" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="item-info">
              <text class="item-title">指纹/面容登录</text>
              <text class="item-desc">使用生物识别快速登录</text>
            </view>
          </view>
          <switch :checked="settings.biometricLogin" @change="toggleSetting('biometricLogin')" />
        </view>

        <view class="divider"></view>

        <view class="security-item">
          <view class="item-left">
            <view class="item-icon gesture-icon">
              <uni-icons type="hand-up" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="item-info">
              <text class="item-title">手势密码</text>
              <text class="item-desc">设置手势密码保护应用</text>
            </view>
          </view>
          <switch :checked="settings.gesturePassword" @change="toggleSetting('gesturePassword')" />
        </view>

        <view class="divider"></view>

        <view class="security-item">
          <view class="item-left">
            <view class="item-icon lock-icon">
              <uni-icons type="locked" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="item-info">
              <text class="item-title">自动锁定</text>
              <text class="item-desc">离开应用后自动锁定</text>
            </view>
          </view>
          <switch :checked="settings.autoLock" @change="toggleSetting('autoLock')" />
        </view>
      </view>

      <!-- Login Records -->
      <view class="section records-section">
        <text class="section-title">登录记录</text>
        
        <view class="record-item" v-for="(record, index) in loginRecords" :key="index">
          <view class="record-left">
            <view class="record-icon" :class="record.isCurrent ? 'current' : ''">
              <uni-icons :type="record.iconType" size="20" :color="record.isCurrent ? '#3cc51f' : '#475569'"></uni-icons>
            </view>
            <view class="record-info">
              <text class="record-device">{{ record.device }}</text>
              <text class="record-time">{{ record.time }}</text>
            </view>
          </view>
          <text v-if="record.isCurrent" class="current-tag">当前设备</text>
        </view>
      </view>

      <!-- Security Tips -->
      <view class="section tips-section">
        <text class="tips-title">安全建议</text>
        <view class="tips-list">
          <view class="tip-item">
            <view class="tip-icon"><uni-icons type="checkbox-filled" size="16" color="#22c55e"></uni-icons></view>
            <text class="tip-text">定期更换密码，使用强密码</text>
          </view>
          <view class="tip-item">
            <view class="tip-icon"><uni-icons type="checkbox-filled" size="16" color="#22c55e"></uni-icons></view>
            <text class="tip-text">不要在公共设备上保持登录状态</text>
          </view>
          <view class="tip-item">
            <view class="tip-icon"><uni-icons type="checkbox-filled" size="16" color="#22c55e"></uni-icons></view>
            <text class="tip-text">绑定手机和邮箱以便找回账户</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCurrentUser } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'

// 用户信息
const userInfo = reactive({
  phone: '',
  email: ''
})

// 安全设置
const settings = reactive({
  biometricLogin: false,
  gesturePassword: false,
  autoLock: true
})

const passwordVerified = ref(true)
const userStore = useUserStore()

// 登录记录
const loginRecords = ref([
  {
    device: '当前设备 - 微信小程序',
    time: '刚刚',
    iconType: 'phone',
    isCurrent: true
  },
  {
    device: 'iPhone 14 Pro',
    time: '2024-01-20 15:30',
    iconType: 'phone',
    isCurrent: false
  },
  {
    device: 'Windows PC',
    time: '2024-01-18 09:15',
    iconType: 'contact',
    isCurrent: false
  }
])

// 计算安全等级
const securityProgress = computed(() => {
  let progress = 0
  if (passwordVerified.value) progress += 30
  if (userInfo.phone) progress += 25
  if (userInfo.email) progress += 25
  if (settings.biometricLogin || settings.gesturePassword) progress += 20
  return Math.min(progress, 100)
})

const securityLevel = computed(() => {
  if (securityProgress.value >= 80) return 'high'
  if (securityProgress.value >= 50) return 'medium'
  return 'low'
})

const securityLevelText = computed(() => {
  if (securityProgress.value >= 80) return '安全'
  if (securityProgress.value >= 50) return '中等'
  return '较低'
})

const securityIconType = computed(() => {
  if (securityProgress.value >= 80) return 'auth'
  if (securityProgress.value >= 50) return 'info'
  return 'help'
})

// 切换设置
const toggleSetting = (key: keyof typeof settings) => {
  settings[key] = !settings[key]
  saveSettings()
  
  if (key === 'biometricLogin' && settings.biometricLogin) {
    uni.showToast({
      title: '功能开发中',
      icon: 'none'
    })
    settings.biometricLogin = false
  }
  
  if (key === 'gesturePassword' && settings.gesturePassword) {
    uni.showToast({
      title: '功能开发中',
      icon: 'none'
    })
    settings.gesturePassword = false
  }
}

// 保存设置
const saveSettings = () => {
  uni.setStorageSync('security_settings', settings)
}

// 加载设置
const loadSettings = () => {
  try {
    const savedSettings = uni.getStorageSync('security_settings')
    if (savedSettings) {
      Object.assign(settings, savedSettings)
    }
  } catch (error) {
    console.error('加载安全设置失败:', error)
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const user = await getCurrentUser()
    userInfo.phone = user.phone || ''
    userInfo.email = user.email || ''
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 修改密码
const handleChangePassword = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 绑定手机
const handleBindPhone = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 绑定邮箱
const handleBindEmail = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 页面加载时获取数据
onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  loadSettings()
  await loadUserInfo()
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
  margin: 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.section-title {
  display: block;
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
  padding: 24rpx 28rpx 16rpx;
}

.status-section {
  padding: 32rpx 28rpx;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.status-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.high {
    background-color: #e8f5e9;
  }
  
  &.medium {
    background-color: #fff3e0;
  }
  
  &.low {
    background-color: #ffebee;
  }
  
  .icon-text {
    font-size: 32rpx;
  }
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 24rpx;
  color: #666666;
  display: block;
  margin-bottom: 4rpx;
}

.status-level {
  font-size: 28rpx;
  font-weight: bold;
  
  &.high {
    color: #3cc51f;
  }
  
  &.medium {
    color: #ff9800;
  }
  
  &.low {
    color: #ef4444;
  }
}

.status-progress {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background-color: #f3f4f6;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3cc51f 0%, #4ade80 100%);
  border-radius: 6rpx;
  transition: width 0.3s;
}

.progress-text {
  font-size: 24rpx;
  font-weight: bold;
  color: #3cc51f;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  transition: background-color 0.2s;
  
  &:active {
    background-color: #f9fafb;
  }
}

.item-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.item-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.password-icon {
    background-color: #fff3e0;
  }
  
  &.phone-icon {
    background-color: #e3f2fd;
  }
  
  &.email-icon {
    background-color: #fce4ec;
  }
  
  &.fingerprint-icon {
    background-color: #e8f5e9;
  }
  
  &.gesture-icon {
    background-color: #f3e5f5;
  }
  
  &.lock-icon {
    background-color: #e0f2f1;
  }
}

.icon-text {
  font-size: 28rpx;
}

.item-info {
  display: flex;
  flex-direction: column;
}

.item-title {
  font-size: 24rpx;
  font-weight: 500;
  color: #333333;
}

.item-desc {
  font-size: 20rpx;
  color: #999999;
  margin-top: 4rpx;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.item-status {
  font-size: 22rpx;
  color: #999999;
  
  &.verified {
    color: #3cc51f;
  }
}

.chevron {
  font-size: 26rpx;
  color: #999999;
}

.divider {
  height: 1rpx;
  background-color: #f3f4f6;
  margin: 0 28rpx;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;
  
  &:last-child {
    border-bottom: none;
  }
}

.record-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.record-icon {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.current {
    background-color: #e8f5e9;
  }
  
  .icon-text {
    font-size: 24rpx;
  }
}

.record-info {
  display: flex;
  flex-direction: column;
}

.record-device {
  font-size: 24rpx;
  color: #333333;
}

.record-time {
  font-size: 20rpx;
  color: #999999;
  margin-top: 4rpx;
}

.current-tag {
  font-size: 20rpx;
  color: #3cc51f;
  background-color: #e8f5e9;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.tips-section {
  padding: 28rpx;
}

.tips-title {
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
  display: block;
  margin-bottom: 20rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.tip-icon {
  font-size: 24rpx;
  color: #3cc51f;
}

.tip-text {
  font-size: 22rpx;
  color: #666666;
}
</style>
