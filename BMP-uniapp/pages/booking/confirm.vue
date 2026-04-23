<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="header-inner">
        <view class="icon-btn" @tap="handleBack">
          <uni-icons type="left" size="20" color="#1a1c1c" />
        </view>
        <text class="header-title">订单确认</text>
        <view class="header-tag">{{ isPresidentFlow ? 'ADMIN' : 'MEMBER' }}</view>
      </view>
    </view>

    <scroll-view scroll-y class="main" :style="{ paddingTop: `${statusBarHeight + 64}px` }">
      <view class="content">
        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载订单信息…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadBooking">重新加载</view>
        </view>

        <template v-else-if="booking">
          <view class="hero-card">
            <text class="hero-kicker">BOOKING ORDER</text>
            <text class="hero-title">{{ booking.venueName || fallbackSummary.venueName || '场地预约' }}</text>
            <text class="hero-sub">{{ booking.courtName || fallbackSummary.courtName || '待确认场地' }}</text>
            <view class="hero-grid">
              <view class="hero-cell">
                <text class="cell-label">日期</text>
                <text class="cell-value">{{ displayDate }}</text>
              </view>
              <view class="hero-cell">
                <text class="cell-label">时段</text>
                <text class="cell-value">{{ displayTime }}</text>
              </view>
            </view>
          </view>

          <view class="card">
            <view class="section-head">
              <text class="section-title">订单状态</text>
              <text class="status-pill" :class="statusClass">{{ statusText }}</text>
            </view>

            <view class="row">
              <text class="label">预约单号</text>
              <text class="value">{{ booking.bookingNo || `#${booking.id}` }}</text>
            </view>
            <view class="row">
              <text class="label">支付状态</text>
              <text class="value">{{ paymentStatusText }}</text>
            </view>
            <view class="row">
              <text class="label">会员余额</text>
              <text class="value">¥{{ memberBalanceText }}</text>
            </view>
          </view>

          <view class="card">
            <view class="section-head">
              <text class="section-title">支付方式</text>
              <text class="section-note">{{ isPresidentFlow ? '会长端统一使用余额代收' : '用户端当前仅开放余额支付' }}</text>
            </view>

            <view
              v-for="item in paymentOptions"
              :key="item.value"
              class="payment-item"
              :class="{ active: selectedPayment === item.value, disabled: item.disabled }"
              @tap="selectPayment(item.value, item.disabled)"
            >
              <view>
                <text class="payment-title">{{ item.label }}</text>
                <text class="payment-desc">{{ item.desc }}</text>
              </view>
              <uni-icons
                :type="selectedPayment === item.value ? 'checkbox-filled' : 'circle'"
                size="22"
                :color="selectedPayment === item.value ? '#ff6600' : '#d4d4d8'"
              />
            </view>
          </view>

          <view class="card">
            <view class="section-head">
              <text class="section-title">费用信息</text>
            </view>

            <view class="row">
              <text class="label">订单金额</text>
              <text class="value">¥{{ amountText }}</text>
            </view>
            <view class="row clickable" @tap="openFeeDetail">
              <text class="label">费用明细</text>
              <view class="inline-action">
                <text class="value">查看详情</text>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>
            </view>
            <view class="row total">
              <text class="label">应付金额</text>
              <text class="total-value">¥{{ amountText }}</text>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>

    <view v-if="booking" class="footer">
      <view class="footer-left">
        <text class="footer-label">待支付金额</text>
        <text class="footer-value">¥{{ amountText }}</text>
      </view>
      <button class="footer-btn" :class="{ disabled: submitDisabled }" @tap="handlePayment">
        {{ submitText }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBookingDetail, processMemberBookingPayment, processPayment, type BookingItem } from '@/api/booking'
import { safeNavigateBack } from '@/utils/navigation'
import { useCurrentMember } from '@/composables/useCurrentMember'
import { useUserStore } from '@/store/modules/user'

type FallbackSummary = {
  venueName?: string
  courtName?: string
  date?: string
  slot?: string
  bookingId?: number
}

type PaymentOption = {
  value: 'BALANCE'
  label: string
  desc: string
  disabled?: boolean
}

const userStore = useUserStore()
const { currentMember, fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(20)
const loading = ref(true)
const errorText = ref('')
const submitting = ref(false)
const bookingId = ref(0)
const booking = ref<BookingItem | null>(null)
const returnUrl = ref('/pages/booking/list')
const fallbackSummary = ref<FallbackSummary>({})
const selectedPayment = ref<'BALANCE'>('BALANCE')

const isPresidentFlow = computed(() => {
  const role = userStore.userInfo?.role || ''
  return role === 'PRESIDENT' || role === 'VENUE_MANAGER' || returnUrl.value.includes('/pages/president/')
})

const paymentOptions = computed<PaymentOption[]>(() => {
  return [
    {
      value: 'BALANCE',
      label: '余额支付',
      desc: isPresidentFlow.value ? '直接按会员余额完成收款' : '立即扣减会员余额并完成确认'
    }
  ]
})

const displayDate = computed(() => booking.value?.bookingDate || fallbackSummary.value.date || '--')
const displayTime = computed(() => {
  if (booking.value?.startTime && booking.value?.endTime) {
    return `${String(booking.value.startTime).slice(0, 5)} - ${String(booking.value.endTime).slice(0, 5)}`
  }
  return fallbackSummary.value.slot || '--'
})
const amountText = computed(() => Number(booking.value?.orderAmount || 0).toFixed(2))
const memberBalanceText = computed(() => Number(currentMember.value?.balance || 0).toFixed(2))

const statusText = computed(() => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '已支付',
    3: '进行中',
    4: '已完成'
  }
  return statusMap[Number(booking.value?.status ?? 1)] || '未知状态'
})

const paymentStatusText = computed(() => {
  const statusMap: Record<number, string> = {
    0: '未支付',
    1: '已支付',
    2: '已退款'
  }
  return statusMap[Number(booking.value?.paymentStatus ?? 0)] || '未知'
})

const statusClass = computed(() => {
  const status = Number(booking.value?.status ?? 1)
  if (status === 1) return 'pending'
  if (status === 3) return 'ongoing'
  if (status === 4) return 'done'
  if (status === 0) return 'cancelled'
  return 'paid'
})

const submitDisabled = computed(() => {
  if (submitting.value || !booking.value) return true
  return Number(booking.value.status ?? 1) !== 1
})

const submitText = computed(() => {
  if (submitting.value) return '处理中...'
  if (Number(booking.value?.status ?? 1) !== 1) return '订单已处理'
  return isPresidentFlow.value ? '确认收款并完成支付' : '确认支付'
})

function selectPayment(value: PaymentOption['value'], disabled?: boolean) {
  selectedPayment.value = value
}

async function loadBooking() {
  if (!bookingId.value) {
    errorText.value = '缺少预约订单参数'
    loading.value = false
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    booking.value = await getBookingDetail(bookingId.value)
    if (!isPresidentFlow.value) {
      await fetchCurrentMember(true)
    }
  } catch (error) {
    console.error('加载预约详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载预约详情失败'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  safeNavigateBack(returnUrl.value)
}

function openFeeDetail() {
  if (!booking.value) return
  const data = encodeURIComponent(JSON.stringify({
    ...fallbackSummary.value,
    bookingId: booking.value.id,
    totalAmount: Number(booking.value.orderAmount || 0),
    payableAmount: Number(booking.value.orderAmount || 0)
  }))
  uni.navigateTo({
    url: `/pages/booking/fee-detail?bookingId=${booking.value.id}&data=${data}&returnUrl=${encodeURIComponent(returnUrl.value)}`
  })
}

async function handlePayment() {
  if (submitDisabled.value || !booking.value) return

  try {
    submitting.value = true
    uni.showLoading({ title: '支付确认中...' })

    if (isPresidentFlow.value) {
      await processPayment(booking.value.id, selectedPayment.value)
    } else {
      await processMemberBookingPayment(booking.value.id, selectedPayment.value)
    }

    if (isPresidentFlow.value) {
      await loadBooking()
    } else {
      await Promise.all([loadBooking(), fetchCurrentMember(true)])
    }
    uni.hideLoading()
    uni.showToast({ title: '支付成功', icon: 'success' })

    setTimeout(() => {
      uni.reLaunch({ url: returnUrl.value })
    }, 900)
  } catch (error) {
    console.error('预约支付失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error instanceof Error ? error.message : '支付失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

onLoad((options?: Record<string, string | undefined>) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20

  if (options?.bookingId) {
    bookingId.value = Number(options.bookingId)
  }
  if (options?.returnUrl) {
    returnUrl.value = decodeURIComponent(options.returnUrl)
  }
  if (options?.data) {
    try {
      fallbackSummary.value = JSON.parse(decodeURIComponent(options.data))
      if (!bookingId.value && fallbackSummary.value.bookingId) {
        bookingId.value = Number(fallbackSummary.value.bookingId)
      }
    } catch (error) {
      console.error('解析预约摘要失败:', error)
    }
  }

  void loadBooking()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.header {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  z-index: 30;
  background: rgba(249, 249, 249, 0.94);
  backdrop-filter: blur(18px);
}

.header-inner {
  min-height: 64px;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 34rpx;
  font-weight: 900;
}

.header-tag {
  min-width: 96rpx;
  padding: 10rpx 16rpx;
  border-radius: 9999rpx;
  background: #fff1e8;
  color: #a33e00;
  font-size: 20rpx;
  font-weight: 800;
  text-align: center;
}

.main {
  height: 100vh;
}

.content {
  padding: 24rpx 20rpx 220rpx;
}

.hero-card,
.card,
.state-card {
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 12rpx 30rpx rgba(26, 28, 28, 0.04);
}

.hero-card {
  padding: 32rpx 28rpx;
  background: linear-gradient(135deg, #241714 0%, #59372d 58%, #9e430b 100%);
}

.hero-kicker,
.cell-label,
.section-note {
  font-size: 20rpx;
  color: rgba(255, 241, 234, 0.74);
}

.hero-title {
  display: block;
  margin-top: 12rpx;
  font-size: 44rpx;
  font-weight: 900;
  color: #ffffff;
}

.hero-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: rgba(255, 241, 234, 0.86);
}

.hero-grid {
  margin-top: 22rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.hero-cell {
  padding: 20rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.12);
}

.cell-value {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  font-weight: 800;
  color: #ffffff;
}

.card {
  margin-top: 22rpx;
  padding: 28rpx 26rpx;
}

.section-head,
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 900;
}

.status-pill {
  padding: 8rpx 16rpx;
  border-radius: 9999rpx;
  font-size: 20rpx;
  font-weight: 800;
}

.status-pill.pending {
  background: #fff1e8;
  color: #a33e00;
}

.status-pill.paid,
.status-pill.ongoing {
  background: #d8e9ff;
  color: #0e406a;
}

.status-pill.done {
  background: #e8f7ee;
  color: #0f8f54;
}

.status-pill.cancelled {
  background: #ffe1dc;
  color: #9a1e0f;
}

.row {
  margin-top: 18rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid #f1f1f1;
}

.row.total {
  align-items: flex-end;
}

.row.clickable {
  cursor: pointer;
}

.label,
.payment-desc {
  color: #6b7280;
  font-size: 24rpx;
}

.value {
  font-size: 26rpx;
  font-weight: 700;
  text-align: right;
}

.inline-action {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.payment-item {
  margin-top: 16rpx;
  padding: 22rpx;
  border-radius: 22rpx;
  background: #f8f7f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  border: 2rpx solid transparent;
}

.payment-item.active {
  border-color: #ff6600;
  background: #fff3eb;
}

.payment-item.disabled {
  opacity: 0.56;
}

.payment-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.payment-desc {
  display: block;
  margin-top: 8rpx;
}

.total-value {
  font-size: 40rpx;
  font-weight: 900;
  color: #a33e00;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  padding: 18rpx 24rpx calc(env(safe-area-inset-bottom) + 18rpx);
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(16px);
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.footer-left {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.footer-label {
  font-size: 20rpx;
  color: #6b7280;
}

.footer-value {
  margin-top: 6rpx;
  font-size: 42rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.footer-btn {
  min-width: 280rpx;
  height: 92rpx;
  border: none;
  border-radius: 9999rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 900;
}

.footer-btn.disabled {
  opacity: 0.45;
}

.state-card {
  margin-top: 24rpx;
  padding: 90rpx 28rpx;
  text-align: center;
}

.state-text {
  font-size: 28rpx;
  color: #777777;
}

.state-action {
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

.spinner {
  width: 48rpx;
  height: 48rpx;
  margin: 0 auto 18rpx;
  border: 4rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 9999rpx;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
