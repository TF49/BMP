<template>
  <view class="home-page bg-surface text-on-surface min-h-screen pb-32">
    <!-- TopAppBar Shell -->
    <view class="top-app-bar w-full fixed top-0 z-50 transition-colors flex justify-between items-center bg-slate-50 dark:bg-zinc-950" :style="{ paddingTop: `calc(${statusBarHeight}px + 16rpx)`, paddingBottom: '20rpx', paddingLeft: '32rpx', paddingRight: menuButtonWidth ? `${menuButtonWidth}px` : '32rpx' }">
      <view class="flex items-center gap-3">
        <view class="avatar-wrapper rounded-full overflow-hidden border-2 border-primary-container scale-102 transition-transform">
          <image class="avatar-img w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCz6-_Na5le1ndz415h4dOrKUk0VB5MsyBTZlK07lH3g5AwqzmTXQQoLKYCqetEuXGRMMviNpptpoDvnxuNvSETOAfdps3CmIHSRlT4SodF9unCGHZ2ySvy6BGYGc6FQwvL4ISGpYHNPDgWbpbHIQBw4qZgBrRyIvlhkS0w1V3hj69IuWS8QyjTJROHRFlCcS35AeuvTDM0P0DCqHr0BT8KKnawpcCKTp8sutGtA3kjtk02NJLQ5yxkdaLuBLAKuY8cyUzTH8B4XDVh" mode="aspectFill" />
        </view>
        <text class="brand-title font-bold tracking-tighter text-orange-600 dark:text-orange-500 uppercase">KINETIC LOGIC</text>
      </view>
      <view class="notification-wrapper relative group cursor-pointer transition-colors active:scale-95 duration-200">
        <text class="icon-notifications text-orange-600 dark:text-orange-500">🔔</text>
        <view class="notification-dot absolute"></view>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll space-y-8" :style="{ paddingTop: `calc(${statusBarHeight}px + 120rpx)`, paddingBottom: '280rpx' }">
      <!-- Status Bar / Quick Access Card -->
      <view class="status-grid grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <view class="balance-card bg-primary-container p-6 rounded-xl flex flex-col justify-between relative overflow-hidden group hover:scale-102 transition-transform duration-300">
          <view class="bg-icon-wrapper absolute -right-4 -top-4 opacity-10 group-hover:rotate-12 transition-transform duration-500">
            <text class="bg-icon text-[120px]">💰</text>
          </view>
          <text class="balance-label text-on-primary-container/80 text-xs font-bold tracking-widest uppercase">账户余额</text>
          <view class="balance-value mt-4">
            <text class="text-on-primary-container text-4xl font-bold">¥2,480.00</text>
          </view>
          <view class="vip-status mt-4 flex items-center gap-2">
            <text class="vip-badge bg-on-primary-container text-primary-container px-2 py-0.5 rounded-full text-[10px] font-black uppercase">VIP会员</text>
            <text class="vip-date text-on-primary-container text-[10px] font-medium opacity-80 uppercase tracking-wider">有效期至 2024.12.31</text>
          </view>
        </view>
        <view class="reminder-card md:col-span-2 bg-surface-container-lowest p-6 rounded-xl flex items-center group hover:bg-surface-container-low transition-colors duration-300 justify-center">
          <view class="reminder-content space-y-1 text-center flex flex-col items-center">
            <text class="reminder-label text-secondary text-xs font-bold tracking-widest uppercase block">预约提醒</text>
            <text class="reminder-title text-on-surface text-xl font-bold">Court 01 @ 14:00</text>
            <text class="reminder-desc text-secondary text-sm">距离开始还有 45 分钟</text>
          </view>
        </view>
      </view>

      <!-- Main Features Grid (King Zone) -->
      <view class="core-services mb-8">
        <view class="section-header flex justify-between items-end mb-6">
          <text class="font-headline text-2xl font-bold tracking-tight text-on-surface uppercase inline-block">核心服务</text>
          <view class="header-line h-1 w-12 bg-primary-container rounded-full"></view>
        </view>
        <view class="services-grid grid grid-cols-2 lg:grid-cols-4 gap-4">
          <view class="service-card cursor-pointer" @tap="navigateTo('/pages/booking/list')">
            <view class="service-icon-bg">
              <text class="service-icon">🏸</text>
            </view>
            <text class="service-name">场地预约</text>
          </view>
          <view class="service-card cursor-pointer" @tap="navigateTo('/pages/course/list')">
            <view class="service-icon-bg">
              <text class="service-icon">👨‍🏫</text>
            </view>
            <text class="service-name">教练课程</text>
          </view>
          <view class="service-card cursor-pointer" @tap="navigateTo('/pages/tournament/list')">
            <view class="service-icon-bg">
              <text class="service-icon">🏆</text>
            </view>
            <text class="service-name">赛事报名</text>
          </view>
          <view class="service-card cursor-pointer" @tap="navigateTo('/pages/equipment/list')">
            <view class="service-icon-bg">
              <text class="service-icon">🎒</text>
            </view>
            <text class="service-name">器材租借</text>
          </view>
        </view>
      </view>

      <!-- Secondary Features Small Grid -->
      <view class="management-services mb-8">
        <view class="section-header flex justify-between items-end mb-6">
          <text class="font-headline text-lg font-bold tracking-tight text-on-surface uppercase">管理与服务</text>
          <text class="view-all text-primary text-xs font-bold uppercase tracking-widest cursor-pointer hover:underline">查看全部</text>
        </view>
        <view class="mini-services-grid grid grid-cols-4 md:grid-cols-8 gap-4">
          <view class="mini-service-card" @tap="navigateTo('/pages/profile/index')">
            <view class="mini-icon-bg"><text>👤</text></view><text>会员中心</text>
          </view>
          <view class="mini-service-card" @tap="navigateTo('/pages/stringing/list')">
            <view class="mini-icon-bg"><text>🔧</text></view><text>穿线服务</text>
          </view>
          <view class="mini-service-card" @tap="navigateTo('/pages/recharge/records')">
            <view class="mini-icon-bg"><text>📊</text></view><text>财务统计</text>
          </view>
          <view class="mini-service-card" @tap="navigateTo('/pages/venue/list')">
            <view class="mini-icon-bg"><text>🏢</text></view><text>场馆管理</text>
          </view>
          <view class="mini-service-card" @tap="navigateTo('/pages/president/user/list')">
            <view class="mini-icon-bg"><text>👥</text></view><text>用户管理</text>
          </view>
          <view class="mini-service-card" @tap="navigateTo('/pages/course/list')">
            <view class="mini-icon-bg"><text>📅</text></view><text>课程预约</text>
          </view>
          <view class="mini-service-card" @tap="navigateTo('/pages/recharge/records')">
            <view class="mini-icon-bg"><text>🧾</text></view><text>消费记录</text>
          </view>
          <view class="mini-service-card" @tap="navigateTo('/pages/settings/index')">
            <view class="mini-icon-bg"><text>✉️</text></view><text>通知管理</text>
          </view>
        </view>
      </view>

      <!-- Promotional Slider Section -->
      <view class="hot-activities pb-8 overflow-hidden">
        <view class="section-header flex justify-between items-end mb-6">
          <text class="font-headline text-lg font-bold tracking-tight text-on-surface uppercase">热门活动与分析</text>
        </view>
        <scroll-view class="horizontal-scroll" scroll-x="true" show-scrollbar="false">
          <view class="slider-container flex gap-4">
            <view class="slider-card min-w-[280px] h-[160px] bg-slate-200 rounded-2xl relative overflow-hidden group">
              <image class="slider-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuD_c4LAdzmXOihkSJste7_7FusJ4J1mRKXkKFP0AXfVsSOw8qoZ48AYVbuGr_zkMWVC78J386F3iW8JjAtUm29pFNZAXEQx-4sDAERT9MRDwqggXYWDRT-sLadAdJJvt6A_H0wj3RmZ8m8eXnURYMClHB1KcqL9uoky1qM_A7iWtqprP5qOD-ukuOOXGI2_qGg2QTMz6X9YffphF2besxBYtfal98JOFmhXnZqjG0GpSusWp5xf1cC-RPO79LSaDax8hPZZxY8BjFPv" mode="aspectFill"></image>
              <view class="slider-overlay absolute inset-0 bg-gradient-to-t from-black/80 to-transparent flex flex-col justify-end p-5">
                <text class="slider-tag text-[10px] font-bold text-primary-container uppercase tracking-widest mb-1">活动管理</text>
                <text class="slider-title text-white font-bold text-lg">夏季冠军杯报名开始</text>
              </view>
            </view>
             <view class="slider-card min-w-[280px] h-[160px] bg-slate-200 rounded-2xl relative overflow-hidden group">
              <image class="slider-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAL39jyeBUWBYhLjS75fSYqBfyz5MKQo2iTkwWfByZt0JybM6vEwjssaFJqneZmkxh77xe_0BRoUPRK85_I5qMLIYlqBiiAEpIoD4dc7WeO-pLbzO0spbnT3y4Waq9MkJ103zd3B1ygwu-3ri2w-S08Lp1wWVaRrt9PA1GQ1we72IEd7HScTq0-GryqRUYzji1Ze8T-443wqaVqaSvnIIGOLg0saqjj6oOtVHOccJzS8PVP4dAvDW-CmkLRXFZePpNb6ek793mf2aIk" mode="aspectFill"></image>
              <view class="slider-overlay absolute inset-0 bg-gradient-to-t from-black/80 to-transparent flex flex-col justify-end p-5">
                <text class="slider-tag text-[10px] font-bold text-primary-container uppercase tracking-widest mb-1">统计分析</text>
                <text class="slider-title text-white font-bold text-lg">月度训练负荷报告</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </scroll-view>

    <!-- FAB (Contextual for Home) -->
    <view class="fab-button fixed right-6 bottom-28 w-14 h-14 bg-primary-container text-on-primary-container rounded-full flex items-center justify-center shadow-2xl z-40" @tap="navigateTo('/pages/booking/create')">
      <text class="fab-icon">＋</text>
    </view>

    <!-- Custom BottomNavBar Shell -->
    <CustomTabBar :current="0" />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const statusBarHeight = ref(44)
const menuButtonWidth = ref(16)

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  // #ifdef MP-WEIXIN
  const menuInfo = uni.getMenuButtonBoundingClientRect()
  if (menuInfo) {
    menuButtonWidth.value = systemInfo.screenWidth - menuInfo.left + 16
  }
  // #endif
})

const navigateTo = (url: string) => {
  uni.navigateTo({ url })
}
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.home-page {
  background-color: #f9f9f9;
  min-height: 100vh;
  color: #1a1c1c;
  font-family: 'Lexend', sans-serif;
  padding-bottom: env(safe-area-inset-bottom);
}

.top-app-bar {
  background-color: #f8fafc;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 50;
  box-sizing: border-box;

  .flex {
    display: flex;
  }
  .items-center {
    align-items: center;
  }
  .gap-3 {
    gap: 24rpx;
  }

  .brand-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #ea580c;
    margin-left: 10rpx;
    white-space: nowrap;
  }
}

.avatar-wrapper {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  border: 4rpx solid #ff6600;
  overflow: hidden;
  flex-shrink: 0;
  
  .avatar-img {
    width: 100%;
    height: 100%;
  }
}

.notification-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: auto;
  margin-right: 16rpx;
  flex-shrink: 0;

  .icon-notifications {
    font-size: 44rpx;
  }

  .notification-dot {
    position: absolute;
    top: -2rpx;
    right: -4rpx;
    width: 16rpx;
    height: 16rpx;
    background-color: #e11d48;
    border-radius: 50%;
    border: 3rpx solid #fff;
  }
}

.main-scroll {
  padding: 0 40rpx;
  box-sizing: border-box;
}

.mb-8 {
  margin-bottom: 64rpx;
}

.grid {
  display: grid;
  gap: 32rpx;
}
.grid-cols-1 { grid-template-columns: repeat(1, minmax(0, 1fr)); }
.grid-cols-2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.grid-cols-4 { grid-template-columns: repeat(4, minmax(0, 1fr)); }

/* Responsive grid: WeChat Mini Program does not support escaped Tailwind selectors.
   Mobile-first layout is handled by the base grid classes above. */

.balance-card {
  background-color: #ff6600;
  padding: 40rpx;
  border-radius: 24rpx;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;

  .bg-icon-wrapper {
    position: absolute;
    right: -40rpx;
    top: -40rpx;
    opacity: 0.1;
  }
  
  .bg-icon {
    font-size: 160rpx;
  }

  .balance-label {
    font-size: 24rpx;
    font-weight: bold;
    letter-spacing: 4rpx;
  }

  .balance-value text {
    font-size: 64rpx;
    font-weight: bold;
  }

  .vip-status {
    margin-top: 40rpx;
    display: flex;
    align-items: center;
    gap: 16rpx;

    .vip-badge {
      background-color: #561d00;
      color: #ff6600;
      padding: 4rpx 16rpx;
      border-radius: 999px;
      font-size: 20rpx;
      font-weight: 900;
    }

    .vip-date {
      font-size: 20rpx;
      opacity: 0.8;
    }
  }
}

.reminder-card {
  background-color: #ffffff;
  padding: 40rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  .reminder-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
  }

  .reminder-label {
    font-size: 24rpx;
    color: #5f5e5e;
    margin-bottom: 20rpx;
  }

  .reminder-title {
    font-size: 40rpx;
    font-weight: bold;
    margin-bottom: 8rpx;
  }

  .reminder-desc {
    font-size: 28rpx;
    color: #5f5e5e;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40rpx;

  .font-headline {
    font-size: 40rpx;
    font-weight: bold;
  }
  
  .text-lg {
    font-size: 32rpx;
  }

  .header-line {
    height: 8rpx;
    width: 96rpx;
    background-color: #ff6600;
    border-radius: 999px;
    margin-bottom: 12rpx;
  }
  
  .view-all {
    color: #a33e00;
    font-size: 24rpx;
  }
}

.service-card {
  background-color: #ffffff;
  padding: 40rpx 0;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 32rpx;

  .service-icon-bg {
    background-color: #ff6600;
    width: 120rpx;
    height: 120rpx;
    border-radius: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .service-icon {
      font-size: 64rpx;
    }
  }

  .service-name {
    font-weight: bold;
    color: #1a1c1c;
    font-size: 28rpx;
  }
}

.mini-services-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32rpx 16rpx;

  .mini-service-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20rpx;

    .mini-icon-bg {
      width: 112rpx;
      height: 112rpx;
      border-radius: 50%;
      background-color: #e8e8e8;
      display: flex;
      align-items: center;
      justify-content: center;
      
      text {
        font-size: 52rpx;
      }
    }

    > text {
      font-size: 24rpx;
      font-weight: bold;
      color: #5f5e5e;
    }
  }
}

.horizontal-scroll {
  width: 100%;
  white-space: nowrap;
}

.slider-container {
  display: flex;
  flex-direction: row;
  gap: 32rpx;
  padding-bottom: 32rpx;
}

.slider-card {
  width: 560rpx;
  height: 320rpx;
  border-radius: 32rpx;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;

  .slider-img {
    width: 100%;
    height: 100%;
  }

  .slider-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 160rpx;
    padding: 32rpx;
    background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
    display: flex;
    flex-direction: column;
    justify-content: flex-end;

    .slider-tag {
      color: #ff6600;
      font-size: 20rpx;
      font-weight: bold;
      margin-bottom: 12rpx;
    }

    .slider-title {
      color: #fff;
      font-size: 32rpx;
      font-weight: bold;
    }
  }
}

.fab-button {
  width: 112rpx;
  height: 112rpx;
  background-color: #ff6600;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  box-shadow: 0 16rpx 40rpx rgba(255, 102, 0, 0.4);
  position: fixed;
  right: 48rpx;
  bottom: 224rpx;
  z-index: 100;
}

</style>
