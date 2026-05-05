<template>
  <MobileLayout :showTabBar="false">
    <view class="booking-detail-page">
      <view class="topbar">
        <view class="topbar-inner">
          <view class="icon-btn" @tap="handleBack">
            <uni-icons type="left" size="18" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">预约详情</text>
            <text class="topbar-sub">BOOKING DETAIL</text>
          </view>
          <view class="icon-btn ghost">
            <uni-icons type="calendar" size="18" color="#a33e00" />
          </view>
        </view>
      </view>

      <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
        <view class="hero-card">
          <view class="hero-glow" />
          <text class="hero-kicker">KINETIC LOGIC</text>
          <text class="hero-title">{{ booking.venueName || '预约信息加载中' }}</text>
          <text class="hero-sub">
            {{ booking.bookingNo || 'BOOKING NO.' }} · {{ courtSummary }}
          </text>

          <view class="hero-grid">
            <view class="hero-cell">
              <text class="cell-k">DATE</text>
              <text class="cell-v">{{ booking.bookingDate || '--' }}</text>
            </view>
            <view class="hero-cell">
              <text class="cell-k">TIME</text>
              <text class="cell-v">{{ bookingTimeRange }}</text>
            </view>
          </view>

          <view class="hero-footer">
            <view class="status-pill" :class="getStatusClass(booking.status)">
              {{ getStatusText(booking.status) }}
            </view>
            <text class="hero-amount">¥{{ formatAmount(booking.orderAmount) }}</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Overview</text>
              <text class="section-title">预约概览</text>
            </view>
          </view>

          <view class="overview-grid">
            <view class="overview-card">
              <text class="overview-label">场馆</text>
              <text class="overview-value">{{ booking.venueName || '--' }}</text>
            </view>
            <view class="overview-card">
              <text class="overview-label">预约模式</text>
              <text class="overview-value">{{ bookingModeText }}</text>
            </view>
            <view class="overview-card">
              <text class="overview-label">订单金额</text>
              <text class="overview-value accent">¥{{ formatAmount(booking.orderAmount) }}</text>
            </view>
            <view class="overview-card">
              <text class="overview-label">状态</text>
              <text class="overview-value">{{ getStatusText(booking.status) }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Schedule</text>
              <text class="section-title">预约信息</text>
            </view>
          </view>

          <view class="detail-list">
            <view class="detail-row">
              <text class="detail-label">预约单号</text>
              <text class="detail-value">{{ booking.bookingNo || '--' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">预约日期</text>
              <text class="detail-value">{{ booking.bookingDate || '--' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">场地信息</text>
              <text class="detail-value">{{ courtNamesText }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">计费方式</text>
              <text class="detail-value">{{ pricingSummary }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">时间段</text>
              <text class="detail-value">{{ bookingTimeRange }}</text>
            </view>
            <view v-if="booking.createTime" class="detail-row">
              <text class="detail-label">下单时间</text>
              <text class="detail-value">{{ booking.createTime }}</text>
            </view>
            <view class="detail-row clickable" @tap="openFeeDetail">
              <text class="detail-label">费用明细</text>
              <view class="detail-action">
                <text class="detail-value">查看当前订单金额说明</text>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>
            </view>
          </view>
        </view>

        <view class="tips-card">
          <text class="tips-title">预约提示</text>
          <view class="tips-list">
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>建议提前 10 分钟到场，方便核验预约信息。</text>
            </view>
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>如需取消，请在平台允许的时间范围内尽早操作。</text>
            </view>
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>如遇异常，可前往帮助与反馈页联系平台客服。</text>
            </view>
          </view>
        </view>

        <view v-if="canCancel" class="action-panel">
          <button class="cancel-btn" @tap="handleCancel">
            <text class="cancel-top">Manage Booking</text>
            <text class="cancel-bottom">取消预约</text>
          </button>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getBookingDetail, cancelBooking } from '@/api/booking'
import { safeNavigateBack } from '@/utils/navigation'

const bookingId = ref(0)
const booking = ref({
  id: 0,
  bookingNo: '',
  venueName: '',
  courtName: '',
  primaryCourtName: '',
  courtNames: [] as string[],
  courtCount: 0,
  bookingMode: '',
  pricingModeSummary: '',
  bookingDate: '',
  startTime: '',
  endTime: '',
  orderAmount: 0,
  status: 0,
  createTime: ''
})
const userStore = useUserStore()

const bookingTimeRange = computed(() => {
  if (!booking.value.startTime || !booking.value.endTime) return '--'
  return `${booking.value.startTime} - ${booking.value.endTime}`
})

const bookingModeText = computed(() => (booking.value.bookingMode === 'PACKAGE' ? '包场' : '拼场'))
const courtNamesText = computed(() => {
  if (booking.value.courtNames.length) return booking.value.courtNames.join('、')
  return booking.value.primaryCourtName || booking.value.courtName || '--'
})
const courtSummary = computed(() => {
  const primary = booking.value.primaryCourtName || booking.value.courtName || 'Court Area'
  const count = Number(booking.value.courtCount || booking.value.courtNames.length || 0)
  return count > 1 ? `${primary} 等 ${count} 块场地` : primary
})
const pricingSummary = computed(() => {
  const raw = booking.value.pricingModeSummary
  const map: Record<string, string> = {
    PACKAGE_HOUR: '包场按小时',
    SHARED_HOUR: '拼场按小时',
    SHARED_TIME: '拼场按次'
  }
  return map[raw] || raw || (booking.value.bookingMode === 'PACKAGE' ? '包场按小时计费' : '拼场计费')
})

const canCancel = computed(() => booking.value.status === 1 || booking.value.status === 2)

onLoad((options?: Record<string, string | undefined>) => {
  if (options?.id) {
    bookingId.value = Number(options.id)
  }
})

const loadBookingDetail = async () => {
  try {
    const result = await getBookingDetail(bookingId.value)
    booking.value = {
      id: result.id,
      bookingNo: result.bookingNo || '',
      venueName: result.venueName || result.courtName || '',
      courtName: result.courtName || '',
      primaryCourtName: result.primaryCourtName || result.courtName || '',
      courtNames: Array.isArray(result.courtNames) ? result.courtNames : [],
      courtCount: Number(result.courtCount || 0),
      bookingMode: result.bookingMode || 'SHARED',
      pricingModeSummary: result.pricingModeSummary || '',
      bookingDate: result.bookingDate || '',
      startTime: result.startTime || '',
      endTime: result.endTime || '',
      orderAmount: Number(result.orderAmount) || 0,
      status: Number(result.status) || 0,
      createTime: result.createTime || ''
    }
  } catch (error) {
    console.error('加载预约详情失败:', error)
    uni.showToast({
      title: '加载预约详情失败',
      icon: 'none'
    })
  }
}

const formatAmount = (amount: number) => {
  const numeric = Number(amount) || 0
  return numeric.toFixed(2)
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '已支付',
    3: '进行中',
    4: '已完成'
  }
  return map[status] ?? '未知'
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'status-cancelled',
    1: 'status-pending',
    2: 'status-paid',
    3: 'status-ongoing',
    4: 'status-completed'
  }
  return map[status] ?? ''
}

const handleBack = () => {
  safeNavigateBack()
}

const openFeeDetail = () => {
  if (!booking.value.id) return
  const summary = {
    venueName: booking.value.venueName,
    courtName: booking.value.courtName,
    date: booking.value.bookingDate,
    slot: bookingTimeRange.value,
    bookingId: booking.value.id,
    totalAmount: Number(booking.value.orderAmount || 0),
    payableAmount: Number(booking.value.orderAmount || 0)
  }
  uni.navigateTo({
    url: `/pages/booking/fee-detail?bookingId=${booking.value.id}&data=${encodeURIComponent(JSON.stringify(summary))}&returnUrl=${encodeURIComponent('/pages/booking/detail?id=' + booking.value.id)}`
  })
}

const handleCancel = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消该预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelBooking(booking.value.id)
          uni.showToast({ title: '取消成功', icon: 'success' })
          await loadBookingDetail()
        } catch (error) {
          console.error('取消预约失败:', error)
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  if (bookingId.value) {
    await loadBookingDetail()
  }
})
</script>

<style lang="scss" scoped>
.booking-detail-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(255, 138, 76, 0.24), transparent 34%),
    linear-gradient(180deg, #fff7ed 0%, #fffaf5 24%, #f6f7fb 100%);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 24rpx 24rpx 0;
  background: linear-gradient(180deg, rgba(255, 247, 237, 0.96) 0%, rgba(255, 247, 237, 0.72) 100%);
  backdrop-filter: blur(18rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 14rpx 34rpx rgba(15, 23, 42, 0.08);
}

.icon-btn.ghost {
  background: rgba(255, 237, 213, 0.95);
}

.topbar-copy {
  flex: 1;
  padding: 0 20rpx;
}

.topbar-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.topbar-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  letter-spacing: 4rpx;
  color: #c2410c;
}

.page-scroll {
  height: calc(100vh - 96rpx);
}

.hero-card,
.section-card,
.tips-card,
.action-panel {
  margin: 24rpx;
}

.hero-card {
  position: relative;
  overflow: hidden;
  padding: 38rpx 34rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #1f2937 0%, #7c2d12 58%, #ea580c 100%);
  box-shadow: 0 28rpx 60rpx rgba(124, 45, 18, 0.24);
}

.hero-glow {
  position: absolute;
  top: -120rpx;
  right: -40rpx;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
}

.hero-kicker,
.hero-title,
.hero-sub,
.cell-k,
.cell-v,
.hero-amount {
  position: relative;
  z-index: 1;
}

.hero-kicker {
  display: inline-flex;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  font-size: 20rpx;
  letter-spacing: 3rpx;
  color: rgba(255, 255, 255, 0.9);
}

.hero-title {
  display: block;
  margin-top: 22rpx;
  font-size: 44rpx;
  font-weight: 700;
  line-height: 1.2;
  color: #ffffff;
}

.hero-sub {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.74);
}

.hero-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 28rpx;
}

.hero-cell {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.12);
}

.cell-k {
  display: block;
  font-size: 18rpx;
  letter-spacing: 3rpx;
  color: rgba(255, 255, 255, 0.68);
}

.cell-v {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #ffffff;
}

.hero-footer {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 28rpx;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.14);
  color: #ffffff;
}

.status-pill.status-pending {
  background: rgba(251, 191, 36, 0.22);
  color: #fde68a;
}

.status-pill.status-paid {
  background: rgba(74, 222, 128, 0.22);
  color: #bbf7d0;
}

.status-pill.status-ongoing {
  background: rgba(96, 165, 250, 0.22);
  color: #bfdbfe;
}

.status-pill.status-completed {
  background: rgba(226, 232, 240, 0.22);
  color: #e2e8f0;
}

.status-pill.status-cancelled {
  background: rgba(248, 113, 113, 0.22);
  color: #fecaca;
}

.hero-amount {
  font-size: 40rpx;
  font-weight: 700;
  color: #ffffff;
}

.section-card,
.tips-card,
.action-panel {
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 22rpx 48rpx rgba(15, 23, 42, 0.08);
}

.section-card,
.tips-card {
  padding: 30rpx;
}

.section-head {
  margin-bottom: 24rpx;
}

.section-head.compact {
  margin-bottom: 20rpx;
}

.section-kicker {
  display: block;
  font-size: 20rpx;
  letter-spacing: 3rpx;
  text-transform: uppercase;
  color: #c2410c;
}

.section-title,
.tips-title {
  display: block;
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2937;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.overview-card {
  padding: 22rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #ffffff 100%);
  border: 1rpx solid rgba(255, 102, 0, 0.08);
}

.overview-label {
  display: block;
  font-size: 22rpx;
  color: #7c2d12;
}

.overview-value {
  display: block;
  margin-top: 12rpx;
  font-size: 28rpx;
  line-height: 1.5;
  font-weight: 600;
  color: #1f2937;
}

.overview-value.accent {
  color: #ea580c;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #ffffff 100%);
  border: 1rpx solid rgba(255, 102, 0, 0.08);
}

.detail-row.clickable {
  cursor: pointer;
}

.detail-label {
  font-size: 24rpx;
  color: #7c2d12;
}

.detail-value {
  flex: 1;
  text-align: right;
  font-size: 25rpx;
  line-height: 1.6;
  color: #334155;
}

.detail-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
  font-size: 25rpx;
  line-height: 1.7;
  color: #475569;
}

.action-panel {
  padding: 18rpx;
  margin-bottom: 36rpx;
}

.cancel-btn {
  width: 100%;
  min-height: 104rpx;
  border: none;
  border-radius: 26rpx;
  background: linear-gradient(135deg, #ff8a4c 0%, #ff6600 55%, #a33e00 100%);
  box-shadow: 0 22rpx 36rpx rgba(255, 102, 0, 0.24);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
}

.cancel-btn::after {
  border: none;
}

.cancel-top,
.cancel-bottom {
  display: block;
  color: #ffffff;
}

.cancel-top {
  font-size: 24rpx;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.cancel-bottom {
  font-size: 30rpx;
  font-weight: 700;
}

@media screen and (max-width: 375px) {
  .hero-title {
    font-size: 40rpx;
  }

  .hero-grid,
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .topbar-copy {
    padding: 0 14rpx;
  }
}
</style>
