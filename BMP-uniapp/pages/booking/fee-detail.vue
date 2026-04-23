<template>
  <view class="fee-detail bg-surface min-h-screen">
    <view class="sticky-header bg-white-80 backdrop-blur-md z-50 px-6 py-4 flex flex-row items-center gap-4 border-b border-neutral-100" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="p-2 rounded-full hover-bg-neutral" @tap="goBack">
        <uni-icons type="left" size="20" color="#1a1c1c" />
      </view>
      <text class="font-headline font-bold text-lg text-on-surface">费用明细</text>
    </view>

    <scroll-view scroll-y class="main-content" :style="{ height: scrollHeight }">
      <view class="px-6 py-8">
        <view v-if="loading" class="bg-white rounded-3xl p-8 text-center card-glow">
          <text class="text-secondary">正在加载费用信息...</text>
        </view>

        <view v-else-if="errorText" class="bg-white rounded-3xl p-8 text-center card-glow">
          <text class="text-secondary block">{{ errorText }}</text>
          <view class="retry-btn" @tap="loadFeeDetail">重新加载</view>
        </view>

        <template v-else>
          <view class="text-center mb-10">
            <text class="text-[11px] text-secondary font-bold uppercase tracking-[0.3em] block mb-2">Total Payable</text>
            <text class="text-5xl font-black text-primary tracking-tighter italic">¥{{ payableAmount.toFixed(2) }}</text>
          </view>

          <view class="bg-white rounded-3xl p-8 card-glow border border-neutral-100 relative overflow-hidden">
            <view class="absolute top-0 right-0 w-24 h-24 bg-primary-10 rounded-full blur-3xl -mr-12 -mt-12" />

            <view class="mb-8 border-b border-dashed border-neutral-100 pb-6">
              <text class="text-[10px] text-secondary font-black uppercase tracking-widest mb-3 block">场馆信息 VENUE</text>
              <text class="text-xl font-bold text-on-surface block mb-2">{{ displayVenueName }}</text>
              <view class="flex flex-row items-center gap-3 wrap">
                <text class="bg-primary-10 text-primary text-[10px] px-2 py-0.5 rounded font-black italic">{{ displayCourtName }}</text>
                <text class="text-secondary text-sm font-medium">{{ displayDate }}</text>
                <text class="text-secondary text-sm font-medium">{{ displaySlot }}</text>
              </view>
            </view>

            <view class="space-y-6">
              <text class="text-[10px] text-secondary font-black uppercase tracking-widest mb-4 block">费用说明 BREAKDOWN</text>

              <view class="flex flex-row justify-between items-center">
                <view>
                  <text class="text-sm font-bold text-on-surface block">订单金额</text>
                  <text class="text-[10px] text-secondary font-medium block">来自当前预约订单的真实金额</text>
                </view>
                <text class="font-bold text-on-surface">¥{{ totalAmount.toFixed(2) }}</text>
              </view>

              <view v-if="showDiscount" class="flex flex-row justify-between items-center text-primary">
                <view>
                  <text class="text-sm font-black italic block">优惠抵扣</text>
                  <text class="text-[10px] opacity-80 font-medium block">仅展示上游真实传入的优惠金额</text>
                </view>
                <text class="font-black">-¥{{ vipDiscount.toFixed(2) }}</text>
              </view>

              <view class="h-[1px] bg-neutral-100 w-full" />

              <view class="flex flex-row justify-between items-end pt-4">
                <view>
                  <text class="text-[11px] font-black text-on-surface uppercase tracking-wider block">应付金额</text>
                  <text class="text-[9px] text-secondary font-medium block mt-0.5 uppercase">Actual Payable</text>
                </view>
                <text class="text-2xl font-black text-on-surface tracking-tighter">¥{{ payableAmount.toFixed(2) }}</text>
              </view>
            </view>
          </view>

          <view class="mt-8 bg-neutral-100/50 rounded-2xl p-5">
            <view class="flex flex-row items-center gap-2 mb-3">
              <uni-icons type="info" size="14" color="#5f5e5e" />
              <text class="text-[10px] text-secondary font-bold uppercase tracking-widest">说明 NOTES</text>
            </view>
            <view class="space-y-2">
              <text class="text-[11px] text-secondary-60 leading-relaxed block">• 当前页面只展示真实订单金额和真实传入摘要，不再补默认折扣或单价演示数据。</text>
              <text class="text-[11px] text-secondary-60 leading-relaxed block">• 若上游未提供优惠拆分，则仅展示订单总额与应付金额。</text>
              <text class="text-[11px] text-secondary-60 leading-relaxed block">• 最终支付结果以订单确认页和支付结果页为准。</text>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBookingDetail } from '@/api/booking'
import { safeNavigateBack } from '@/utils/navigation'

type BookingSummary = {
  venueName?: string
  courtName?: string
  date?: string
  slot?: string
  totalAmount?: number
  vipDiscount?: number
  payableAmount?: number
  bookingId?: number
}

const statusBarHeight = ref(44)
const bookingId = ref(0)
const loading = ref(true)
const errorText = ref('')
const returnUrl = ref('/pages/booking/list')
const bookingDetail = ref<any | null>(null)
const summary = ref<BookingSummary>({})

const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value + 60}px)`)
const hasSummaryData = computed(() => {
  return Boolean(
    summary.value.venueName ||
    summary.value.courtName ||
    summary.value.date ||
    summary.value.slot ||
    summary.value.totalAmount != null ||
    summary.value.vipDiscount != null ||
    summary.value.payableAmount != null
  )
})
const totalAmount = computed(() => {
  if (summary.value.totalAmount != null) return Number(summary.value.totalAmount)
  return Number(bookingDetail.value?.orderAmount || 0)
})
const vipDiscount = computed(() => Number(summary.value.vipDiscount || 0))
const payableAmount = computed(() => {
  if (summary.value.payableAmount != null) return Number(summary.value.payableAmount)
  if (summary.value.totalAmount != null) {
    return Math.max(Number(summary.value.totalAmount) - vipDiscount.value, 0)
  }
  return Number(bookingDetail.value?.orderAmount || 0)
})
const showDiscount = computed(() => vipDiscount.value > 0)
const displayVenueName = computed(() => summary.value.venueName || bookingDetail.value?.venueName || '待确认场馆')
const displayCourtName = computed(() => summary.value.courtName || bookingDetail.value?.courtName || '待确认场地')
const displayDate = computed(() => summary.value.date || bookingDetail.value?.bookingDate || '--')
const displaySlot = computed(() => {
  if (summary.value.slot) return summary.value.slot
  const start = String(bookingDetail.value?.startTime || '').slice(0, 5)
  const end = String(bookingDetail.value?.endTime || '').slice(0, 5)
  if (start && end) return `${start} - ${end}`
  return '--'
})

async function loadFeeDetail() {
  if (!bookingId.value) {
    if (hasSummaryData.value) {
      loading.value = false
      errorText.value = ''
      return
    }
    loading.value = false
    errorText.value = '缺少订单参数'
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    bookingDetail.value = await getBookingDetail(bookingId.value)
  } catch (error) {
    console.error('加载费用明细失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载费用明细失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  safeNavigateBack(returnUrl.value)
}

onLoad((options: Record<string, string | undefined> = {}) => {
  if (options.bookingId) {
    bookingId.value = Number(options.bookingId)
  }
  if (options.returnUrl) {
    returnUrl.value = decodeURIComponent(options.returnUrl)
  }
  if (options.data) {
    try {
      summary.value = JSON.parse(decodeURIComponent(options.data))
      if (!bookingId.value && summary.value.bookingId) {
        bookingId.value = Number(summary.value.bookingId)
      }
    } catch (error) {
      console.error('解析费用摘要失败:', error)
    }
  }
})

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  void loadFeeDetail()
})
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

.retry-btn {
  width: 220rpx;
  height: 76rpx;
  margin: 22rpx auto 0;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.blur-large { filter: blur(80px); }
.wrap { flex-wrap: wrap; }

.text-on-surface { color: #1a1c1c; }
.text-secondary { color: #5f5e5e; }
.text-secondary-60 { color: rgba(95, 94, 94, 0.7); }
.text-primary { color: #FF6600; }
.bg-primary { background-color: #FF6600; }
.bg-surface { background-color: #f8f9fa; }
.bg-primary-10 { background-color: rgba(255, 102, 0, 0.1); }

.flex { display: flex; }
.flex-row { flex-direction: row; }
.items-center { align-items: center; }
.items-end { align-items: flex-end; }
.justify-between { justify-content: space-between; }
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

.mb-2 { margin-bottom: 8rpx; }
.mb-3 { margin-bottom: 12rpx; }
.mb-4 { margin-bottom: 16rpx; }
.mb-8 { margin-bottom: 32rpx; }
.mb-10 { margin-bottom: 40rpx; }
.mt-0-5 { margin-top: 2rpx; }

.w-full { width: 100%; }
.w-24 { width: 96px; }
.h-24 { height: 96px; }

.gap-2 { gap: 8rpx; }
.gap-3 { gap: 12rpx; }
.gap-4 { gap: 16rpx; }

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
.tracking-tighter { letter-spacing: -0.05em; }
.tracking-wider { letter-spacing: 0.05em; }
.tracking-widest { letter-spacing: 0.1em; }

.bg-white { background-color: #ffffff; }
.bg-white-80 { background-color: rgba(255, 255, 255, 0.85); }
.bg-neutral-100 { background-color: #f5f5f5; }

.z-50 { z-index: 50; }

.hover-bg-neutral {
  &:active { background-color: rgba(0, 0, 0, 0.05); }
}

.block { display: block; }
.text-center { text-align: center; }

.space-y-2 { view + view { margin-top: 8rpx; } }
.space-y-6 { view + view { margin-top: 24rpx; } }
</style>
