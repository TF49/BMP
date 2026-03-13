<template>
  <MobileLayout backgroundColor="bg-muted">
    <!-- Header -->
    <view class="profile-header">
      <view class="header-content">
        <view class="user-info">
          <view class="avatar">
            <uni-icons type="person" size="28" color="#94a3b8" class="avatar-icon"></uni-icons>
          </view>
          <view class="user-details">
            <text class="user-name">微信用户</text>
            <view class="user-id">
              <text>ID: 8839201</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>
      </view>

      <!-- Stats Card -->
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-value">120</text>
          <text class="stat-label">余额</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">12</text>
          <text class="stat-label">积分</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">2</text>
          <text class="stat-label">优惠券</text>
        </view>
      </view>
    </view>

    <view class="profile-content">
      <!-- Member Card -->
      <view class="member-card">
        <view class="member-info">
          <text class="member-title">开通尊享会员</text>
          <text class="member-desc">享订场8折优惠，优先预订权</text>
        </view>
        <button class="member-btn" @click="handleOpenMember">立即开通</button>
      </view>

      <!-- Menu List -->
      <view class="menu-list">
        <view 
          v-for="(item, index) in menuItems" 
          :key="index"
          class="menu-item"
          :class="{ last: index === menuItems.length - 1 }"
          @click="handleMenuClick(item.path)"
        >
          <view class="menu-left">
            <view class="menu-icon" :class="item.iconClass">
              <uni-icons :type="item.iconType" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="menu-label">{{ item.label }}</text>
          </view>
          <view class="menu-right">
            <text v-if="item.value" class="menu-value">{{ item.value }}</text>
            <text class="chevron">›</text>
          </view>
        </view>
      </view>

      <button class="logout-btn" @click="handleLogout">
        <uni-icons type="redo" size="18" color="#ef4444" class="logout-icon"></uni-icons>
        <text>退出登录</text>
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { safeReLaunch } from '@/utils/safeRoute'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCurrentUser } from '@/api/auth'
import { getMemberInfo } from '@/api/member'

const userStore = useUserStore()
const menuItems = ref([
  { label: '预订记录', iconType: 'calendar', iconClass: 'icon-clock', value: '', path: '/pages/booking/list' },
  { label: '充值中心', iconType: 'wallet', iconClass: 'icon-wallet', value: '余额 ¥120', path: '/pages/recharge/index' },
  { label: '消费记录', iconType: 'bars', iconClass: 'icon-chart', value: '', path: '/pages/profile/records' },
  { label: '会员信息', iconType: 'person', iconClass: 'icon-user', value: '普通会员', path: '/pages/profile/member' },
  { label: '个人信息', iconType: 'compose', iconClass: 'icon-edit', value: '', path: '/pages/profile/info' },
  { label: '设置', iconType: 'gear', iconClass: 'icon-settings', value: '', path: '/pages/settings/index' },
])

// 用户信息
const userInfo = ref({
  username: '微信用户',
  balance: 0,
  points: 0,
  coupons: 0,
  memberId: 0
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 获取当前用户信息
    const user = await getCurrentUser()
    
    // 获取会员信息
    const memberInfo = await getMemberInfo(user.id)
    
    // 更新菜单项（仅显示普通用户需要的功能）
    menuItems.value = [
      { label: '预订记录', iconType: 'calendar', iconClass: 'icon-clock', value: '', path: '/pages/booking/list' },
      { label: '充值中心', iconType: 'wallet', iconClass: 'icon-wallet', value: `余额 ¥${memberInfo.balance || 0}`, path: '/pages/recharge/index' },
      { label: '消费记录', iconType: 'bars', iconClass: 'icon-chart', value: '', path: '/pages/profile/records' },
      { label: '会员信息', iconType: 'person', iconClass: 'icon-user', value: memberInfo.memberType || '普通会员', path: '/pages/profile/member' },
      { label: '个人信息', iconType: 'compose', iconClass: 'icon-edit', value: '', path: '/pages/profile/info' },
      { label: '设置', iconType: 'gear', iconClass: 'icon-settings', value: '', path: '/pages/settings/index' },
    ]
    
    // 更新用户信息（仅显示普通用户需要的信息）
    userInfo.value = {
      username: user.username,
      balance: memberInfo.balance || 0,
      points: 0, // 积分功能暂不实现
      coupons: 0, // 卡券功能暂不实现
      memberId: user.id
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    uni.showToast({
      title: '加载用户信息失败',
      icon: 'none'
    })
  }
}

const handleOpenMember = () => {
  uni.navigateTo({
    url: '/pages/profile/member'
  })
}

const handleMenuClick = (path: string) => {
  if (path) {
    uni.navigateTo({
      url: path
    })
  } else {
    uni.showToast({
      title: '功能开发中',
      icon: 'none'
    })
  }
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // 清除登录状态（使用store的方法）
        userStore.logout()
        safeReLaunch('/pages/login/login', '/pages/login/login')
      }
    }
  })
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
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.profile-header {
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  padding-top: 72rpx;
  padding-bottom: 96rpx;
  padding-left: 36rpx;
  padding-right: 36rpx;
  border-radius: 0 0 48rpx 48rpx;
  box-shadow: 0 2rpx 6rpx rgba(60, 197, 31, 0.3);
  position: relative;
}

.header-content {
  margin-bottom: 60rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  background-color: #ffffff;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 16rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.avatar-icon {
  flex-shrink: 0;
}

.user-details {
  flex: 1;
  color: #ffffff;
}

.user-name {
  font-size: 28rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 6rpx;
}

.user-id {
  display: inline-block;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 6rpx 12rpx;
  border-radius: 9999rpx;
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.8);
}

.chevron {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
}

.stats-card {
  position: absolute;
  bottom: -60rpx;
  left: 24rpx;
  right: 24rpx;
  background-color: $bg-white;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  border: 1rpx solid $border-color;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.stat-value {
  font-size: 28rpx;
  font-weight: bold;
  color: $text-color;
}

.stat-label {
  font-size: 20rpx;
  color: $text-color-secondary;
}

.stat-divider {
  width: 2rpx;
  height: 48rpx;
  background-color: $border-color;
}

.profile-content {
  margin-top: 88rpx;
  padding: 24rpx 28rpx;
  padding-bottom: 180rpx;
}

.member-card {
  background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
  border-radius: 18rpx;
  padding: 24rpx 28rpx;
  margin-bottom: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
}

.member-info {
  flex: 1;
}

.member-title {
  font-size: 24rpx;
  font-weight: bold;
  color: #fbbf24;
  display: block;
  margin-bottom: 6rpx;
}

.member-desc {
  font-size: 20rpx;
  color: rgba(251, 191, 36, 0.6);
  display: block;
}

.member-btn {
  background-color: #fbbf24;
  color: #1f2937;
  font-size: 20rpx;
  font-weight: bold;
  padding: 8rpx 20rpx;
  border-radius: 9999rpx;
  border: none;
}

.menu-list {
  background-color: $bg-white;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid $border-color;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid $border-color;
  transition: background-color 0.2s ease;
  
  &:active {
    background-color: $muted-bg;
  }
  
  &.last {
    border-bottom: none;
  }
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.menu-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background-color: $muted-bg;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-label {
  font-size: 24rpx;
  font-weight: 500;
  color: $text-color;
}

.menu-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.menu-value {
  font-size: 20rpx;
  color: $text-color-secondary;
}

.logout-btn {
  width: 100%;
  background-color: $bg-white;
  color: $error-color;
  font-size: 24rpx;
  font-weight: 500;
  padding: 20rpx;
  border-radius: 18rpx;
  border: 1rpx solid $border-color;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  transition: background-color 0.2s ease, opacity 0.2s ease;
}

.logout-btn:active {
  opacity: 0.9;
}

.logout-icon {
  flex-shrink: 0;
}
</style>
