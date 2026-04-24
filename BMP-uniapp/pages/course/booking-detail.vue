<template>
  <MobileLayout>
    <view class="page">
      <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
        <view class="header-inner">
          <view class="icon-btn" @tap="handleBack">
            <uni-icons type="left" size="20" color="#1a1c1c" />
          </view>
          <text class="header-title">课程预约详情</text>
          <view class="header-tag">ORDER</view>
        </view>
      </view>

      <scroll-view scroll-y class="main" :style="{ paddingTop: `${statusBarHeight + 64}px` }">
        <view class="content">
          <view v-if="loading" class="state-card">
            <view class="spinner" />
            <text class="state-text">正在加载预约信息…</text>
          </view>

          <view v-else-if="errorText" class="state-card">
            <text class="state-text">{{ errorText }}</text>
            <view class="state-action" @tap="loadDetail">重新加载</view>
          </view>

          <template v-else-if="detail">
            <view class="hero-card">
              <text class="hero-kicker">COURSE BOOKING</text>
              <text class="hero-title">{{ detail.courseName || '课程预约' }}</text>
              <text class="hero-sub">{{ detail.bookingNo || `预约 #${detail.id}` }}</text>
              <view class="hero-grid">
                <view class="hero-cell">
                  <text class="cell-label">预约状态</text>
                  <text class="cell-value">{{ statusText }}</text>
                </view>
                <view class="hero-cell">
                  <text class="cell-label">支付状态</text>
                  <text class="cell-value">{{ paymentStatusText }}</text>
                </view>
              </view>
            </view>

            <view class="card">
              <text class="section-title">课程信息</text>
              <view class="row">
                <text class="label">教练</text>
                <text class="value">{{ detail.coachName || '待分配' }}</text>
              </view>
              <view class="row">
                <text class="label">上课日期</text>
                <text class="value">{{ detail.courseDate || '待定' }}</text>
              </view>
              <view class="row">
                <text class="label">时间段</text>
                <text class="value">{{ timeRangeText }}</text>
              </view>
              <view class="row">
                <text class="label">课程详情</text>
                <view class="inline-link" @tap.stop="openCourseDetail">
                  <text class="value link">查看课程页</text>
                  <uni-icons type="right" size="16" color="#8e7164" />
                </view>
              </view>
            </view>

            <view class="card">
              <text class="section-title">订单信息</text>
              <view class="row">
                <text class="label">预约人</text>
                <text class="value">{{ detail.memberName || `会员 #${detail.memberId || '-'}` }}</text>
              </view>
              <view class="row">
                <text class="label">订单金额</text>
                <text class="value amount">¥{{ amountText }}</text>
              </view>
              <view class="row">
                <text class="label">支付方式</text>
                <text class="value">{{ paymentMethodText }}</text>
              </view>
              <view class="row">
                <text class="label">创建时间</text>
                <text class="value">{{ formatDateTime(detail.createTime) || '未知时间' }}</text>
              </view>
              <view class="row">
                <text class="label">更新时间</text>
                <text class="value">{{ formatDateTime(detail.updateTime) || '未知时间' }}</text>
              </view>
              <view class="row multiline">
                <text class="label">备注</text>
                <text class="value remark">{{ detail.remark || '无' }}</text>
              </view>
            </view>
          </template>
        </view>
      </scroll-view>

      <view v-if="detail && showPayButton" class="footer">
        <view class="footer-left">
          <text class="footer-label">待支付金额</text>
          <text class="footer-value">¥{{ amountText }}</text>
        </view>
        <button class="footer-btn" :class="{ disabled: submitting }" @tap="handlePay">
          {{ submitting ? '处理中...' : '余额支付' }}
        </button>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCourseBookingDetail, processCourseBookingPayment, type CourseBookingItem } from '@/api/course'
import { safeNavigateBack } from '@/utils/navigation'
import { useUserStore } from '@/store/modules/user'
import { PAYMENT_METHOD_TEXT, BOOKING_STATUS, BOOKING_STATUS_TEXT } from '@/utils/constant'
import { formatAmount, formatDateTime } from '@/utils/format'

const userStore = useUserStore()

const statusBarHeight = ref(20)
const bookingId = ref(0)
const detail = ref<CourseBookingItem | null>(null)
const loading = ref(true)
const submitting = ref(false)
const errorText = ref('')

const bookingStatusText = BOOKING_STATUS_TEXT as Record<number, string>
const amountText = computed(() => formatAmount(detail.value?.orderAmount || 0))
const timeRangeText = computed(() => {
  if (!detail.value?.courseStartTime || !detail.value?.courseEndTime) return '待定'
  return `${String(detail.value.courseStartTime).slice(0, 5)} - ${String(detail.value.courseEndTime).slice(0, 5)}`
})
const statusText = computed(() => bookingStatusText[Number(detail.value?.status ?? BOOKING_STATUS.PENDING_PAYMENT)] || '未知状态')
const paymentStatusText = computed(() => {
  const status = Number(detail.value?.paymentStatus ?? 0)
  if (status === 1) return '已支付'
  if (status === 2) return '已退款'
  return '待支付'
})
const paymentMethodText = computed(() => {
  const method = detail.value?.paymentMethod as keyof typeof PAYMENT_METHOD_TEXT | undefined
  return method ? PAYMENT_METHOD_TEXT[method] || method : '余额'
})
const showPayButton = computed(() => {
  if (!detail.value) return false
  return Number(detail.value.status ?? -1) === BOOKING_STATUS.PENDING_PAYMENT && Number(detail.value.paymentStatus ?? 0) !== 1
})

async function loadDetail() {
  if (!bookingId.value) {
    errorText.value = '缺少有效的课程预约 ID'
    loading.value = false
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    detail.value = await getCourseBookingDetail(bookingId.value)
  } catch (error) {
    console.error('加载课程预约详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载课程预约详情失败'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/profile/orders')
}

function openCourseDetail() {
  if (!detail.value?.courseId) return
  uni.navigateTo({ url: `/pages/course/detail?id=${detail.value.courseId}` })
}

async function handlePay() {
  if (!detail.value || submitting.value || !showPayButton.value) return

  uni.showModal({
    title: '确认余额支付',
    content: `将使用本人余额支付课程预约费用。\n支付金额：¥${amountText.value}`,
    success: async (res) => {
      if (!res.confirm || !detail.value) return
      try {
        submitting.value = true
        uni.showLoading({ title: '余额支付中...' })
        await processCourseBookingPayment(detail.value.id, 'BALANCE')
        await loadDetail()
        uni.hideLoading()
        uni.showToast({ title: '余额支付成功', icon: 'success' })
      } catch (error) {
        console.error('课程预约支付失败:', error)
        uni.hideLoading()
        uni.showToast({
          title: error instanceof Error ? error.message : '支付失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        submitting.value = false
      }
    }
  })
}

onLoad((options?: Record<string, string | undefined>) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  bookingId.value = Number(options?.id || options?.bookingId || 0)
  void loadDetail()
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
.cell-label {
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

.section-title {
  font-size: 30rpx;
  font-weight: 900;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 18rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid #f1f1f1;
}

.label {
  color: #6b7280;
  font-size: 24rpx;
}

.value {
  font-size: 26rpx;
  font-weight: 700;
  text-align: right;
}

.value.amount,
.value.link {
  color: #a33e00;
}

.inline-link {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
}

.multiline {
  align-items: flex-start;
}

.remark {
  white-space: pre-wrap;
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
  min-width: 260rpx;
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
