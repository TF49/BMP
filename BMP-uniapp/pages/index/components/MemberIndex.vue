<template>
  <view class="home-page bg-surface text-on-surface min-h-screen pb-24">
    <scroll-view scroll-y class="main-content">
      <!-- TopAppBar (Moved inside scroll-view) -->
      <view class="w-full bg-[#f3f3f3] flex justify-between items-center px-6 py-4 pb-6" :style="{ paddingTop: (statusBarHeight || 44) + 'px' }">
        <view class="flex items-center gap-3">
          <view class="w-10 h-10 rounded-full overflow-hidden transition-all duration-300 ease-in-out hover-scale-102">
            <image class="w-full h-full object-cover" mode="aspectFill" src="/static/placeholders/avatar.svg" />
          </view>
          <text class="font-lexend text-xl font-bold tracking-tight text-[#1a1a1a]">Kinetic Logic</text>
        </view>
        <view class="text-[#5f5e5e] transition-transform duration-200" :style="{ marginRight: navBarMarginRight + 'px' }" hover-class="scale-105" @tap="navigateTo('/pages/notice/index')">
          <uni-icons type="chatbubble" size="20" color="#5f5e5e"></uni-icons>
        </view>
      </view>

      <view class="px-6 space-y-8 pb-24 pt-2">
        <!-- Account & Booking Reminder Section -->
        <view class="grid-section flex flex-col gap-4">
          <!-- Account Summary -->
          <view class="bg-surface-container-lowest p-6 rounded-xl flex flex-col justify-between h-40 relative overflow-hidden group shadow-sm" @tap="navigateTo('/pages/recharge/index')">
            <view class="z-10">
              <text class="text-xs-10 font-bold tracking-wide uppercase text-secondary mb-1 block">账户余额</text>
              <text class="text-3xl font-extrabold text-on-surface block mt-1">¥2,480.00</text>
              <view class="inline-flex mt-2 px-3 py-1 bg-primary-container-alpha rounded-full">
                <text class="text-primary text-xs-10 font-bold">VIP 会员</text>
              </view>
            </view>
            <view class="absolute-bottom-right opacity-5 transition-transform duration-500">
              <uni-icons type="wallet" size="52" color="rgba(0,0,0,0.12)"></uni-icons>
            </view>
            <view class="z-10 self-end">
              <view class="text-primary text-sm font-bold flex items-center gap-1">
                立即充值 <uni-icons type="right" size="14" color="#a33e00"></uni-icons>
              </view>
            </view>
          </view>
          <!-- Booking Reminder -->
          <view class="gradient-cta p-6 rounded-xl text-white flex flex-col items-center justify-center text-center shadow-lg" @tap="navigateTo('/pages/booking/list')">
            <text class="text-xs-10 font-bold tracking-wide uppercase opacity-80 mb-2 block">预约提醒</text>
            <view class="flex flex-col items-center">
              <text class="text-lg font-bold block">Court 01</text>
              <text class="text-2xl font-black block mt-1">14:00</text>
            </view>
            <view class="mt-4 flex flex-row gap-2 items-center">
              <uni-icons type="location" size="14" color="#ffffff"></uni-icons>
              <text class="text-xs-10 font-medium">中心体育馆 · 3号场地</text>
            </view>
          </view>
        </view>

        <!-- Core Services Grid -->
        <view class="mt-8">
          <view class="flex flex-row justify-between items-end mb-6">
            <text class="font-headline text-2xl font-bold tracking-tight text-on-surface">核心服务</text>
            <text class="text-primary text-xs-12 font-bold uppercase tracking-widest">Core Services</text>
          </view>
          <!-- Always uses 2 columns for a better mobile display, mapped from grid-cols-2 md:grid-cols-3 -->
          <view class="grid-services gap-4">
            <!-- Court Booking -->
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/venue/list')">
              <view class="w-12 h-12 rounded-lg bg-primary-alpha flex items-center justify-center text-primary">
                <uni-icons type="flag" size="24" color="#a33e00"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">场地预订</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Court Booking</text>
              </view>
            </view>
            <!-- Coach Courses -->
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/course/list')">
              <view class="w-12 h-12 rounded-lg bg-tertiary-fixed flex items-center justify-center text-tertiary">
                <uni-icons type="compose" size="24" color="#0062a1"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">教练课程</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Coach Courses</text>
              </view>
            </view>
            <!-- Tournament Registration -->
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/tournament/list')">
              <view class="w-12 h-12 rounded-lg bg-orange-100 flex items-center justify-center text-orange-600">
                <uni-icons type="medal" size="24" color="#ea580c"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">赛事报名</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Tournaments</text>
              </view>
            </view>
            <!-- Equipment Rental -->
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/equipment/list')">
              <view class="w-12 h-12 rounded-lg bg-slate-100 flex items-center justify-center text-slate-600">
                <uni-icons type="cart" size="24" color="#475569"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">器材租赁</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Equipment</text>
              </view>
            </view>
            <!-- Member Center -->
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/profile/index')">
              <view class="w-12 h-12 rounded-lg bg-purple-100 flex items-center justify-center text-purple-600">
                <uni-icons type="vip" size="24" color="#9333ea"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">会员中心</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Member Center</text>
              </view>
            </view>
            <!-- Stringing Services -->
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/stringing/list')">
              <view class="w-12 h-12 rounded-lg bg-emerald-100 flex items-center justify-center text-emerald-600">
                <uni-icons type="compose" size="24" color="#059669"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">穿线服务</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Stringing</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Insights Section -->
        <view class="mt-8 pb-8">
          <view class="flex flex-row justify-between items-end mb-6">
            <text class="font-headline text-2xl font-bold tracking-tight text-on-surface">精彩发现</text>
            <text class="text-primary text-xs-12 font-bold uppercase tracking-widest">Quick Insights</text>
          </view>
          <view class="flex flex-col gap-4">
            <!-- Large Feature Card -->
            <view class="relative h-64 rounded-xl overflow-hidden group shadow-md bg-gray-900 group-hover-scale" @tap="navigateTo('/pages/insights/detail')">
              <image class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover-image" mode="aspectFill" src="/static/placeholders/hero.svg" />
              <view class="absolute inset-0 bg-gradient-to-t flex flex-col justify-end p-6">
                <text class="bg-primary px-2 py-1 text-xs-8 font-bold text-white rounded w-fit mb-2">热门话题</text>
                <text class="text-white text-xl font-bold block">夏季羽毛球公开赛现已开启报名</text>
                <text class="text-white-70 text-xs-12 mt-1 block">挑战自我，赢取丰厚奖金与积分。</text>
              </view>
            </view>
            
            <view class="flex flex-col gap-4 mt-2">
              <!-- Small Insight 1 -->
              <view class="bg-surface-container-low p-4 rounded-xl flex flex-row items-center gap-4 shadow-sm" @tap="navigateTo('/pages/insights/detail')">
                <view class="w-16 h-16 rounded-lg overflow-hidden flex-shrink-0 bg-gray-200">
                  <image class="w-full h-full object-cover" mode="aspectFill" src="/static/placeholders/avatar.svg" />
                </view>
                <view class="flex flex-col flex-1">
                  <text class="font-bold text-sm text-on-surface block">新手如何提升反手球？</text>
                  <text class="text-secondary text-xs-10 mt-1 block">五位资深教练的实战技巧分享...</text>
                </view>
              </view>
              <!-- Small Insight 2 -->
              <view class="bg-surface-container-low p-4 rounded-xl flex flex-row items-center gap-4 shadow-sm" @tap="navigateTo('/pages/insights/detail')">
                <view class="w-16 h-16 rounded-lg overflow-hidden flex-shrink-0 bg-gray-200">
                  <image class="w-full h-full object-cover" mode="aspectFill" src="/static/placeholders/avatar.svg" />
                </view>
                <view class="flex flex-col flex-1">
                  <text class="font-bold text-sm text-on-surface block">新一代穿线科技揭秘</text>
                  <text class="text-secondary text-xs-10 mt-1 block">更强弹性，更精准控球...</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Floating Action Button -->
    <view class="fixed right-6 bottom-10 w-14 h-14 gradient-cta text-white rounded-full flex items-center justify-center shadow-2xl transition-transform z-60" hover-class="scale-95" @tap="navigateTo('/pages/venue/list')">
      <uni-icons type="plusempty" size="24" color="#ffffff"></uni-icons>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSafeSystemInfo } from '@/utils/systemInfo'

const statusBarHeight = ref(44)
const navBarMarginRight = ref(0)

onMounted(() => {
  const systemInfo = getSafeSystemInfo()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  // #ifdef MP
  try {
    const menuInfo = uni.getMenuButtonBoundingClientRect()
    if (menuInfo) {
      // menuInfo.left is the left boundary of capsule
      // windowWidth - menuInfo.left is distance from capsule left to screen right edge.
      // We subtract 12px because container has px-6 (about 12px right padding).
      navBarMarginRight.value = Math.max(0, systemInfo.windowWidth - menuInfo.left - 12 + 8) 
    }
  } catch (e) {}
  // #endif
})

const navigateTo = (url: string) => {
  if (url === '#') return
  uni.navigateTo({ url })
}
</script>

<style lang="scss" scoped>
.home-page {
  background-color: #f9f9f9;
}

/* Colors specifically defined in the HTML */
.text-on-surface { color: #1a1c1c; }
.bg-surface { background-color: #f9f9f9; }
.bg-surface-container-lowest { background-color: #ffffff; }
.bg-surface-container-low { background-color: #f3f3f3; }
.text-secondary { color: #5f5e5e; }
.text-primary { color: #a33e00; }
.bg-primary { background-color: #a33e00; }
.bg-primary-alpha { background-color: rgba(255, 102, 0, 0.1); }
.bg-primary-container-alpha { background-color: rgba(255, 102, 0, 0.1); }
.text-tertiary { color: #0062a1; }
.bg-tertiary-fixed { background-color: #d0e4ff; }
.text-orange-600 { color: #ea580c; }
.bg-orange-100 { background-color: #ffedd5; }
.text-slate-600 { color: #475569; }
.bg-slate-100 { background-color: #f1f5f9; }
.text-purple-600 { color: #9333ea; }
.bg-purple-100 { background-color: #f3e8ff; }
.text-emerald-600 { color: #059669; }
.bg-emerald-100 { background-color: #d1fae5; }
.text-white { color: #ffffff; }
.text-white-70 { color: rgba(255, 255, 255, 0.7); }

.gradient-cta {
  background: linear-gradient(45deg, #a33e00, #ff6600);
}

.bg-gradient-to-t {
  background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.4), transparent);
}

.shadow-sm { box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); }
.shadow-md { box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); }
.shadow-lg { box-shadow: 0 10px 15px -3px rgba(163, 62, 0, 0.2), 0 4px 6px -2px rgba(163, 62, 0, 0.1); }
.shadow-2xl { box-shadow: 0 25px 50px -12px rgba(163, 62, 0, 0.25); }

.main-content {
  height: 100vh;
}

/* Grids */
.grid-services {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

/* Tailwind Utility classes fallback */
.flex { display: flex; }
.flex-col { flex-direction: column; }
.flex-row { flex-direction: row; }
.flex-1 { flex: 1; }
.items-center { align-items: center; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.justify-end { justify-content: flex-end; }
.items-end { align-items: flex-end; }
.self-end { align-self: flex-end; }
.flex-shrink-0 { flex-shrink: 0; }
.inline-flex { display: inline-flex; }

.block { display: block; }
.inline-block { display: inline-block; }

.gap-1 { gap: 4rpx; }
.gap-2 { gap: 8rpx; }
.gap-3 { gap: 12rpx; }
.gap-4 { gap: 16rpx; }

.px-6 { padding-left: 24rpx; padding-right: 24rpx; }
.py-4 { padding-top: 16rpx; padding-bottom: 16rpx; }
.px-3 { padding-left: 12rpx; padding-right: 12rpx; }
.py-1 { padding-top: 4rpx; padding-bottom: 4rpx; }
.px-2 { padding-left: 8rpx; padding-right: 8rpx; }
.p-6 { padding: 24rpx; }
.p-5 { padding: 20rpx; }
.p-4 { padding: 16rpx; }
.pt-2 { padding-top: 8rpx; }
.pt-4 { padding-top: 16rpx; }

.mt-20 { margin-top: 80rpx; }
.mt-8 { margin-top: 32rpx; }
.mb-6 { margin-bottom: 24rpx; }
.mb-1 { margin-bottom: 4rpx; }
.mb-2 { margin-bottom: 8rpx; }
.mt-2 { margin-top: 8rpx; }
.mt-4 { margin-top: 16rpx; }
.mt-1 { margin-top: 4rpx; }
.mt-0-5 { margin-top: 2rpx; }
.pb-24 { padding-bottom: 96rpx; }
.pb-32 { padding-bottom: 240rpx; } /* Increased from 128rpx to prevent tabbar overlap */
.pb-48 { padding-bottom: 280rpx; }
.pb-8 { padding-bottom: 32rpx; }

.w-full { width: 100%; }
.h-full { height: 100%; }
.w-10 { width: 40px; }
.h-10 { height: 40px; }
.w-12 { width: 48px; }
.h-12 { height: 48px; }
.w-14 { width: 56px; }
.h-14 { height: 56px; }
.w-16 { width: 64px; }
.h-16 { height: 64px; }
.h-40 { height: 160px; }
.h-64 { height: 256px; }
.w-fit { width: fit-content; }

.min-h-screen { min-height: 100vh; }

.rounded-full { border-radius: 9999px; }
.rounded-xl { border-radius: 24rpx; }
.rounded-lg { border-radius: 16rpx; }
.rounded { border-radius: 8rpx; }

.overflow-hidden { overflow: hidden; }

.fixed { position: fixed; }
.absolute { position: absolute; }
.relative { position: relative; }

.z-10 { z-index: 10; }
.z-50 { z-index: 50; }
.z-60 { z-index: 200; }

.top-0 { top: 0; }
.right-6 { right: 24rpx; }
.bottom-10 { bottom: 40rpx; } /* 首页无底部 TabBar，FAB 贴近安全区 */
.inset-0 { top: 0; right: 0; bottom: 0; left: 0; }
.absolute-bottom-right { position: absolute; right: -16rpx; bottom: -16rpx; }

.font-bold { font-weight: bold; }
.font-extrabold { font-weight: 800; }
.font-black { font-weight: 900; }
.font-medium { font-weight: 500; }

.text-3xl { font-size: 30px; line-height: 36px; }
.text-2xl { font-size: 24px; line-height: 32px; }
.text-xl { font-size: 20px; line-height: 28px; }
.text-lg { font-size: 18px; line-height: 28px; }
.text-sm { font-size: 14px; line-height: 20px; }
.text-xs-12 { font-size: 12px; }
.text-xs-10 { font-size: 10px; }
.text-xs-8 { font-size: 8px; }

.tracking-tight { letter-spacing: -0.025em; }
.tracking-wide { letter-spacing: 0.05em; }
.tracking-widest { letter-spacing: 0.1em; }

.uppercase { text-transform: uppercase; }
.text-center { text-align: center; }

.opacity-5 { opacity: 0.05; }
.opacity-80 { opacity: 0.8; }

.transition-all { transition-property: all; }
.transition-transform { transition-property: transform; }
.duration-300 { transition-duration: 300ms; }
.duration-500 { transition-duration: 500ms; }
.duration-200 { transition-duration: 200ms; }
.duration-700 { transition-duration: 700ms; }
.ease-in-out { transition-timing-function: ease-in-out; }

.scale-105 { transform: scale(1.05); }
.scale-95 { transform: scale(0.95); }
.hover-scale { transition: transform 0.3s; &:active { transform: scale(0.98); } }
.hover-scale-102 { transition: transform 0.3s; &:active { transform: scale(1.02); } }
.group-hover-image { transition: transform 0.7s; }
.group-hover-scale {
  &:active .group-hover-image { transform: scale(1.1); }
}

.icon-9xl { font-size: 140px; }
.aspect-square { aspect-ratio: 1 / 1; }
.object-cover { object-fit: cover; }
</style>
