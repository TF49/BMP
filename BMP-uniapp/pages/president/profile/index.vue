<template>
  <PresidentLayout :showTabBar="true">
    <view class="profile-page">
      <PresidentBrandLogo floating />
      <view class="header glass-card">
        <view class="avatar-wrap">
          <uni-icons type="person-filled" size="48" color="#ffffff"></uni-icons>
        </view>
        <text class="name">{{ userInfo?.username || '协会会长' }}</text>
        <text class="role">协会会长</text>
      </view>
      <view class="menu-list glass-card">
        <view class="menu-item" @click="goChangePwd">
          <text>修改密码</text>
          <uni-icons type="right" size="16" color="#94a3b8"></uni-icons>
        </view>
        <view class="menu-item" @click="logout">
          <text class="logout-text">退出登录</text>
          <uni-icons type="right" size="16" color="#94a3b8"></uni-icons>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import PresidentBrandLogo from '@/components/president/PresidentBrandLogo.vue'
import { useUserStore } from '@/store/modules/user'
import { safeReLaunch } from '@/utils/safeRoute'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

function goChangePwd() {
  uni.navigateTo({ url: '/pages/president/profile/change-password' })
}

function logout() {
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
</script>

<style lang="scss" scoped>
.profile-page { padding: 120rpx 24rpx 24rpx; }
.header {
  padding: 48rpx 32rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  text-align: center;
}
.avatar-wrap {
  width: 120rpx; height: 120rpx;
  margin: 0 auto 24rpx;
  background: linear-gradient(135deg, #a33e00, #ff6600);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 32rpx rgba(163, 62, 0, 0.28);
}
.name { display: block; font-size: 36rpx; font-weight: 600; color: #1E293B; margin-bottom: 8rpx; }
.role { font-size: 26rpx; color: #475569; }
.menu-list { border-radius: 24rpx; overflow: hidden; }
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid rgba(226,232,240,0.6);
}
.menu-item:last-child { border-bottom: none; }
.logout-text { color: #ef4444; }
</style>
