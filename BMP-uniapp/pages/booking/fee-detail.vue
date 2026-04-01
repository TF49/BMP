<template>
  <view class="fee-detail bg-surface min-h-screen">
    <!-- TopAppBar -->
    <view class="sticky-header bg-white-80 backdrop-blur-md z-50 px-6 py-4 flex flex-row items-center gap-4 border-b border-neutral-100" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="p-2 rounded-full hover-bg-neutral" @tap="goBack">
        <uni-icons type="left" size="20" color="#1a1c1c"></uni-icons>
      </view>
      <text class="font-headline font-bold text-lg text-on-surface">费用明细</text>
    </view>

    <scroll-view scroll-y class="main-content" :style="{ height: scrollHeight }">
      <view class="px-6 py-8">
        <!-- Amount Hero -->
        <view class="text-center mb-10">
          <text class="text-[11px] text-secondary font-bold uppercase tracking-[0.3em] block mb-2">Total Payable</text>
          <text class="text-5xl font-black text-primary tracking-tighter italic">¥{{ bookingInfo.payableAmount.toFixed(2) }}</text>
        </view>

        <!-- Detail Card -->
        <view class="bg-white rounded-3xl p-8 card-glow border border-neutral-100 relative overflow-hidden">
          <view class="absolute top-0 right-0 w-24 h-24 bg-primary-10 rounded-full blur-3xl -mr-12 -mt-12"></view>
          
          <!-- Venue Section -->
          <view class="mb-8 border-b border-dashed border-neutral-100 pb-6">
            <text class="text-[10px] text-secondary font-black uppercase tracking-widest mb-3 block">场馆信息 VENUE</text>
            <text class="text-xl font-bold text-on-surface block mb-2">{{ bookingInfo.venueName }}</text>
            <view class="flex flex-row items-center gap-3">
              <text class="bg-primary-10 text-primary text-[10px] px-2 py-0.5 rounded font-black italic">{{ bookingInfo.courtName }}</text>
              <text class="text-secondary text-sm font-medium">{{ bookingInfo.date }}</text>
              <text class="text-secondary text-sm font-medium">{{ bookingInfo.slot }}</text>
            </view>
          </view>

          <!-- Price Calculation -->
          <view class="space-y-6">
            <text class="text-[10px] text-secondary font-black uppercase tracking-widest mb-4 block">费用计算 BREAKDOWN</text>
            
            <view class="flex flex-row justify-between items-center">
              <view>
                <text class="text-sm font-bold text-on-surface block">场地租赁费</text>
                <text class="text-[10px] text-secondary font-medium block">Base Court Fee (¥80.00 / 小时)</text>
              </view>
              <text class="font-bold text-on-surface">¥{{ (80 * (bookingInfo.duration || 2)).toFixed(2) }}</text>
            </view>

            <view class="flex flex-row justify-between items-center text-primary">
              <view>
                <text class="text-sm font-black italic block">会员专享折扣</text>
                <text class="text-[10px] opacity-80 font-medium block">Member Discount (20% OFF)</text>
              </view>
              <text class="font-black">-¥{{ bookingInfo.vipDiscount.toFixed(2) }}</text>
            </view>

            <view class="h-[1px] bg-neutral-100 w-full"></view>

            <view class="flex flex-row justify-between items-end pt-4">
              <view>
                <text class="text-[11px] font-black text-on-surface uppercase tracking-wider block">应付预计</text>
                <text class="text-[9px] text-secondary font-medium block mt-0.5 uppercase">Estimated Total</text>
              </view>
              <text class="text-2xl font-black text-on-surface tracking-tighter">¥{{ bookingInfo.payableAmount.toFixed(2) }}</text>
            </view>
          </view>
        </view>

        <!-- Policy Card -->
        <view class="mt-8 bg-neutral-100/50 rounded-2xl p-5">
          <view class="flex flex-row items-center gap-2 mb-3">
            <uni-icons type="info" size="14" color="#5f5e5e"></uni-icons>
            <text class="text-[10px] text-secondary font-bold uppercase tracking-widest">预订说明 POLICIES</text>
          </view>
          <view class="space-y-2">
            <text class="text-[11px] text-secondary-60 leading-relaxed block">• 预约成功后，不可随意更改，如需取消请参考取消政策。</text>
            <text class="text-[11px] text-secondary-60 leading-relaxed block">• 请在预约时段前15分钟到达场馆完成核销。</text>
            <text class="text-[11px] text-secondary-60 leading-relaxed block">• 此明细仅供费用构成参考，最终以实际支付为准。</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Decorative Elements -->
    <view class="fixed -top-24 -left-24 w-72 h-72 bg-primary/5 rounded-full blur-large -z-10 animate-pulse"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { safeNavigateBack } from '@/utils/navigation'

const statusBarHeight = ref(44)
const bookingInfo = ref({
  venueName: '天际线羽毛球中心',
  courtName: '#04 COURT',
  date: '2023年10月24日',
  slot: '18:00 - 20:00',
  duration: 2,
  totalAmount: 160.00,
  vipDiscount: 32.00,
  payableAmount: 128.00
})

const scrollHeight = computed(() => {
  return `calc(100vh - ${statusBarHeight.value + 60}px)`
})

onLoad((options: any = {}) => {
  if (options.data) {
    try {
      bookingInfo.value = JSON.parse(decodeURIComponent(options.data))
    } catch (e) {
      console.error('Parse booking data error:', e)
    }
  }
})

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
})

const goBack = () => {
  safeNavigateBack('/pages/index/index')
}
</script>

<style lang="scss" scoped>
.fee-detail {
  font-family: 'Lexend', sans-serif;
}

.sticky-header {
  position: sticky;
  top: 0;
}

.card-glow {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
}

.blur-large { filter: blur(80px); }

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.1; }
  50% { opacity: 0.3; }
}

/* Colors & Tokens */
.text-on-surface { color: #1a1c1c; }
.text-secondary { color: #5f5e5e; }
.text-secondary-60 { color: rgba(95, 94, 94, 0.7); }
.text-primary { color: #FF6600; }
.bg-primary { background-color: #FF6600; }
.bg-surface { background-color: #f8f9fa; }
.bg-primary-10 { background-color: rgba(255, 102, 0, 0.1); }

/* Utilities */
.flex { display: flex; }
.flex-row { flex-direction: row; }
.flex-col { flex-direction: column; }
.items-center { align-items: center; }
.items-end { align-items: flex-end; }
.justify-between { justify-content: space-between; }
.flex-1 { flex: 1; }
.shrink-0 { flex-shrink: 0; }
.relative { position: relative; }
.absolute { position: absolute; }
.overflow-hidden { overflow: hidden; }

.p-2 { padding: 8rpx; }
.p-5 { padding: 20rpx; }
.p-8 { padding: 32rpx; }
.px-2 { padding-left: 8rpx; padding-right: 8rpx; }
.px-6 { padding-left: 24rpx; padding-right: 24rpx; }
.py-0-5 { padding-top: 2rpx; padding-bottom: 2rpx; }
.py-4 { padding-top: 16rpx; padding-bottom: 16rpx; }
.py-8 { padding-top: 32rpx; padding-bottom: 32rpx; }
.pb-6 { padding-bottom: 24rpx; }
.pt-4 { padding-top: 16rpx; }

.mb-1 { margin-bottom: 4rpx; }
.mb-2 { margin-bottom: 8rpx; }
.mb-3 { margin-bottom: 12rpx; }
.mb-4 { margin-bottom: 16rpx; }
.mb-8 { margin-bottom: 32rpx; }
.mb-10 { margin-bottom: 40rpx; }
.mt-0-5 { margin-top: 2rpx; }
.ml-1 { margin-left: 4rpx; }

.w-full { width: 100%; }
.w-24 { width: 96px; }
.h-24 { height: 96px; }
.w-72 { width: 288px; }
.h-72 { height: 288px; }

.gap-2 { gap: 8rpx; }
.gap-3 { gap: 12rpx; }

.rounded { border-radius: 4rpx; }
.rounded-2xl { border-radius: 32rpx; }
.rounded-3xl { border-radius: 48rpx; }
.rounded-full { border-radius: 9999px; }

.border { border-width: 1rpx; border-style: solid; }
.border-b { border-bottom-width: 1rpx; border-style: solid; }
.border-dashed { border-style: dashed; }
.border-neutral-100 { border-color: #f5f5f5; }

.text-sm { font-size: 14px; }
.text-lg { font-size: 18px; }
.text-xl { font-size: 20px; }
.text-2xl { font-size: 24px; }
.text-5xl { font-size: 44px; }

.font-bold { font-weight: bold; }
.font-black { font-weight: 900; }
.font-medium { font-weight: 500; }
.italic { font-style: italic; }
.uppercase { text-transform: uppercase; }
.leading-relaxed { line-height: 1.6; }
.tracking-tight { letter-spacing: -0.025em; }
.tracking-tighter { letter-spacing: -0.05em; }
.tracking-wider { letter-spacing: 0.05em; }
.tracking-widest { letter-spacing: 0.1em; }

.bg-white { background-color: #ffffff; }
.bg-white-80 { background-color: rgba(255, 255, 255, 0.85); }
.bg-neutral-100 { background-color: #f5f5f5; }

.z-50 { z-index: 50; }
.-z-10 { z-index: -10; }

.hover-bg-neutral {
  &:active { background-color: rgba(0, 0, 0, 0.05); }
}

.block { display: block; }
.text-center { text-align: center; }

.space-y-2 { view + view { margin-top: 8rpx; } }
.space-y-6 { view + view { margin-top: 24rpx; } }
</style>
