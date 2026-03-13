<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">隐私设置</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Privacy Options -->
      <view class="section privacy-section">
        <view class="privacy-category">
          <text class="category-title">个人信息可见性</text>
          <view class="privacy-item">
            <view class="privacy-left">
              <text class="privacy-text">展示真实姓名</text>
              <text class="privacy-desc">其他用户可以看到您的真实姓名</text>
            </view>
            <switch :checked="settings.showRealName" @change="toggleSetting('showRealName')" />
          </view>
          <view class="privacy-item">
            <view class="privacy-left">
              <text class="privacy-text">展示手机号</text>
              <text class="privacy-desc">其他用户可以看到您的手机号码</text>
            </view>
            <switch :checked="settings.showPhone" @change="toggleSetting('showPhone')" />
          </view>
          <view class="privacy-item">
            <view class="privacy-left">
              <text class="privacy-text">展示预约记录</text>
              <text class="privacy-desc">其他用户可以看到您的预约记录</text>
            </view>
            <switch :checked="settings.showBookingHistory" @change="toggleSetting('showBookingHistory')" />
          </view>
        </view>

        <view class="privacy-category">
          <text class="category-title">位置与数据</text>
          <view class="privacy-item">
            <view class="privacy-left">
              <text class="privacy-text">允许获取位置</text>
              <text class="privacy-desc">用于推荐附近的场馆和服务</text>
            </view>
            <switch :checked="settings.allowLocation" @change="toggleSetting('allowLocation')" />
          </view>
          <view class="privacy-item">
            <view class="privacy-left">
              <text class="privacy-text">个性化推荐</text>
              <text class="privacy-desc">根据您的使用习惯推荐内容</text>
            </view>
            <switch :checked="settings.personalizedRecommendation" @change="toggleSetting('personalizedRecommendation')" />
          </view>
          <view class="privacy-item">
            <view class="privacy-left">
              <text class="privacy-text">数据分析</text>
              <text class="privacy-desc">帮助我们改进产品体验</text>
            </view>
            <switch :checked="settings.dataAnalytics" @change="toggleSetting('dataAnalytics')" />
          </view>
        </view>

        <view class="privacy-category">
          <text class="category-title">社交设置</text>
          <view class="privacy-item">
            <view class="privacy-left">
              <text class="privacy-text">允许被搜索</text>
              <text class="privacy-desc">其他用户可以通过搜索找到您</text>
            </view>
            <switch :checked="settings.allowSearch" @change="toggleSetting('allowSearch')" />
          </view>
          <view class="privacy-item">
            <view class="privacy-left">
              <text class="privacy-text">接收私信</text>
              <text class="privacy-desc">允许其他用户向您发送私信</text>
            </view>
            <switch :checked="settings.allowMessage" @change="toggleSetting('allowMessage')" />
          </view>
        </view>
      </view>

      <!-- Data Management -->
      <view class="section data-section">
        <text class="section-title">数据管理</text>
        <view class="data-item" @click="handleClearCache">
          <view class="data-left">
            <view class="data-icon cache-icon">
              <uni-icons type="trash" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="data-info">
              <text class="data-title">清除缓存</text>
              <text class="data-desc">当前缓存: {{ cacheSize }}</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="data-item" @click="handleExportData">
          <view class="data-left">
            <view class="data-icon export-icon">
              <uni-icons type="upload" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="data-info">
              <text class="data-title">导出个人数据</text>
              <text class="data-desc">下载您的个人数据副本</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="data-item danger" @click="handleDeleteAccount">
          <view class="data-left">
            <view class="data-icon delete-icon">
              <uni-icons type="info" size="20" color="#ef4444"></uni-icons>
            </view>
            <view class="data-info">
              <text class="data-title danger-text">注销账户</text>
              <text class="data-desc">永久删除您的账户和所有数据</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>
      </view>

      <!-- Privacy Policy -->
      <view class="section policy-section">
        <view class="policy-item" @click="handleViewPrivacyPolicy">
          <text class="policy-text">查看隐私政策</text>
          <text class="chevron">›</text>
        </view>
        <view class="divider"></view>
        <view class="policy-item" @click="handleViewUserAgreement">
          <text class="policy-text">查看用户协议</text>
          <text class="chevron">›</text>
        </view>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'

// 隐私设置
const settings = reactive({
  showRealName: false,
  showPhone: false,
  showBookingHistory: true,
  allowLocation: true,
  personalizedRecommendation: true,
  dataAnalytics: true,
  allowSearch: true,
  allowMessage: true
})

const cacheSize = ref('0 KB')
const userStore = useUserStore()

// 切换设置
const toggleSetting = (key: keyof typeof settings) => {
  settings[key] = !settings[key]
  saveSettings()
}

// 保存设置
const saveSettings = () => {
  uni.setStorageSync('privacy_settings', settings)
  console.log('隐私设置已保存:', settings)
}

// 加载设置
const loadSettings = () => {
  try {
    const savedSettings = uni.getStorageSync('privacy_settings')
    if (savedSettings) {
      Object.assign(settings, savedSettings)
    }
  } catch (error) {
    console.error('加载隐私设置失败:', error)
  }
}

// 计算缓存大小
const calculateCacheSize = () => {
  try {
    const info = uni.getStorageInfoSync()
    const sizeKB = info.currentSize
    if (sizeKB > 1024) {
      cacheSize.value = (sizeKB / 1024).toFixed(2) + ' MB'
    } else {
      cacheSize.value = sizeKB + ' KB'
    }
  } catch (error) {
    cacheSize.value = '未知'
  }
}

// 清除缓存
const handleClearCache = () => {
  uni.showModal({
    title: '确认清除',
    content: '确定要清除所有缓存数据吗？这不会影响您的登录状态。',
    success: (res) => {
      if (res.confirm) {
        try {
          // 保存重要数据
          const token = uni.getStorageSync('token')
          const userInfo = uni.getStorageSync('userInfo')
          
          // 清除所有缓存
          uni.clearStorageSync()
          
          // 恢复重要数据
          if (token) uni.setStorageSync('token', token)
          if (userInfo) uni.setStorageSync('userInfo', userInfo)
          
          uni.showToast({
            title: '缓存已清除',
            icon: 'success'
          })
          
          calculateCacheSize()
        } catch (error) {
          uni.showToast({
            title: '清除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 导出数据
const handleExportData = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 注销账户
const handleDeleteAccount = () => {
  uni.showModal({
    title: '确认注销',
    content: '注销后您的所有数据将被永久删除，且无法恢复。确定要继续吗？',
    confirmColor: '#ef4444',
    success: (res) => {
      if (res.confirm) {
        uni.showModal({
          title: '再次确认',
          content: '请再次确认您要注销账户，此操作不可撤销！',
          confirmColor: '#ef4444',
          success: (res2) => {
            if (res2.confirm) {
              uni.showToast({
                title: '功能开发中',
                icon: 'none'
              })
            }
          }
        })
      }
    }
  })
}

// 查看隐私政策
const handleViewPrivacyPolicy = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 查看用户协议
const handleViewUserAgreement = () => {
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
onMounted(() => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  loadSettings()
  calculateCacheSize()
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

.privacy-category {
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.category-title, .section-title {
  display: block;
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
  padding: 24rpx 28rpx 16rpx;
}

.privacy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.privacy-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.privacy-text {
  font-size: 24rpx;
  color: #333333;
}

.privacy-desc {
  font-size: 20rpx;
  color: #999999;
}

.data-section {
  padding-bottom: 0;
}

.data-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  transition: background-color 0.2s;
  
  &:active {
    background-color: #f9fafb;
  }
  
  &.danger {
    .data-title {
      color: #ef4444;
    }
  }
}

.data-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.data-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.cache-icon {
    background-color: #e3f2fd;
  }
  
  &.export-icon {
    background-color: #e8f5e9;
  }
  
  &.delete-icon {
    background-color: #ffebee;
  }
}

.icon-text {
  font-size: 28rpx;
}

.data-info {
  display: flex;
  flex-direction: column;
}

.data-title {
  font-size: 24rpx;
  font-weight: 500;
  color: #333333;
  
  &.danger-text {
    color: #ef4444;
  }
}

.data-desc {
  font-size: 20rpx;
  color: #999999;
  margin-top: 4rpx;
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

.policy-section {
  padding: 0;
}

.policy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  
  &:active {
    background-color: #f9fafb;
  }
}

.policy-text {
  font-size: 24rpx;
  color: #333333;
}
</style>
