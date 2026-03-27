<template>
  <view class="profile-page">
    <!-- 顶部橙色渐变区域 -->
    <view class="profile-header">
      <view class="header-content">
        <view class="user-row">
          <view class="avatar-wrap">
            <text class="avatar-emoji">👤</text>
          </view>
          <view class="user-info">
            <text class="user-name">{{ userInfo.username || '微信用户' }}</text>
            <view class="user-id-tag">
              <text>ID: {{ userInfo.memberId || '8839201' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 悬浮统计卡片 -->
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-value">{{ userInfo.balance }}</text>
          <text class="stat-label">余额</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ userInfo.points }}</text>
          <text class="stat-label">积分</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ userInfo.coupons }}</text>
          <text class="stat-label">优惠券</text>
        </view>
      </view>
    </view>

    <view class="profile-main">
      <!-- 尊享会员卡 -->
      <view class="vip-banner" @click="handleOpenMember">
        <view class="vip-info">
          <text class="vip-title">开通尊享会员</text>
          <text class="vip-desc">享订场 8 折优惠 · 优先预订权</text>
        </view>
        <view class="vip-btn">
          <text>立即开通</text>
        </view>
      </view>

      <!-- 菜单列表组 -->
      <view class="menu-group">
        <view 
          v-for="(item, index) in menuItems" 
          :key="index"
          class="menu-item"
          @click="handleMenuClick(item.path)"
        >
          <view class="menu-left">
            <view class="menu-icon-box">
              <uni-icons :type="item.iconType" size="20" color="#FF7A00"></uni-icons>
            </view>
            <text class="menu-label">{{ item.label }}</text>
          </view>
          <view class="menu-right">
            <text v-if="item.value" class="menu-value">{{ item.value }}</text>
            <text class="chevron">›</text>
          </view>
        </view>
      </view>

      <!-- 退出登录按钮 -->
      <view class="logout-section">
        <button class="logout-btn" @click="handleLogout">
          <text>退出登录</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { safeReLaunch } from '@/utils/safeRoute'
import { getCurrentUser } from '@/api/auth'
import { getMemberInfo } from '@/api/member'

const userStore = useUserStore()
const menuItems = ref([
  { label: '预订记录', iconType: 'calendar', value: '', path: '/pages/booking/list' },
  { label: '充值中心', iconType: 'wallet', value: '余额 ¥120', path: '/pages/recharge/index' },
  { label: '消费记录', iconType: 'bars', value: '', path: '/pages/profile/records' },
  { label: '会员信息', iconType: 'person', value: '普通会员', path: '/pages/profile/member' },
  { label: '个人信息', iconType: 'compose', value: '', path: '/pages/profile/info' },
  { label: '设置', iconType: 'gear', value: '', path: '/pages/settings/index' },
])

const userInfo = ref({
  username: '微信用户',
  balance: 0,
  points: 0,
  coupons: 0,
  memberId: 0
})

const loadUserInfo = async () => {
  try {
    const user = await getCurrentUser()
    const memberInfo = await getMemberInfo(user.id)
    
    userInfo.value = {
      username: user.username,
      balance: memberInfo.balance || 0,
      points: 0,
      coupons: 0,
      memberId: user.id
    }
    
    menuItems.value[1].value = `余额 ¥${memberInfo.balance || 0}`
    menuItems.value[3].value = memberInfo.memberType || '普通会员'
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const handleOpenMember = () => {
  uni.navigateTo({ url: '/pages/profile/member' })
}

const handleMenuClick = (path: string) => {
  if (path) {
    uni.navigateTo({ url: path })
  } else {
    uni.showToast({ title: '功能开发中', icon: 'none' })
  }
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#FF7A00',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        safeReLaunch('/pages/login/login', '/pages/login/login')
      }
    }
  })
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  await loadUserInfo()
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.profile-page {
  min-height: 100vh;
  background-color: #F8F9FA;
}

.profile-header {
  position: relative;
  height: 520rpx;
  background: linear-gradient(135deg, #FF9500 0%, #FF5E00 100%);
  padding: calc(80rpx + env(safe-area-inset-top)) 48rpx 0;
  border-bottom-left-radius: 64rpx;
  border-bottom-right-radius: 64rpx;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.avatar-wrap {
  width: 144rpx;
  height: 144rpx;
  background: #FFFFFF;
  border-radius: 72rpx;
  border: 8rpx solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.avatar-emoji {
  font-size: 64rpx;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 44rpx;
  font-weight: bold;
  color: #FFFFFF;
  margin-bottom: 12rpx;
  display: block;
}

.user-id-tag {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  padding: 6rpx 20rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

.stats-card {
  position: absolute;
  left: 48rpx;
  right: 48rpx;
  bottom: -80rpx;
  height: 200rpx;
  background: #FFFFFF;
  border-radius: 48rpx;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.06);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #1F1F1F;
}

.stat-label {
  font-size: 24rpx;
  color: #9E9E9E;
}

.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background: #F0F0F0;
}

.profile-main {
  margin-top: 120rpx;
  padding: 0 40rpx 60rpx;
}

.vip-banner {
  height: 160rpx;
  background: #1F1F1F;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.15);
}

.vip-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #FFD700;
  display: block;
  margin-bottom: 8rpx;
}

.vip-desc {
  font-size: 22rpx;
  color: rgba(255, 215, 0, 0.6);
}

.vip-btn {
  background: #FFD700;
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
  font-size: 24rpx;
  font-weight: bold;
  color: #1F1F1F;
}

.menu-group {
  background: #FFFFFF;
  border-radius: 48rpx;
  border: 2rpx solid #F0F0F0;
  overflow: hidden;
  margin-bottom: 40rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 36rpx 40rpx;
  border-bottom: 2rpx solid #F8F9FA;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: #FDFDFD;
  }
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.menu-icon-box {
  width: 64rpx;
  height: 64rpx;
  background: #FFF8F0;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-label {
  font-size: 30rpx;
  font-weight: 500;
  color: #1F1F1F;
}

.menu-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.menu-value {
  font-size: 26rpx;
  color: #BDBDBD;
}

.chevron {
  font-size: 36rpx;
  color: #E0E0E0;
}

.logout-section {
  margin-top: 20rpx;
}

.logout-btn {
  height: 112rpx;
  background: #FFFFFF;
  border-radius: 40rpx;
  border: 2rpx solid #F0F0F0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF3B30;
  font-size: 30rpx;
  font-weight: bold;

  &::after {
    border: none;
  }

  &:active {
    background-color: #FFF5F5;
  }
}
</style>
