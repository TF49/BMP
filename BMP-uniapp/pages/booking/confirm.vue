<template>
  <view class="confirm-order bg-surface min-h-screen">
    <!-- TopAppBar -->
    <view class="sticky-header bg-white-80 backdrop-blur-md z-50 px-6 py-4 flex flex-row items-center justify-between border-b border-neutral-100" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="flex flex-row items-center gap-4">
        <view class="p-2 rounded-full hover-bg-neutral" @tap="goBack">
          <uni-icons type="left" size="20" color="#1a1c1c"></uni-icons>
        </view>
        <text class="font-headline font-bold text-lg text-on-surface">确认订单</text>
      </view>
      <view class="flex flex-row items-center gap-2" :style="{ marginRight: navBarMarginRight + 'px' }">
        <text class="font-headline font-black text-primary italic text-xl tracking-tighter">KINETIC</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-content" :style="{ height: scrollHeight }">
      <view class="px-6 py-6 pb-48">
      <!-- Order Summary Card -->
      <view class="mb-8">
        <view class="bg-white rounded-xl p-6 card-glow border-l-4 border-primary relative overflow-hidden">
          <view class="flex flex-row justify-between items-start mb-6">
            <view class="space-y-1">
              <view class="flex flex-row items-center gap-2">
                <view class="w-1-5 h-1-5 rounded-full bg-primary"></view>
                <text class="text-[10px] uppercase tracking-[0.2em] text-secondary font-bold">VENUE BOOKING</text>
              </view>
              <text class="text-2xl font-black text-on-surface tracking-tight leading-tight block">{{ bookingInfo.venueName }}</text>
            </view>
            <view class="bg-primary text-white px-3 py-1-5 rounded-lg text-sm font-black italic shadow-sm">
              {{ bookingInfo.courtName }}
            </view>
          </view>
          <view class="grid-2 gap-4">
            <view class="bg-surface-low p-4 rounded-xl border border-neutral-100">
              <view class="flex flex-row items-center gap-2 mb-2">
                <uni-icons type="calendar" size="16" color="#FF6600"></uni-icons>
                <text class="text-[10px] text-secondary font-bold uppercase tracking-wider">DATE</text>
              </view>
              <text class="font-bold text-sm">{{ bookingInfo.date }}</text>
            </view>
            <view class="bg-surface-low p-4 rounded-xl border border-neutral-100">
              <view class="flex flex-row items-center gap-2 mb-2">
                <uni-icons type="refreshtime" size="16" color="#FF6600"></uni-icons>
                <text class="text-[10px] text-secondary font-bold uppercase tracking-wider">SLOT</text>
              </view>
              <text class="font-bold text-sm">{{ bookingInfo.slot }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Payment Method Selection -->
      <view class="mb-10">
        <view class="flex flex-row items-center justify-between mb-4 px-1">
          <text class="text-[11px] font-black uppercase tracking-[0.15em] text-secondary/70">选择支付方式</text>
          <text class="text-[10px] text-primary font-bold">安全加密支付</text>
        </view>
        <view class="space-y-3">
          <!-- Balance Payment -->
          <view 
            class="payment-option p-5 rounded-2xl flex flex-row items-center justify-between border-2 transition-all"
            :class="selectedPayment === 'balance' ? 'border-primary bg-white shadow-lg shadow-primary/5' : 'border-transparent bg-white card-glow'"
            @tap="selectedPayment = 'balance'"
          >
            <view class="flex flex-row items-center gap-4">
              <view class="w-12 h-12 rounded-xl bg-primary-10 flex items-center justify-center shrink-0">
                <uni-icons type="wallet-filled" size="24" color="#FF6600"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface text-base block">余额支付</text>
                <view class="flex flex-row items-center">
                  <text class="text-[11px] text-secondary font-medium">可用余额: </text>
                  <text class="text-[11px] text-primary font-bold ml-1">¥1,240.50</text>
                </view>
              </view>
            </view>
            <uni-icons v-if="selectedPayment === 'balance'" type="checkbox-filled" size="24" color="#FF6600"></uni-icons>
            <view v-else class="w-6 h-6 rounded-full border-2 border-neutral-200 shrink-0"></view>
          </view>

          <!-- WeChat Pay -->
          <view 
            class="payment-option p-5 rounded-2xl flex flex-row items-center justify-between border-2 transition-all"
            :class="selectedPayment === 'wechat' ? 'border-primary bg-white shadow-lg shadow-primary/5' : 'border-transparent bg-white card-glow'"
            @tap="selectedPayment = 'wechat'"
          >
            <view class="flex flex-row items-center gap-4">
              <view class="w-12 h-12 rounded-xl bg-green-50 flex items-center justify-center shrink-0">
                <uni-icons type="weixin" size="24" color="#07C160"></uni-icons>
              </view>
              <text class="font-bold text-on-surface text-base">微信支付</text>
            </view>
            <uni-icons v-if="selectedPayment === 'wechat'" type="checkbox-filled" size="24" color="#FF6600"></uni-icons>
            <view v-else class="w-6 h-6 rounded-full border-2 border-neutral-200 shrink-0"></view>
          </view>

          <!-- Alipay -->
          <view 
            class="payment-option p-5 rounded-2xl flex flex-row items-center justify-between border-2 transition-all"
            :class="selectedPayment === 'alipay' ? 'border-primary bg-white shadow-lg shadow-primary/5' : 'border-transparent bg-white card-glow'"
            @tap="selectedPayment = 'alipay'"
          >
            <view class="flex flex-row items-center gap-4">
              <view class="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center shrink-0">
                <uni-icons type="phone-filled" size="24" color="#1677FF"></uni-icons>
              </view>
              <text class="font-bold text-on-surface text-base">支付宝</text>
            </view>
            <uni-icons v-if="selectedPayment === 'alipay'" type="checkbox-filled" size="24" color="#FF6600"></uni-icons>
            <view v-else class="w-6 h-6 rounded-full border-2 border-neutral-200 shrink-0"></view>
          </view>
        </view>
      </view>

      <!-- Cost Breakdown -->
      <view class="bg-white rounded-2xl p-6 card-glow border border-neutral-100">
        <text class="text-[11px] font-black uppercase tracking-[0.15em] text-secondary/70 mb-5 block">费用结算</text>
        <view class="space-y-4">
          <view class="flex flex-row justify-between items-center">
            <text class="text-secondary font-medium text-sm">订单总额 ({{ bookingInfo.duration || 2 }}小时)</text>
            <text class="font-bold text-on-surface">¥{{ bookingInfo.totalAmount.toFixed(2) }}</text>
          </view>
          <view class="flex flex-row justify-between items-center text-primary">
            <view class="flex flex-row items-center gap-2">
              <text class="text-sm font-medium">会员折扣</text>
              <view class="bg-primary-10 px-2 py-0.5 rounded ml-1">
                <text class="text-[10px] italic font-black uppercase text-primary">GOLD VIP</text>
              </view>
            </view>
            <text class="font-black">-¥{{ bookingInfo.vipDiscount.toFixed(2) }}</text>
          </view>
          <view class="h-[1px] border-t border-dashed border-neutral-200 my-2 w-full"></view>
          <view class="flex flex-row justify-between items-end pt-2">
            <view>
              <text class="font-black text-lg text-on-surface block">实付金额</text>
              <text class="text-[10px] text-secondary font-bold uppercase tracking-widest mt-1 block">Total Payable</text>
            </view>
            <view class="text-right">
              <text class="text-4xl font-black text-primary tracking-tighter leading-none italic">¥{{ bookingInfo.payableAmount.toFixed(2) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Terms Agreement -->
      <view class="flex flex-row items-start gap-3 px-2 mt-8 mb-4">
        <view class="mt-0.5 flex flex-row items-center justify-center shrink-0" @tap="agreed = !agreed">
          <uni-icons :type="agreed ? 'checkbox-filled' : 'circle'" size="20" :color="agreed ? '#FF6600' : '#d1d5db'"></uni-icons>
        </view>
        <view class="flex-1">
          <text class="text-[13px] text-secondary leading-snug font-medium">
            我已阅读并同意
            <text class="text-primary font-bold decoration-primary/30 underline" @tap.stop="openRule('venue')">《场馆预约规则》</text>
            及
            <text class="text-primary font-bold decoration-primary/30 underline" @tap.stop="openRule('cancel')">《取消政策》</text>
          </text>
        </view>
      </view>
      </view>
    </scroll-view>

    <!-- Bottom Action Bar -->
    <view class="fixed-bottom bg-white-80 backdrop-blur-xl border-t border-neutral-100 px-6 pt-4 pb-safe">
      <view class="flex flex-row items-center gap-5">
        <view class="flex flex-col items-center justify-center text-secondary/60 shrink-0 px-2 transition-all active-scale" @tap="showDetails">
          <uni-icons type="list" size="24" color="#5f5e5e"></uni-icons>
          <text class="font-bold text-[9px] uppercase tracking-tighter mt-0.5">明细</text>
        </view>
        <button 
          class="flex-1 bg-primary text-white rounded-2xl py-3 energy-shadow flex flex-col items-center justify-center overflow-hidden relative transition-all"
          :class="{'opacity-50 grayscale': !agreed}"
          @tap="handlePayment"
        >
          <view class="absolute inset-0 bg-white-10 opacity-0 active-opacity-20 transition-opacity"></view>
          <text class="font-headline font-black text-base uppercase tracking-widest leading-none block">立即支付</text>
          <text class="text-[9px] font-bold opacity-80 mt-1 uppercase tracking-tighter block">SECURE CHECKOUT • ¥{{ bookingInfo.payableAmount.toFixed(2) }}</text>
        </button>
      </view>
    </view>

    <!-- Decorative Elements -->
    <view class="fixed top-1/4 -right-24 w-72 h-72 bg-primary/5 rounded-full blur-large -z-10 animate-pulse"></view>
    <view class="fixed bottom-1/4 -left-24 w-72 h-72 bg-orange-400/5 rounded-full blur-large -z-10 animate-pulse" style="animation-delay: 1s;"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const statusBarHeight = ref(44)
const navBarMarginRight = ref(0)
const selectedPayment = ref('balance')
const agreed = ref(false)

const scrollHeight = computed(() => {
  return `calc(100vh - ${statusBarHeight.value + 60}px)`
})

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

onLoad((options: any = {}) => {
  if (options.data) {
    try {
      const data = JSON.parse(decodeURIComponent(options.data))
      bookingInfo.value = { ...bookingInfo.value, ...data }
    } catch (e) {
      console.error('Parse booking data error:', e)
    }
  }
})

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  // #ifdef MP
  try {
    const menuInfo = uni.getMenuButtonBoundingClientRect()
    if (menuInfo) {
      navBarMarginRight.value = Math.max(0, systemInfo.windowWidth - menuInfo.left + 8) 
    }
  } catch (e) {}
  // #endif
})

const goBack = () => {
  safeNavigateBack('/pages/index/index')
}

const openRule = (type: string) => {
  console.log('Open rule:', type)
  uni.showModal({
    title: type === 'venue' ? '场馆预约规则' : '取消政策',
    content: '此处显示规则详细内容...',
    showCancel: false,
    confirmColor: '#FF6600'
  })
}

const showDetails = () => {
  uni.navigateTo({
    url: `/pages/booking/fee-detail?data=${encodeURIComponent(JSON.stringify(bookingInfo.value))}`
  })
}

const handlePayment = () => {
  if (!agreed.value) {
    uni.showToast({ title: '请先阅读并同意相关规则', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '支付中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '支付成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/booking/list' })
    }, 1500)
  }, 2000)
}
</script>

<style lang="scss" scoped>
.confirm-order {
  font-family: 'Lexend', sans-serif;
}

.sticky-header {
  position: sticky;
  top: 0;
}

.card-glow {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.energy-shadow {
  box-shadow: 0 10px 30px -10px rgba(255, 102, 0, 0.4);
}

.fixed-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 -10px 30px rgba(0,0,0,0.03);
}

.pb-safe {
  padding-bottom: calc(env(safe-area-inset-bottom) + 24rpx);
}

.active-scale:active {
  transform: scale(0.92);
}

.active-opacity-20:active {
  opacity: 0.2;
}

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
.text-primary { color: #FF6600; }
.bg-primary { background-color: #FF6600; }
.bg-surface { background-color: #f8f9fa; }
.bg-surface-low { background-color: #f3f3f3; }
.border-primary { border-color: #FF6600; }

/* Utilities */
.flex { display: flex; }
.flex-row { flex-direction: row; }
.flex-col { flex-direction: column; }
.items-center { align-items: center; }
.items-start { align-items: flex-start; }
.items-end { align-items: flex-end; }
.justify-between { justify-content: space-between; }
.justify-center { justify-content: center; }
.flex-1 { flex: 1; }
.shrink-0 { flex-shrink: 0; }
.relative { position: relative; }
.absolute { position: absolute; }
.inset-0 { top: 0; right: 0; bottom: 0; left: 0; }
.overflow-hidden { overflow: hidden; }

.p-2 { padding: 8rpx; }
.p-4 { padding: 16rpx; }
.p-5 { padding: 20rpx; }
.p-6 { padding: 24rpx; }
.px-1 { padding-left: 4rpx; padding-right: 4rpx; }
.px-2 { padding-left: 8rpx; padding-right: 8rpx; }
.px-6 { padding-left: 24rpx; padding-right: 24rpx; }
.py-1-5 { padding-top: 6rpx; padding-bottom: 6rpx; }
.py-4 { padding-top: 16rpx; padding-bottom: 16rpx; }
.py-6 { padding-top: 24rpx; padding-bottom: 24rpx; }
.pb-6 { padding-bottom: 24rpx; }
.pb-8 { padding-bottom: 32rpx; }
.pb-12 { padding-bottom: 48rpx; }
.pt-2 { padding-top: 8rpx; }
.pt-4 { padding-top: 16rpx; }

.m-0 { margin: 0; }
.mb-1 { margin-bottom: 4rpx; }
.mb-2 { margin-bottom: 8rpx; }
.mb-4 { margin-bottom: 16rpx; }
.mb-5 { margin-bottom: 20rpx; }
.mb-6 { margin-bottom: 24rpx; }
.mb-8 { margin-bottom: 32rpx; }
.mb-10 { margin-bottom: 40rpx; }
.mt-0-5 { margin-top: 2rpx; }
.mt-1 { margin-top: 4rpx; }
.mt-8 { margin-top: 32rpx; }
.ml-1 { margin-left: 4rpx; }
.my-2 { margin-top: 8rpx; margin-bottom: 8rpx; }

.w-full { width: 100%; }
.h-full { height: 100%; }
.w-1-5 { width: 6rpx; }
.h-1-5 { height: 6rpx; }
.w-6 { width: 24px; }
.h-6 { height: 24px; }
.w-10 { width: 40px; }
.h-10 { height: 40px; }
.w-12 { width: 48px; }
.h-12 { height: 48px; }
.w-72 { width: 288px; }
.h-72 { height: 288px; }

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
}

.gap-2 { gap: 8rpx; }
.gap-3 { gap: 12rpx; }
.gap-4 { gap: 16rpx; }
.gap-5 { gap: 20rpx; }

.rounded { border-radius: 4rpx; }
.rounded-lg { border-radius: 16rpx; }
.rounded-xl { border-radius: 24rpx; }
.rounded-2xl { border-radius: 32rpx; }
.rounded-full { border-radius: 9999px; }

.border { border-width: 1rpx; border-style: solid; }
.border-2 { border-width: 2rpx; border-style: solid; }
.border-b { border-bottom-width: 1rpx; border-style: solid; }
.border-t { border-top-width: 2rpx; border-style: solid; }
.border-l-4 { border-left-width: 4px; border-style: solid; }
.border-dashed { border-style: dashed; }
.border-neutral-100 { border-color: #f5f5f5; }
.border-neutral-200 { border-color: #e5e5e5; }
.border-transparent { border-color: transparent; }

.text-white { color: #ffffff; }
.text-base { font-size: 16px; }
.text-sm { font-size: 14px; }
.text-lg { font-size: 18px; }
.text-xl { font-size: 20px; }
.text-2xl { font-size: 24px; }
.text-4xl { font-size: 36px; }

.font-bold { font-weight: bold; }
.font-black { font-weight: 900; }
.font-medium { font-weight: 500; }
.italic { font-style: italic; }
.uppercase { text-transform: uppercase; }
.underline { text-decoration: underline; }
.leading-tight { line-height: 1.25; }
.leading-none { line-height: 1; }
.leading-snug { line-height: 1.375; }
.tracking-tight { letter-spacing: -0.025em; }
.tracking-wider { letter-spacing: 0.05em; }
.tracking-widest { letter-spacing: 0.1em; }

.bg-white { background-color: #ffffff; }
.bg-white-80 { background-color: rgba(255, 255, 255, 0.85); }
.bg-white-10 { background-color: rgba(255, 255, 255, 0.1); }
.bg-primary-10 { background-color: rgba(255, 102, 0, 0.1); }
.bg-neutral-50 { background-color: #fafafa; }
.bg-neutral-100 { background-color: #f5f5f5; }
.bg-green-50 { background-color: #f0fdf4; }
.bg-blue-50 { background-color: #eff6ff; }

.blur-large { filter: blur(80px); }
.shadow-lg { box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05); }

.grayscale { filter: grayscale(1); }
.opacity-0 { opacity: 0; }
.opacity-50 { opacity: 0.5; }
.opacity-80 { opacity: 0.8; }

.transition-all { transition: all 0.3s ease; }
.transition-opacity { transition: opacity 0.2s ease; }

.z-50 { z-index: 50; }
.z-100 { z-index: 100; }
.-z-10 { z-index: -10; }

.hover-bg-neutral {
  &:active { background-color: rgba(0, 0, 0, 0.05); }
}

.payment-option {
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  &:active { transform: scale(0.98); }
}

.block { display: block; }

.space-y-1 { view + view { margin-top: 4rpx; } }
.space-y-3 { view + view { margin-top: 12rpx; } }
.space-y-4 { view + view { margin-top: 16rpx; } }

.pb-48 { padding-bottom: 300rpx; }
</style>

