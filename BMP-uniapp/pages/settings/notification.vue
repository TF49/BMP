<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">通知设置</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Notification Categories -->
      <view class="section notifications-section">
        <view class="notification-category">
          <text class="category-title">预订通知</text>
          <view class="notification-item">
            <text class="notification-text">预约成功通知</text>
            <switch :checked="settings.bookingSuccess" @change="toggleSetting('bookingSuccess')" />
          </view>
          <view class="notification-item">
            <text class="notification-text">预约变更通知</text>
            <switch :checked="settings.bookingChange" @change="toggleSetting('bookingChange')" />
          </view>
          <view class="notification-item">
            <text class="notification-text">预约提醒</text>
            <switch :checked="settings.bookingReminder" @change="toggleSetting('bookingReminder')" />
          </view>
        </view>

        <view class="notification-category">
          <text class="category-title">支付通知</text>
          <view class="notification-item">
            <text class="notification-text">支付成功通知</text>
            <switch :checked="settings.paymentSuccess" @change="toggleSetting('paymentSuccess')" />
          </view>
          <view class="notification-item">
            <text class="notification-text">余额变动通知</text>
            <switch :checked="settings.balanceChange" @change="toggleSetting('balanceChange')" />
          </view>
        </view>

        <view class="notification-category">
          <text class="category-title">系统通知</text>
          <view class="notification-item">
            <text class="notification-text">系统消息</text>
            <switch :checked="settings.systemMessage" @change="toggleSetting('systemMessage')" />
          </view>
          <view class="notification-item">
            <text class="notification-text">活动推送</text>
            <switch :checked="settings.eventPush" @change="toggleSetting('eventPush')" />
          </view>
          <view class="notification-item">
            <text class="notification-text">优惠活动</text>
            <switch :checked="settings.promotion" @change="toggleSetting('promotion')" />
          </view>
        </view>

        <view class="notification-category">
          <text class="category-title">其他设置</text>
          <view class="notification-item">
            <text class="notification-text">接收推送通知</text>
            <switch :checked="settings.pushNotifications" @change="toggleSetting('pushNotifications')" />
          </view>
          <view class="notification-item">
            <text class="notification-text">声音提醒</text>
            <switch :checked="settings.soundAlert" @change="toggleSetting('soundAlert')" />
          </view>
          <view class="notification-item">
            <text class="notification-text">震动提醒</text>
            <switch :checked="settings.vibrationAlert" @change="toggleSetting('vibrationAlert')" />
          </view>
        </view>
      </view>

      <!-- Notification Time -->
      <view class="section time-section">
        <text class="section-title">通知时段</text>
        <view class="time-setting">
          <text class="time-label">免打扰时段</text>
          <view class="time-inputs">
            <picker mode="time" :value="settings.dndStart" @change="onDNDStartChange">
              <view class="time-picker">
                <text class="time-value">{{ settings.dndStart }}</text>
              </view>
            </picker>
            <text class="time-separator">至</text>
            <picker mode="time" :value="settings.dndEnd" @change="onDNDEndChange">
              <view class="time-picker">
                <text class="time-value">{{ settings.dndEnd }}</text>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- Tips -->
      <view class="section tips-section">
        <text class="tips-title">提示</text>
        <text class="tips-text">为了获得最佳体验，建议开启推送通知权限。您可以在手机设置中管理应用的通知权限。</text>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'

// 通知设置
const settings = reactive({
  bookingSuccess: true,
  bookingChange: true,
  bookingReminder: true,
  paymentSuccess: true,
  balanceChange: true,
  systemMessage: true,
  eventPush: true,
  promotion: false,
  pushNotifications: true,
  soundAlert: true,
  vibrationAlert: true,
  dndStart: '22:00',
  dndEnd: '08:00'
})

const userStore = useUserStore()

// 切换设置
const toggleSetting = (key: keyof typeof settings) => {
  settings[key] = !settings[key]
  saveSettings()
}

// 免打扰开始时间变化
const onDNDStartChange = (e: any) => {
  settings.dndStart = e.detail.value
  saveSettings()
}

// 免打扰结束时间变化
const onDNDEndChange = (e: any) => {
  settings.dndEnd = e.detail.value
  saveSettings()
}

// 保存设置
const saveSettings = () => {
  // 保存到本地存储
  uni.setStorageSync('notification_settings', settings)
  console.log('通知设置已保存:', settings)
}

// 加载设置
const loadSettings = () => {
  try {
    const savedSettings = uni.getStorageSync('notification_settings')
    if (savedSettings) {
      Object.assign(settings, savedSettings)
    }
  } catch (error) {
    console.error('加载通知设置失败:', error)
  }
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 页面加载时获取数据
onMounted(() => {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    // 未登录用户重定向到登录页
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  loadSettings()
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

.notifications-section {
  margin: 24rpx;
}

.notification-category {
  margin-bottom: 32rpx;
}

.category-title {
  display: block;
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
  padding-left: 28rpx;
}

.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.notification-text {
  font-size: 24rpx;
  color: #333333;
}

.time-section {
  margin: 0 24rpx 20rpx;
  
  .section-title {
    display: block;
    font-size: 24rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 20rpx;
    padding-left: 28rpx;
  }
}

.time-setting {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.time-label {
  font-size: 24rpx;
  color: #333333;
}

.time-inputs {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.time-picker {
  padding: 8rpx 16rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
}

.time-value {
  font-size: 24rpx;
  color: #333333;
}

.time-separator {
  font-size: 24rpx;
  color: #999999;
}

.tips-section {
  margin: 0 24rpx 20rpx;
  padding: 28rpx;
  
  .tips-title {
    display: block;
    font-size: 24rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 16rpx;
  }
  
  .tips-text {
    font-size: 22rpx;
    color: #999999;
    line-height: 1.5;
  }
}
</style>