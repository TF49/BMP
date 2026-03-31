<template>
  <view class="notice-page bg-surface min-h-screen pb-24">
    <scroll-view scroll-y class="main-content">
      <!-- Top Navbar -->
      <view class="w-full bg-[#F8FAFC] flex justify-between items-center px-6 py-4 pb-4 sticky-nav" :style="{ paddingTop: (statusBarHeight || 44) + 'px' }">
        <view class="text-[#1a1a1a] transition-transform duration-200 w-10 h-10 flex items-center" hover-class="scale-95" @tap="goBack">
          <text class="material-symbols-outlined text-2xl">arrow_back</text>
        </view>
        <text class="font-bold text-lg text-[#1a1a1a]">通知中心</text>
        <view class="text-primary text-sm font-bold w-fit whitespace-nowrap transition-transform duration-200" :style="{ marginRight: navBarMarginRight + 'px' }" hover-class="scale-95" @tap="markAllAsRead">
          <text>全部已读</text>
        </view>
      </view>

      <view class="px-6 space-y-6 pt-6 pb-12">
        <!-- Title Area -->
        <view class="flex flex-col gap-1">
          <text class="text-primary text-xs-10 font-bold tracking-widest uppercase">收件箱动态</text>
          <text class="text-3xl font-black text-on-surface tracking-tight mt-1">时刻掌握最新动态</text>
        </view>

        <!-- Tabs -->
        <view class="flex flex-row gap-3 mt-6">
          <view 
            class="tab-pill" 
            :class="{ 'active': activeTab === 'all' }" 
            @tap="activeTab = 'all'"
          >
            全部
          </view>
          <view 
            class="tab-pill" 
            :class="{ 'active': activeTab === 'match' }" 
            @tap="activeTab = 'match'"
          >
            赛事
          </view>
          <view 
            class="tab-pill" 
            :class="{ 'active': activeTab === 'system' }" 
            @tap="activeTab = 'system'"
          >
            系统
          </view>
        </view>

        <!-- Notice List -->
        <view class="flex flex-col gap-4 mt-6">
          
          <!-- Item 1: Match starts soon -->
          <view class="notice-card group shadow-sm flex flex-row p-5 rounded-2xl bg-white relative overflow-hidden active-scale border-left-primary">
            <view class="icon-box bg-orange-50 text-orange-600 relative">
              <text class="material-symbols-outlined icon-lg">sports_tennis</text>
              <view class="red-dot"></view>
            </view>
            <view class="flex flex-col flex-1 pl-4">
              <view class="flex justify-between items-center">
                <text class="font-bold text-on-surface text-base">即将开始的比赛</text>
                <text class="text-xs text-secondary opacity-70">10分钟前</text>
              </view>
              <text class="text-sm text-secondary mt-2 leading-relaxed">
                Your semi-final match against <text class="font-bold text-on-surface">Alex Chen</text> at Court 04 starts in 30 minutes. Get warmed up!
              </text>
            </view>
          </view>

          <!-- Item 2: Registration Reminder -->
          <view class="notice-card shadow-sm flex flex-row p-5 rounded-2xl bg-white relative overflow-hidden active-scale">
             <view class="icon-box bg-gray-100 text-gray-500 relative">
              <text class="material-symbols-outlined icon-lg">calendar_month</text>
            </view>
            <view class="flex flex-col flex-1 pl-4">
              <view class="flex justify-between items-center">
                <text class="font-bold text-on-surface text-base">赛事报名提醒</text>
                <text class="text-xs text-secondary opacity-70">2小时前</text>
              </view>
              <text class="text-sm text-secondary mt-2 leading-relaxed">
                Early bird registration for the <text class="font-bold text-on-surface">Summer Smash Open</text> closes in 24 hours.
              </text>
            </view>
          </view>

          <!-- Item 3: System Maintenance -->
          <view class="notice-card shadow-sm flex flex-row p-5 rounded-2xl bg-white relative overflow-hidden active-scale border-left-primary">
             <view class="icon-box bg-orange-50 text-orange-600 relative">
              <text class="material-symbols-outlined icon-lg">settings</text>
              <view class="red-dot"></view>
            </view>
            <view class="flex flex-col flex-1 pl-4">
              <view class="flex justify-between items-center">
                <text class="font-bold text-on-surface text-base">系统维护公告</text>
                <text class="text-xs text-secondary opacity-70">5小时前</text>
              </view>
              <text class="text-sm text-secondary mt-2 leading-relaxed">
                Kinetic Logic will be down for scheduled maintenance tonight from <text class="font-bold text-on-surface">02:00 AM to 03:00 AM EST</text>.
              </text>
            </view>
          </view>

          <!-- Item 4: Premium Upgrade -->
          <view class="premium-card shadow-md flex flex-row p-6 rounded-2xl relative overflow-hidden active-scale">
            <!-- Background Decorative Elements -->
            <view class="premium-bg-circle premium-bg-circle-1"></view>
            <view class="premium-bg-circle premium-bg-circle-2"></view>
            <view class="premium-bg-line premium-bg-line-1"></view>
            <view class="premium-bg-line premium-bg-line-2"></view>
            
            <view class="flex flex-col flex-1 z-20">
              <text class="text-primary text-xs-10 font-bold tracking-widest uppercase">高级会员特权</text>
              <text class="text-xl font-bold text-white mt-2 leading-snug" style="width: 80%;">解锁下一场赛事的耐力数据分析报告</text>
              <view class="mt-4">
                <view class="inline-flex py-2 px-5 bg-white rounded-full active-scale" @tap="navigateTo('/pages/profile/member')">
                  <text class="text-sm font-bold text-black">立即升级</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Item 5: Match Result -->
          <view class="notice-card shadow-sm flex flex-row p-5 rounded-2xl bg-white relative overflow-hidden active-scale">
             <view class="icon-box bg-gray-100 text-gray-500 relative">
              <text class="material-symbols-outlined icon-lg">emoji_events</text>
            </view>
            <view class="flex flex-col flex-1 pl-4">
              <view class="flex justify-between items-center">
                <text class="font-bold text-on-surface text-base">比赛结果已发布</text>
                <text class="text-xs text-secondary opacity-70">昨天</text>
              </view>
              <text class="text-sm text-secondary mt-2 leading-relaxed">
                Your score against <text class="font-bold text-on-surface">Marcus J.</text> has been verified. Rank updated to <text class="font-bold text-primary">#14</text>.
              </text>
            </view>
          </view>

          <!-- Item 6: Security Alert -->
          <view class="notice-card shadow-sm flex flex-row p-5 rounded-2xl bg-white relative overflow-hidden active-scale">
             <view class="icon-box bg-gray-100 text-gray-500 relative">
              <text class="material-symbols-outlined icon-lg">security</text>
            </view>
            <view class="flex flex-col flex-1 pl-4">
              <view class="flex justify-between items-center">
                <text class="font-bold text-on-surface text-base">安全警报</text>
                <text class="text-xs text-secondary opacity-70">2天前</text>
              </view>
              <text class="text-sm text-secondary mt-2 leading-relaxed">
                A new login was detected from <text class="font-bold text-on-surface">Chrome on MacOS (San Jose, CA)</text>.
              </text>
            </view>
          </view>

        </view>

        <!-- Bottom Load More -->
        <view class="py-8 flex justify-center">
          <text class="text-xs text-secondary opacity-60 tracking-wider">加载历史通知</text>
        </view>
        
      </view>
      
      <!-- Safe Area Spacer -->
      <view class="h-10"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const statusBarHeight = ref(44)
const activeTab = ref('all')
const navBarMarginRight = ref(0) // right margin to avoid capsule

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  // #ifdef MP
  try {
    const menuInfo = uni.getMenuButtonBoundingClientRect()
    if (menuInfo) {
      // menuInfo.left is the left edge of the capsule
      // Calculate distance to the right screen edge
      // Subtract 24px because the header has px-6 padding
      // Add roughly 8px buffer
      navBarMarginRight.value = Math.max(0, systemInfo.windowWidth - menuInfo.left - 24 + 8) 
    }
  } catch (e) {}
  // #endif
})

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.reLaunch({ url: '/pages/index/index' })
    }
  })
}

const navigateTo = (url: string) => {
  if (url === '#') return
  uni.navigateTo({ url })
}

const markAllAsRead = () => {
  uni.showToast({
    title: '已全部标为已读',
    icon: 'success'
  })
}
</script>

<style lang="scss" scoped>
.notice-page {
  font-family: 'Lexend', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* Colors */
.text-on-surface { color: #1a1a1a; }
.bg-surface { background-color: #F8FAFC; }
.text-secondary { color: #64748b; }
.text-primary { color: #ff6600; }
.text-white { color: #ffffff; }
.text-black { color: #000000; }
.bg-white { background-color: #ffffff; }
.bg-gray-900 { background-color: #111827; }
.bg-gray-100 { background-color: #f1f5f9; }
.text-gray-500 { color: #64748b; }
.bg-orange-50 { background-color: #fff7ed; }
.text-orange-600 { color: #ea580c; }

/* Utilities */
.flex { display: flex; }
.flex-col { flex-direction: column; }
.flex-row { flex-direction: row; }
.flex-1 { flex: 1; }
.justify-between { justify-content: space-between; }
.justify-center { justify-content: center; }
.items-center { align-items: center; }
.w-full { width: 100%; }
.h-full { height: 100%; }
.min-h-screen { min-height: 100vh; }
.relative { position: relative; }
.absolute { position: absolute; }
.z-10 { z-index: 10; }
.z-20 { z-index: 20; }
.overflow-hidden { overflow: hidden; }
.inline-flex { display: inline-flex; }
.whitespace-nowrap { white-space: nowrap; }

/* Insets and sizes */
.w-10 { width: 40px; }
.h-10 { height: 40px; }
.w-12 { width: 48px; }
.w-fit { width: fit-content; }
.w-half { width: 50%; }
.w-3-quarters { width: 75%; }
.w-4-fifths { width: 80%; }
.h-10 { height: 40px; }
.px-6 { padding-left: 24px; padding-right: 24px; }
.py-4 { padding-top: 16px; padding-bottom: 16px; }
.pb-4 { padding-bottom: 16px; }
.pb-12 { padding-bottom: 48px; }
.pb-24 { padding-bottom: 96px; }
.pt-6 { padding-top: 24px; }
.py-8 { padding-top: 32px; padding-bottom: 32px; }
.py-2 { padding-top: 8px; padding-bottom: 8px; }
.px-5 { padding-left: 20px; padding-right: 20px; }
.p-5 { padding: 20px; }
.p-6 { padding: 24px; }
.pl-4 { padding-left: 16px; }

/* Spacing */
.mt-1 { margin-top: 4px; }
.mt-2 { margin-top: 8px; }
.mt-4 { margin-top: 16px; }
.mt-6 { margin-top: 24px; }
.gap-1 { gap: 4px; }
.gap-3 { gap: 12px; }
.gap-4 { gap: 16px; }

/* Typography */
.font-bold { font-weight: 700; }
.font-black { font-weight: 900; }
.text-3xl { font-size: 28px; line-height: 1.2; }
.text-2xl { font-size: 24px; }
.text-xl { font-size: 20px; line-height: 1.4; }
.text-lg { font-size: 18px; }
.text-base { font-size: 16px; }
.text-sm { font-size: 14px; }
.text-xs { font-size: 12px; }
.text-xs-10 { font-size: 10px; }
.tracking-tight { letter-spacing: -0.02em; }
.tracking-widest { letter-spacing: 0.1em; }
.tracking-wider { letter-spacing: 0.05em; }
.uppercase { text-transform: uppercase; }
.text-right { text-align: right; }
.leading-relaxed { line-height: 1.6; }
.leading-snug { line-height: 1.375; }

/* Decorations */
.rounded-2xl { border-radius: 16px; }
.rounded-full { border-radius: 9999px; }
.shadow-sm { box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03); }
.shadow-md { box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1); }
.opacity-70 { opacity: 0.7; }
.opacity-60 { opacity: 0.6; }
.opacity-40 { opacity: 0.4; }
.object-cover { object-fit: cover; }

.bg-gradient-to-l {
  background: linear-gradient(to left, transparent, #111827);
}

.sticky-nav {
  position: sticky;
  top: 0;
  z-index: 50;
}

/* Custom components */
.tab-pill {
  padding: 6px 16px;
  border-radius: 99px;
  background-color: #e2e8f0;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  
  &.active {
    background-color: #ea580c;
    color: white;
  }
}

.icon-box {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-lg {
  font-size: 24px;
}

.red-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  background-color: #ea580c;
  border-radius: 50%;
  border: 2px solid #fff7ed;
}

.border-left-primary::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 60%;
  width: 4px;
  background-color: #a33e00;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}

.premium-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.premium-bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
}

.premium-bg-circle-1 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, #ea580c 0%, transparent 70%);
  top: -40px;
  right: -30px;
}

.premium-bg-circle-2 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, #ff6600 0%, transparent 70%);
  bottom: -30px;
  right: 40px;
  opacity: 0.08;
}

.premium-bg-line {
  position: absolute;
  background: rgba(255, 255, 255, 0.06);
  transform: rotate(-35deg);
}

.premium-bg-line-1 {
  width: 200px;
  height: 1px;
  top: 30px;
  right: -20px;
}

.premium-bg-line-2 {
  width: 160px;
  height: 1px;
  top: 50px;
  right: -10px;
}

.active-scale {
  transition: transform 0.2s ease;
  &:active {
    transform: scale(0.98);
  }
}

.scale-95 {
  transform: scale(0.95);
}
.transition-transform {
  transition-property: transform;
}
.duration-200 {
  transition-duration: 200ms;
}
</style>
