<template>
  <view class="home-page bg-surface text-on-surface min-h-screen pb-24">
    <scroll-view scroll-y class="main-content">
      <!-- TopAppBar (Moved inside scroll-view) -->
      <view class="w-full bg-[#f3f3f3] flex justify-between items-center px-6 py-4 pb-6" :style="{ paddingTop: (statusBarHeight || 44) + 'px' }">
        <PresidentBrandLogo width="220rpx" />
        <view class="text-[#5f5e5e] transition-transform duration-200" :style="{ marginRight: navBarMarginRight + 'px' }" hover-class="scale-105" @tap="navigateTo('/pages/notice/index')">
          <uni-icons type="chatbubble" size="20" color="#5f5e5e"></uni-icons>
        </view>
      </view>

      <view class="px-6 space-y-8 pb-32 pt-2">
        <!-- Hero Section -->
        <view class="relative h-48 rounded-2xl overflow-hidden shadow-lg group">
          <image
            class="absolute inset-0 w-full h-full object-cover"
            mode="aspectFill"
            src="/static/placeholders/hero.svg"
            @error="onImgError('hero_unsplash', $event)"
          />
          <view class="absolute inset-0 bg-gradient-to-t flex flex-col justify-center p-6 bg-black-alpha-40">
            <text class="text-primary font-bold text-sm tracking-widest mb-2">会长管理面板</text>
            <text class="text-white text-3xl font-bold tracking-tight">BMP 经营概览</text>
          </view>
        </view>

        <!-- Stats Section -->
        <view class="bg-surface-container-lowest p-6 rounded-2xl shadow-sm mt-6">
          <view class="mb-6">
            <text class="text-secondary text-xs font-bold tracking-wide">总营收</text>
            <view class="flex items-end gap-2 mt-1">
              <text class="text-3xl font-extrabold text-on-surface">¥2,480,000.00</text>
              <view class="bg-emerald-100 px-2 py-1 rounded-full mb-1">
                <text class="text-emerald-600 text-xs font-bold">+12.5%</text>
              </view>
            </view>
            <view class="w-full h-1 bg-gray-100 rounded-full mt-4">
              <view class="h-full bg-primary rounded-full progress-bar" style="width: 60%;"></view>
            </view>
          </view>

          <view class="grid grid-cols-2 gap-4 pt-4 border-t border-gray-100">
            <view class="flex flex-col gap-2">
              <view class="text-primary flex items-center gap-1">
                <uni-icons type="wallet" size="18" color="#a33e00"></uni-icons>
              </view>
              <text class="text-2xl font-bold text-on-surface">82%</text>
              <text class="text-secondary text-xs-12">场地利用率</text>
            </view>
            <view class="flex flex-col gap-2 border-l border-gray-100 pl-4">
              <view class="text-primary flex items-center gap-1">
                <uni-icons type="person" size="18" color="#a33e00"></uni-icons>
              </view>
              <text class="text-2xl font-bold text-on-surface">1,402</text>
              <text class="text-secondary text-xs-12">新增会员</text>
            </view>
          </view>
        </view>

        <!-- Management Center Grid -->
        <view class="mt-8">
          <view class="flex flex-row justify-between items-end mb-6">
            <text class="font-headline text-xl font-bold tracking-tight text-on-surface">管理中心</text>
            <text class="text-primary text-xs-12 font-bold hover-scale" @tap="navigateTo(PRESIDENT_PAGES.USER_LIST)">查看全部</text>
          </view>
          <view class="grid-card-3 gap-4">
            <view class="bg-surface-container-lowest aspect-square rounded-2xl flex flex-col items-center justify-center gap-3 text-center shadow-sm" hover-class="scale-95 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.VENUE_LIST)">
              <uni-icons type="location" size="24" color="#a33e00"></uni-icons>
              <text class="text-xs-12 font-bold text-on-surface">场馆管理</text>
            </view>
            <view class="bg-surface-container-lowest aspect-square rounded-2xl flex flex-col items-center justify-center gap-3 text-center shadow-sm" hover-class="scale-95 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.COURT_LIST)">
              <uni-icons type="calendar" size="24" color="#a33e00"></uni-icons>
              <text class="text-xs-12 font-bold text-on-surface">场地管理</text>
            </view>
            <view class="bg-surface-container-lowest aspect-square rounded-2xl flex flex-col items-center justify-center gap-3 text-center shadow-sm" hover-class="scale-95 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.COACH_LIST)">
              <uni-icons type="staff" size="24" color="#a33e00"></uni-icons>
              <text class="text-xs-12 font-bold text-on-surface">教练管理</text>
            </view>
            <view class="bg-surface-container-lowest aspect-square rounded-2xl flex flex-col items-center justify-center gap-3 text-center shadow-sm" hover-class="scale-95 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.MEMBER_LIST)">
              <uni-icons type="person" size="24" color="#a33e00"></uni-icons>
              <text class="text-xs-12 font-bold text-on-surface">会员中心</text>
            </view>
            <view class="bg-surface-container-lowest aspect-square rounded-2xl flex flex-col items-center justify-center gap-3 text-center shadow-sm" hover-class="scale-95 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.USER_LIST)">
              <uni-icons type="person" size="24" color="#a33e00"></uni-icons>
              <text class="text-xs-12 font-bold text-on-surface">员工管理</text>
            </view>
            <view class="bg-surface-container-lowest aspect-square rounded-2xl flex flex-col items-center justify-center gap-3 text-center shadow-sm" hover-class="scale-95 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.EQUIPMENT_LIST)">
              <uni-icons type="cart" size="24" color="#a33e00"></uni-icons>
              <text class="text-xs-12 font-bold text-on-surface">器材库存</text>
            </view>
            <view class="bg-surface-container-lowest aspect-square rounded-2xl flex flex-col items-center justify-center gap-3 text-center shadow-sm" hover-class="scale-95 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.TOURNAMENT_LIST)">
              <uni-icons type="medal" size="24" color="#a33e00"></uni-icons>
              <text class="text-xs-12 font-bold text-on-surface">赛事中心</text>
            </view>
          </view>
        </view>

        <!-- Real-time Alerts List -->
        <view class="mt-8 pb-8">
          <view class="mb-6">
            <text class="font-headline text-xl font-bold tracking-tight text-on-surface">实时预警</text>
          </view>
          <view class="flex flex-col gap-4">
            <view class="bg-surface-container-lowest p-4 rounded-2xl shadow-sm flex items-center justify-between" hover-class="scale-98 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.USER_LIST)">
              <view class="flex items-center gap-4">
                <view class="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center flex-shrink-0">
                  <uni-icons type="person" size="20" color="#ea580c"></uni-icons>
                </view>
                <view>
                  <text class="text-sm font-bold text-on-surface block">待审核会员</text>
                  <text class="text-xs-12 text-secondary mt-1 block">5 名申请人等待审核</text>
                </view>
              </view>
              <uni-icons type="right" size="16" color="#5f5e5e"></uni-icons>
            </view>
            <view class="bg-surface-container-lowest p-4 rounded-2xl shadow-sm flex items-center justify-between" hover-class="scale-98 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.EQUIPMENT_LIST)">
              <view class="flex items-center gap-4">
                <view class="w-12 h-12 bg-red-100 rounded-xl flex items-center justify-center flex-shrink-0">
                  <uni-icons type="info" size="20" color="#dc2626"></uni-icons>
                </view>
                <view>
                  <text class="text-sm font-bold text-on-surface block">库存不足预警</text>
                  <text class="text-xs-12 text-secondary mt-1 block">3 项核心耗材低于阈值</text>
                </view>
              </view>
              <uni-icons type="right" size="16" color="#5f5e5e"></uni-icons>
            </view>
            <view class="gradient-cta p-4 rounded-2xl shadow-md flex items-center justify-between" hover-class="scale-98 transition-transform" @tap="navigateTo(PRESIDENT_PAGES.TOURNAMENT_LIST)">
               <view class="flex items-center gap-4">
                <view class="w-12 h-12 bg-white-20 rounded-xl flex items-center justify-center flex-shrink-0">
                  <uni-icons type="calendar" size="20" color="#ffffff"></uni-icons>
                </view>
                <view>
                  <text class="text-sm font-bold text-white block">即将开始的赛事</text>
                  <text class="text-xs-12 text-white-80 mt-1 block">夏季公开赛将在 3 天后开始</text>
                </view>
              </view>
              <uni-icons type="right" size="16" color="#ffffff"></uni-icons>
            </view>
          </view>
        </view>

      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PresidentBrandLogo from '@/components/president/PresidentBrandLogo.vue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getSafeSystemInfo } from '@/utils/systemInfo'

const statusBarHeight = ref(44)
const navBarMarginRight = ref(0)

type ImgErrEvent = {
  detail?: any
  target?: any
  currentTarget?: any
  timeStamp?: number
}

const onImgError = (tag: string, e: ImgErrEvent) => {
  // keep parameters consumed for future diagnostics without side effects in MP runtime
  void tag
  void e
}

onMounted(() => {
  const systemInfo = getSafeSystemInfo()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  const uniPlatform = typeof process !== 'undefined' && (process as any)?.env ? (process as any).env.UNI_PLATFORM : 'unknown'
  
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

  void uniPlatform
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

.text-on-surface { color: #1a1c1c; }
.bg-surface { background-color: #f9f9f9; }
.bg-surface-container-lowest { background-color: #ffffff; }
.text-secondary { color: #5f5e5e; }
.text-primary { color: #a33e00; }
.bg-primary { background-color: #a33e00; }
.text-orange-600 { color: #ea580c; }
.bg-orange-100 { background-color: #ffedd5; }
.text-emerald-600 { color: #059669; }
.bg-emerald-100 { background-color: #d1fae5; }
.text-red-600 { color: #dc2626; }
.bg-red-100 { background-color: #fee2e2; }
.text-white { color: #ffffff; }
.text-white-80 { color: rgba(255, 255, 255, 0.8); }
.bg-white-20 { background-color: rgba(255, 255, 255, 0.2); }
.bg-black-alpha-40 { background-color: rgba(0, 0, 0, 0.4); }
.bg-gray-100 { background-color: #f3f4f6; }

.gradient-cta {
  background: linear-gradient(45deg, #a33e00, #ea580c);
}
.bg-gradient-to-t {
  background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.4), transparent);
}

.shadow-sm { box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); }
.shadow-md { box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.shadow-lg { box-shadow: 0 10px 15px -3px rgba(163, 62, 0, 0.2); }

.border-t { border-top-width: 1px; border-top-style: solid; }
.border-l { border-left-width: 1px; border-left-style: solid; }
.border-gray-100 { border-color: #f3f4f6; }

.main-content { height: 100vh; }
.grid-cols-2 { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.grid-card-3 { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); }

.flex { display: flex; }
.flex-col { flex-direction: column; }
.flex-row { flex-direction: row; }
.items-center { align-items: center; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.items-end { align-items: flex-end; }
.flex-shrink-0 { flex-shrink: 0; }

.block { display: block; }

.gap-1 { gap: 4rpx; }
.gap-2 { gap: 8rpx; }
.gap-3 { gap: 12rpx; }
.gap-4 { gap: 16rpx; }

.px-6 { padding-left: 24rpx; padding-right: 24rpx; }
.py-4 { padding-top: 16rpx; padding-bottom: 16rpx; }
.px-2 { padding-left: 8rpx; padding-right: 8rpx; }
.py-1 { padding-top: 4rpx; padding-bottom: 4rpx; }
.p-6 { padding: 24rpx; }
.p-4 { padding: 16rpx; }
.pl-4 { padding-left: 16rpx; }
.pb-6 { padding-bottom: 24rpx; }
.pt-2 { padding-top: 8rpx; }
.pt-4 { padding-top: 16rpx; }

.mt-8 { margin-top: 32rpx; }
.mt-6 { margin-top: 24rpx; }
.mb-6 { margin-bottom: 24rpx; }
.mb-2 { margin-bottom: 8rpx; }
.mb-1 { margin-bottom: 4rpx; }
.mt-4 { margin-top: 16rpx; }
.mt-1 { margin-top: 4rpx; }
.pb-24 { padding-bottom: 96rpx; }
.pb-32 { padding-bottom: 240rpx; }
.pb-8 { padding-bottom: 32rpx; }

.w-full { width: 100%; }
.h-full { height: 100%; }
.w-10 { width: 40px; }
.h-10 { height: 40px; }
.w-12 { width: 48px; }
.h-12 { height: 48px; }
.h-48 { height: 320rpx; }
.h-1 { height: 6rpx; }

.min-h-screen { min-height: 100vh; }
.rounded-full { border-radius: 9999px; }
.rounded-2xl { border-radius: 32rpx; }
.rounded-xl { border-radius: 24rpx; }

.overflow-hidden { overflow: hidden; }
.relative { position: relative; }
.absolute { position: absolute; }

.inset-0 { top: 0; right: 0; bottom: 0; left: 0; }

.font-bold { font-weight: bold; }
.font-extrabold { font-weight: 800; }
.font-black { font-weight: 900; }

.text-3xl { font-size: 30px; line-height: 1.2; }
.text-2xl { font-size: 24px; line-height: 1.3; }
.text-xl { font-size: 20px; line-height: 1.4; }
.text-sm { font-size: 14px; line-height: 1.5; }
.text-xs { font-size: 12px; }
.text-xs-12 { font-size: 12px; }

.tracking-tight { letter-spacing: -0.025em; }
.tracking-wide { letter-spacing: 0.05em; }
.tracking-widest { letter-spacing: 0.1em; }

.text-center { text-align: center; }

.transition-all { transition-property: all; transition-duration: 300ms; }
.transition-transform { transition-property: transform; transition-duration: 300ms; }

.scale-105 { transform: scale(1.05); }
.scale-98 { transform: scale(0.98); }
.scale-95 { transform: scale(0.95); }
.hover-scale { transition: transform 0.2s; &:active { transform: scale(0.95); } }
.hover-scale-102 { transition: transform 0.2s; &:active { transform: scale(1.02); } }

.aspect-square { aspect-ratio: 1 / 1; }
.object-cover { object-fit: cover; }
</style>
