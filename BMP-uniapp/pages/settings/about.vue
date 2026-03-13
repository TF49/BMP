<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">关于我们</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- App Info -->
      <view class="section app-section">
        <view class="app-logo">
          <uni-icons type="medal" size="48" color="#3cc51f" class="logo-icon"></uni-icons>
        </view>
        <text class="app-name">羽毛球场馆管理系统</text>
        <text class="app-slogan">让运动更便捷</text>
        <text class="app-version">版本 {{ appVersion }}</text>
      </view>

      <!-- Features -->
      <view class="section features-section">
        <text class="section-title">主要功能</text>
        <view class="features-grid">
          <view class="feature-item">
            <view class="feature-icon venue-icon">
              <uni-icons type="location" size="24" color="#3cc51f"></uni-icons>
            </view>
            <text class="feature-name">场馆预订</text>
          </view>
          <view class="feature-item">
            <view class="feature-icon course-icon">
              <uni-icons type="compose" size="24" color="#3cc51f"></uni-icons>
            </view>
            <text class="feature-name">课程预约</text>
          </view>
          <view class="feature-item">
            <view class="feature-icon equipment-icon">
              <uni-icons type="cart" size="24" color="#3cc51f"></uni-icons>
            </view>
            <text class="feature-name">器材租借</text>
          </view>
          <view class="feature-item">
            <view class="feature-icon tournament-icon">
              <uni-icons type="medal" size="24" color="#3cc51f"></uni-icons>
            </view>
            <text class="feature-name">赛事报名</text>
          </view>
        </view>
      </view>

      <!-- Company Info -->
      <view class="section company-section">
        <text class="section-title">公司信息</text>
        <view class="info-item">
          <text class="info-label">公司名称</text>
          <text class="info-value">BMP羽毛球俱乐部</text>
        </view>
        <view class="divider"></view>
        <view class="info-item">
          <text class="info-label">成立时间</text>
          <text class="info-value">2024年</text>
        </view>
        <view class="divider"></view>
        <view class="info-item">
          <text class="info-label">服务范围</text>
          <text class="info-value">全国</text>
        </view>
      </view>

      <!-- Contact -->
      <view class="section contact-section">
        <text class="section-title">联系我们</text>
        <view class="contact-item" @click="handleCall">
          <view class="contact-left">
            <view class="contact-icon phone-icon">
              <uni-icons type="phone" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="contact-info">
              <text class="contact-label">客服电话</text>
              <text class="contact-value">400-888-8888</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="contact-item" @click="handleEmail">
          <view class="contact-left">
            <view class="contact-icon email-icon">
              <uni-icons type="email" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="contact-info">
              <text class="contact-label">客服邮箱</text>
              <text class="contact-value">support@bmp.com</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="contact-item" @click="handleAddress">
          <view class="contact-left">
            <view class="contact-icon address-icon">
              <uni-icons type="location" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="contact-info">
              <text class="contact-label">公司地址</text>
              <text class="contact-value">北京市朝阳区xxx大厦</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>
      </view>

      <!-- Social Media -->
      <view class="section social-section">
        <text class="section-title">关注我们</text>
        <view class="social-grid">
          <view class="social-item" @click="handleWechat">
            <view class="social-icon wechat">
              <uni-icons type="weixin" size="24" color="#3cc51f"></uni-icons>
            </view>
            <text class="social-name">微信公众号</text>
          </view>
          <view class="social-item" @click="handleWeibo">
            <view class="social-icon weibo">
              <uni-icons type="chatbubble" size="24" color="#3cc51f"></uni-icons>
            </view>
            <text class="social-name">官方微博</text>
          </view>
          <view class="social-item" @click="handleDouyin">
            <view class="social-icon douyin">
              <uni-icons type="sound" size="24" color="#3cc51f"></uni-icons>
            </view>
            <text class="social-name">抖音号</text>
          </view>
        </view>
      </view>

      <!-- Legal -->
      <view class="section legal-section">
        <view class="legal-item" @click="handlePrivacyPolicy">
          <text class="legal-text">隐私政策</text>
          <text class="chevron">›</text>
        </view>
        <view class="divider"></view>
        <view class="legal-item" @click="handleUserAgreement">
          <text class="legal-text">用户协议</text>
          <text class="chevron">›</text>
        </view>
        <view class="divider"></view>
        <view class="legal-item" @click="handleLicenses">
          <text class="legal-text">开源许可</text>
          <text class="chevron">›</text>
        </view>
      </view>

      <!-- Check Update -->
      <view class="update-container">
        <button class="update-btn" @click="handleCheckUpdate">
          检查更新
        </button>
      </view>

      <!-- Copyright -->
      <view class="copyright">
        <text class="copyright-text">© 2024 BMP羽毛球俱乐部 版权所有</text>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'

const appVersion = ref('1.0.0')
const userStore = useUserStore()

// 拨打电话
const handleCall = () => {
  uni.makePhoneCall({
    phoneNumber: '4008888888',
    fail: () => {
      uni.showToast({
        title: '拨打失败',
        icon: 'none'
      })
    }
  })
}

// 发送邮件
const handleEmail = () => {
  uni.setClipboardData({
    data: 'support@bmp.com',
    success: () => {
      uni.showToast({
        title: '邮箱已复制',
        icon: 'success'
      })
    }
  })
}

// 查看地址
const handleAddress = () => {
  uni.openLocation({
    latitude: 39.9042,
    longitude: 116.4074,
    name: 'BMP羽毛球俱乐部',
    address: '北京市朝阳区xxx大厦',
    fail: () => {
      uni.showToast({
        title: '打开地图失败',
        icon: 'none'
      })
    }
  })
}

// 微信公众号
const handleWechat = () => {
  uni.showModal({
    title: '关注微信公众号',
    content: '请在微信中搜索"BMP羽毛球"关注我们',
    showCancel: false
  })
}

// 微博
const handleWeibo = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 抖音
const handleDouyin = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 隐私政策
const handlePrivacyPolicy = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 用户协议
const handleUserAgreement = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 开源许可
const handleLicenses = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 检查更新
const handleCheckUpdate = () => {
  uni.showLoading({
    title: '检查中...'
  })
  
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '已是最新版本',
      icon: 'success'
    })
  }, 1500)
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 页面加载
onMounted(() => {
  // 获取版本号
  try {
    const accountInfo = uni.getAccountInfoSync()
    if (accountInfo && accountInfo.miniProgram) {
      appVersion.value = accountInfo.miniProgram.version || '1.0.0'
    }
  } catch (error) {
    console.log('获取版本信息失败')
  }
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

.app-section {
  padding: 60rpx 28rpx;
  text-align: center;
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  color: #ffffff;
}

.app-logo {
  width: 128rpx;
  height: 128rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.logo-icon {
  font-size: 64rpx;
}

.app-name {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 12rpx;
}

.app-slogan {
  font-size: 24rpx;
  opacity: 0.8;
  display: block;
  margin-bottom: 16rpx;
}

.app-version {
  font-size: 22rpx;
  opacity: 0.6;
  display: block;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  padding: 16rpx 28rpx 28rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.feature-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.venue-icon {
    background-color: #e8f5e9;
  }
  
  &.course-icon {
    background-color: #e3f2fd;
  }
  
  &.equipment-icon {
    background-color: #fff3e0;
  }
  
  &.tournament-icon {
    background-color: #fce4ec;
  }
}

.icon-text {
  font-size: 28rpx;
}

.feature-name {
  font-size: 20rpx;
  color: #666666;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 28rpx;
}

.info-label {
  font-size: 24rpx;
  color: #666666;
}

.info-value {
  font-size: 24rpx;
  color: #333333;
}

.divider {
  height: 1rpx;
  background-color: #f3f4f6;
  margin: 0 28rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  transition: background-color 0.2s;
  
  &:active {
    background-color: #f9fafb;
  }
}

.contact-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.contact-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.phone-icon {
    background-color: #e8f5e9;
  }
  
  &.email-icon {
    background-color: #e3f2fd;
  }
  
  &.address-icon {
    background-color: #fff3e0;
  }
}

.contact-info {
  display: flex;
  flex-direction: column;
}

.contact-label {
  font-size: 24rpx;
  font-weight: 500;
  color: #333333;
}

.contact-value {
  font-size: 20rpx;
  color: #999999;
  margin-top: 4rpx;
}

.chevron {
  font-size: 26rpx;
  color: #999999;
}

.social-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  padding: 16rpx 28rpx 28rpx;
}

.social-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.social-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.wechat {
    background-color: #e8f5e9;
  }
  
  &.weibo {
    background-color: #ffebee;
  }
  
  &.douyin {
    background-color: #f3e5f5;
  }
}

.social-name {
  font-size: 22rpx;
  color: #666666;
}

.legal-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 28rpx;
  
  &:active {
    background-color: #f9fafb;
  }
}

.legal-text {
  font-size: 24rpx;
  color: #333333;
}

.update-container {
  padding: 0 24rpx 24rpx;
}

.update-btn {
  width: 100%;
  background-color: #ffffff;
  color: #3cc51f;
  font-size: 28rpx;
  font-weight: 500;
  padding: 24rpx;
  border-radius: 18rpx;
  border: 2rpx solid #3cc51f;
}

.copyright {
  padding: 32rpx;
  text-align: center;
}

.copyright-text {
  font-size: 22rpx;
  color: #999999;
}
</style>
