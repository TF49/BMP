<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
        <view class="header-inner">
          <view class="header-left" @tap="goBack">
            <uni-icons type="arrow-left" size="24" color="#ff6600" />
            <text class="header-title">预约详情</text>
          </view>
          <view class="header-avatar" @tap="goProfile">
            <image class="header-avatar-image" :src="avatarUrl" mode="aspectFill" />
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :style="{ height: scrollHeight }" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text class="state-text">加载预约详情中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text class="state-text">{{ errorText }}</text>
          <view class="retry-btn" @tap="loadDetail">重新加载</view>
        </view>

        <view v-else-if="detail" class="content">
          <!-- Status Banner -->
          <view class="status-banner" :class="`status-banner--${statusMeta.key}`">
            <view class="status-banner-bg" />
            <view class="status-banner-content">
              <uni-icons :type="statusMeta.icon" size="48" :color="statusMeta.iconColor" />
              <text class="status-banner-title">{{ statusMeta.text }}</text>
              <text class="status-banner-subtitle">订单编号: {{ detail.bookingNo }}</text>
            </view>
          </view>

          <!-- Member Info -->
          <view class="section">
            <view class="section-card">
              <text class="section-label">会员信息</text>
              <view class="member-info">
                <image class="member-avatar" :src="memberAvatar" mode="aspectFill" />
                <view class="member-details">
                  <view class="member-row">
                    <view class="member-main">
                      <text class="member-name">{{ detail.memberName || '未知会员' }}</text>
                      <view class="member-phone">
                        <uni-icons type="phone" size="14" color="#5f5e5e" />
                        <text>{{ formatPhone(String(detail.memberPhone || '')) }}</text>
                      </view>
                    </view>
                    <view v-if="detail.memberLevel" class="member-badge">
                      {{ detail.memberLevel }}
                    </view>
                  </view>
                </view>
              </view>
              <view v-if="detail.memberIdCard" class="member-extra">
                <text class="extra-label">身份证号</text>
                <text class="extra-value">{{ maskIdCard(String(detail.memberIdCard || '')) }}</text>
              </view>
            </view>
          </view>

          <!-- Booking Details -->
          <view class="section">
            <view class="section-card">
              <text class="section-label">场馆预约详情</text>
              <view class="detail-grid">
                <view class="detail-item detail-item--full">
                  <text class="detail-label">场馆名称</text>
                  <text class="detail-value detail-value--primary">{{ detail.venueName || '未知场馆' }}</text>
                </view>
                <view class="detail-item">
                  <text class="detail-label">场地编号</text>
                  <text class="detail-value detail-value--bold">{{ detail.courtName || '--' }}</text>
                </view>
                <view class="detail-item detail-item--highlight">
                  <text class="detail-label">预约日期</text>
                  <view class="detail-value-row">
                    <uni-icons type="calendar" size="16" color="#ff6600" />
                    <text class="detail-value">{{ formatDate(detail.bookingDate, 'YYYY-MM-DD') }}</text>
                  </view>
                </view>
                <view class="detail-item detail-item--highlight">
                  <text class="detail-label">时间段</text>
                  <view class="detail-value-row">
                    <uni-icons type="clock" size="16" color="#ff6600" />
                    <text class="detail-value">{{ timeSlotText }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- Payment Summary -->
          <view class="section">
            <view class="section-card">
              <text class="section-label">支付概览</text>
              <view class="payment-rows">
                <view class="payment-row">
                  <text class="payment-label">订单总额</text>
                  <text class="payment-value">¥{{ formatAmount(detail.orderAmount) }}</text>
                </view>
                <view v-if="discountAmount > 0" class="payment-row">
                  <text class="payment-label">优惠折扣</text>
                  <text class="payment-value payment-value--discount">- ¥{{ formatAmount(discountAmount) }}</text>
                </view>
                <view class="payment-row payment-row--total">
                  <text class="payment-label payment-label--bold">实际支付</text>
                  <text class="payment-value payment-value--total">¥{{ formatAmount(detail.amount) }}</text>
                </view>
              </view>
              <view class="payment-method">
                <uni-icons type="wallet" size="20" color="#ff6600" />
                <view class="payment-method-text">
                  <text class="payment-method-label">支付方式：</text>
                  <text class="payment-method-value">{{ paymentMethodText }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Remark -->
          <view v-if="detail.remark" class="section">
            <view class="remark-card">
              <text class="remark-label">备注信息</text>
              <text class="remark-text">{{ detail.remark }}</text>
            </view>
          </view>

          <view class="bottom-space" />
        </view>
      </scroll-view>

      <!-- Action Footer -->
      <view v-if="detail && !loading" class="footer">
        <view class="footer-inner">
          <button
            v-if="canCancel"
            class="footer-btn footer-btn--secondary"
            @tap="handleCancel"
          >
            取消预约
          </button>
          <button
            v-if="canCheckIn"
            class="footer-btn footer-btn--primary"
            @tap="handleCheckIn"
          >
            <uni-icons type="scan" size="20" color="#561d00" />
            <text>核销/入场</text>
          </button>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getBookingDetail, cancelBooking, type BookingItem } from '@/api/president/booking'
import { formatAmount, formatDate, formatPhone } from '@/utils/format'
import { getAvatarImage } from '@/utils/displayImage'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { BOOKING_STATUS, PAYMENT_METHOD_TEXT } from '@/utils/constant'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const statusBarHeight = ref(getSafeSystemInfo().statusBarHeight || 20)
const loading = ref(false)
const errorText = ref('')
const detail = ref<BookingItem | null>(null)
const bookingId = ref(0)

const avatarUrl = computed(() => getAvatarImage(userStore.userInfo?.avatar))
const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value}px - 96rpx)`)
const memberAvatar = computed(() => getAvatarImage(detail.value?.memberAvatar as string | undefined))

const statusMeta = computed(() => {
  const status = detail.value?.status
  switch (status) {
    case BOOKING_STATUS.PENDING_PAYMENT:
      return { key: 'pending', text: '待支付', icon: 'clock', iconColor: '#ff9800' }
    case BOOKING_STATUS.PAID:
      return { key: 'paid', text: '已支付 - 待核销', icon: 'checkmarkempty', iconColor: '#561d00' }
    case BOOKING_STATUS.IN_PROGRESS:
      return { key: 'ongoing', text: '进行中', icon: 'loop', iconColor: '#2196f3' }
    case BOOKING_STATUS.COMPLETED:
      return { key: 'completed', text: '已完成', icon: 'checkbox-filled', iconColor: '#4caf50' }
    case BOOKING_STATUS.CANCELLED:
      return { key: 'cancelled', text: '已取消', icon: 'close', iconColor: '#999999' }
    default:
      return { key: 'unknown', text: '未知状态', icon: 'help', iconColor: '#999999' }
  }
})

const timeSlotText = computed(() => {
  if (!detail.value) return '--'
  const start = detail.value.startTime?.slice(0, 5) || '--:--'
  const end = detail.value.endTime?.slice(0, 5) || '--:--'
  return `${start} - ${end}`
})

const paymentMethodText = computed(() => {
  const method = detail.value?.paymentMethod as keyof typeof PAYMENT_METHOD_TEXT | undefined
  return method ? PAYMENT_METHOD_TEXT[method] || '未知' : '未支付'
})

const discountAmount = computed(() => {
  if (!detail.value) return 0
  const order = Number(detail.value.orderAmount || 0)
  const actual = Number(detail.value.amount || 0)
  return Math.max(0, order - actual)
})

const canCancel = computed(() => {
  const status = detail.value?.status
  return status === BOOKING_STATUS.PENDING_PAYMENT || status === BOOKING_STATUS.PAID
})

const canCheckIn = computed(() => {
  const status = detail.value?.status
  return status === BOOKING_STATUS.PAID
})

function maskIdCard(idCard: string): string {
  if (!idCard || idCard.length < 8) return idCard
  return idCard.replace(/^(.{4})(.*)(.{4})$/, '$1**********$3')
}

async function loadDetail() {
  if (!bookingId.value) {
    errorText.value = '缺少预约ID'
    return
  }

  loading.value = true
  errorText.value = ''

  try {
    const res = await getBookingDetail(bookingId.value)
    detail.value = res
  } catch (error) {
    console.error('Failed to load booking detail:', error)
    errorText.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function handleCancel() {
  const { confirm } = await uni.showModal({
    title: '确认取消',
    content: '确定要取消此预约吗？取消后将无法恢复。'
  })

  if (!confirm) return

  try {
    await cancelBooking(bookingId.value)
    uni.showToast({ title: '取消成功', icon: 'success' })
    setTimeout(() => {
      goBack()
    }, 1500)
  } catch (error) {
    console.error('Failed to cancel booking:', error)
    uni.showToast({
      title: error instanceof Error ? error.message : '取消失败',
      icon: 'none'
    })
  }
}

function handleCheckIn() {
  uni.showToast({ title: '核销功能开发中', icon: 'none' })
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.BOOKING_LIST)
}

function goProfile() {
  uni.navigateTo({ url: PRESIDENT_PAGES.PROFILE })
}

onLoad((query?: Record<string, string | undefined>) => {
  statusBarHeight.value = getSafeSystemInfo().statusBarHeight || 20
  const id = Number(query?.id || 0)
  if (id > 0) {
    bookingId.value = id
    loadDetail()
  } else {
    errorText.value = '无效的预约ID'
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
  padding-bottom: 160rpx;
}

.header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: #f9f9f9;
}

.header-inner {
  height: 96rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-title {
  color: #1a1c1c;
  font-size: 32rpx;
  font-weight: 700;
}

.header-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 999rpx;
  overflow: hidden;
  border: 2rpx solid #ff6600;
  background: #ffffff;
}

.header-avatar-image {
  width: 100%;
  height: 100%;
  display: block;
}

.state-wrap {
  min-height: 480rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
}

.spinner {
  width: 44rpx;
  height: 44rpx;
  border-radius: 999rpx;
  border: 4rpx solid rgba(255, 102, 0, 0.16);
  border-top-color: #ff6600;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.state-text {
  color: #5f5e5e;
  font-size: 26rpx;
}

.retry-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 180rpx;
  height: 72rpx;
  padding: 0 28rpx;
  border-radius: 18rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 700;
}

.content {
  padding: 32rpx 24rpx;
}

.status-banner {
  position: relative;
  min-height: 280rpx;
  border-radius: 28rpx;
  padding: 48rpx 32rpx;
  overflow: hidden;
  margin-bottom: 32rpx;
}

.status-banner--pending {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
}

.status-banner--paid {
  background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
}

.status-banner--ongoing {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.status-banner--completed {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.status-banner--cancelled {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
}

.status-banner-bg {
  position: absolute;
  right: -40rpx;
  top: -40rpx;
  width: 240rpx;
  height: 240rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.2);
}

.status-banner-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.status-banner-title {
  font-size: 44rpx;
  font-weight: 900;
  color: #561d00;
  letter-spacing: -0.02em;
}

.status-banner--pending .status-banner-title {
  color: #e65100;
}

.status-banner--ongoing .status-banner-title {
  color: #0d47a1;
}

.status-banner--completed .status-banner-title {
  color: #1b5e20;
}

.status-banner--cancelled .status-banner-title {
  color: #424242;
}

.status-banner-subtitle {
  font-size: 20rpx;
  font-weight: 700;
  color: rgba(86, 29, 0, 0.8);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.section {
  margin-bottom: 24rpx;
}

.section-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.04);
}

.section-label {
  display: block;
  color: #5f5e5e;
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 24rpx;
}

.member-info {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.member-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  background: #f3f3f3;
}

.member-details {
  flex: 1;
  min-width: 0;
}

.member-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.member-main {
  flex: 1;
  min-width: 0;
}

.member-name {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1c;
  margin-bottom: 8rpx;
}

.member-phone {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #5f5e5e;
  font-size: 24rpx;
}

.member-badge {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #fff3e0;
  color: #e65100;
  font-size: 20rpx;
  font-weight: 800;
}

.member-extra {
  padding-top: 20rpx;
  border-top: 2rpx solid #f3f3f3;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.extra-label {
  color: #5f5e5e;
  font-size: 24rpx;
}

.extra-value {
  color: #1a1c1c;
  font-size: 24rpx;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32rpx;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.detail-item--full {
  grid-column: 1 / -1;
}

.detail-item--highlight {
  background: #f3f3f3;
  padding: 20rpx;
  border-radius: 16rpx;
}

.detail-label {
  color: #5f5e5e;
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-value {
  color: #1a1c1c;
  font-size: 32rpx;
  font-weight: 700;
}

.detail-value--primary {
  color: #ff6600;
  font-size: 36rpx;
  font-weight: 800;
}

.detail-value--bold {
  font-size: 36rpx;
  font-weight: 800;
}

.detail-value-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.payment-rows {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.payment-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-row--total {
  padding-top: 16rpx;
  border-top: 2rpx solid #f3f3f3;
}

.payment-label {
  color: #5f5e5e;
  font-size: 24rpx;
}

.payment-label--bold {
  color: #1a1c1c;
  font-weight: 700;
}

.payment-value {
  color: #1a1c1c;
  font-size: 24rpx;
  font-weight: 600;
}

.payment-value--discount {
  color: #ff6600;
  font-weight: 700;
}

.payment-value--total {
  font-size: 44rpx;
  font-weight: 900;
}

.payment-method {
  padding: 20rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.payment-method-text {
  flex: 1;
  font-size: 24rpx;
}

.payment-method-label {
  color: #5f5e5e;
}

.payment-method-value {
  color: #1a1c1c;
  font-weight: 700;
}

.remark-card {
  background: #fff3e0;
  border-left: 6rpx solid #ff6600;
  border-radius: 20rpx;
  padding: 28rpx;
}

.remark-label {
  display: block;
  color: #5f5e5e;
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 12rpx;
}

.remark-text {
  color: #1a1c1c;
  font-size: 26rpx;
  line-height: 1.6;
}

.bottom-space {
  height: 48rpx;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: rgba(249, 249, 249, 0.9);
  backdrop-filter: blur(20rpx);
  padding: 24rpx;
  box-shadow: 0 -8rpx 30rpx rgba(0, 0, 0, 0.04);
}

.footer-inner {
  display: flex;
  gap: 16rpx;
  max-width: 1200rpx;
  margin: 0 auto;
}

.footer-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border: none;
  margin: 0;
  padding: 0;
}

.footer-btn::after {
  border: none;
}

.footer-btn--secondary {
  background: #e2dfde;
  color: #5f5e5e;
}

.footer-btn--primary {
  flex: 2;
  background: linear-gradient(180deg, #ff7a1a 0%, #ff6600 100%);
  color: #561d00;
  box-shadow: 0 10rpx 24rpx rgba(255, 102, 0, 0.2);
}
</style>
