<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="icon-btn">
              <uni-icons type="arrow-left" size="22" color="#ff6600" />
            </view>
            <text class="nav-title">场地详情</text>
          </view>
          <view class="nav-actions">
            <view class="icon-btn" @click="loadDetail">
              <uni-icons type="refresh" size="20" color="#71717a" />
            </view>
            <view class="primary-btn" @click="goEdit">
              <uni-icons type="compose" size="16" color="#ffffff" />
              <text class="primary-btn-text">编辑</text>
            </view>
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false">
        <view class="content">
          <view v-if="loading" class="state-card">
            <text class="state-text">正在加载场地详情...</text>
          </view>

          <view v-else-if="loadError" class="state-card error">
            <text class="state-text">{{ loadError }}</text>
            <button class="retry-btn" @click="loadDetail">重新加载</button>
          </view>

          <template v-else-if="detail">
            <view class="hero-card">
              <view>
                <view class="hero-tags">
                  <text class="status-badge" :class="statusMeta.className">{{ statusMeta.label }}</text>
                  <text class="code-badge">{{ detail.courtCode || `#${detail.id}` }}</text>
                </view>
                <text class="hero-title">{{ detail.courtName || '未命名场地' }}</text>
                <text class="hero-subtitle">{{ detail.venueName || '未关联场馆' }}</text>
              </view>
              <view class="hero-price">
                <text class="hero-price-label">{{ billingTypeLabel }}</text>
                <text class="hero-price-value">¥{{ priceLabel }}</text>
              </view>
            </view>

            <view class="card">
              <text class="card-title">基础信息</text>
              <view class="info-grid">
                <view class="info-item">
                  <text class="info-label">场地 ID</text>
                  <text class="info-value">{{ detail.id }}</text>
                </view>
                <view class="info-item">
                  <text class="info-label">状态</text>
                  <text class="info-value">{{ statusMeta.label }}</text>
                </view>
                <view class="info-item">
                  <text class="info-label">计费方式</text>
                  <text class="info-value">{{ billingTypeLabel }}</text>
                </view>
                <view class="info-item">
                  <text class="info-label">单价</text>
                  <text class="info-value">¥{{ priceLabel }}</text>
                </view>
                <view class="info-item">
                  <text class="info-label">创建时间</text>
                  <text class="info-value">{{ formatDateTime(detail.createTime) || '-' }}</text>
                </view>
                <view class="info-item">
                  <text class="info-label">更新时间</text>
                  <text class="info-value">{{ formatDateTime(detail.updateTime) || '-' }}</text>
                </view>
              </view>
            </view>

            <view class="card">
              <view class="card-head">
                <text class="card-title">今日预约概览</text>
                <text class="card-subtitle">真实接口数据</text>
              </view>

              <view class="booking-summary">
                <view class="summary-item">
                  <text class="summary-label">预约单数</text>
                  <text class="summary-value">{{ todayBookings.length }}</text>
                </view>
                <view class="summary-item">
                  <text class="summary-label">最近更新时间</text>
                  <text class="summary-value">{{ formatDateTime(detail.updateTime, 'MM-DD HH:mm') || '-' }}</text>
                </view>
              </view>

              <view v-if="bookingLoading" class="sub-state">正在加载今日预约...</view>
              <view v-else-if="bookingError" class="sub-state">{{ bookingError }}</view>
              <view v-else-if="todayBookings.length === 0" class="sub-state">今日暂无预约记录</view>
              <view v-else class="booking-list">
                <view v-for="(item, index) in todayBookings" :key="`${item.bookingUserName || 'user'}-${index}`" class="booking-item">
                  <view>
                    <text class="booking-name">{{ item.bookingUserName || '未命名预约人' }}</text>
                    <text class="booking-time">{{ item.bookingTime || '时间待确认' }}</text>
                  </view>
                  <uni-icons type="calendar" size="18" color="#ff6600" />
                </view>
              </view>
            </view>
          </template>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getCourtDetail, getTodayCourtBookings, type CourtItem } from '@/api/court'
import { formatAmount, formatDateTime } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getCourtStatusMeta } from '@/utils/presidentStatus'

type TodayBooking = {
  bookingUserName?: string
  bookingTime?: string
}

const courtId = ref<number | null>(null)
const detail = ref<CourtItem | null>(null)
const todayBookings = ref<TodayBooking[]>([])
const loading = ref(false)
const loadError = ref('')
const bookingLoading = ref(false)
const bookingError = ref('')

const statusMeta = computed(() => getCourtStatusMeta(detail.value?.status))
const billingTypeLabel = computed(() =>
  detail.value?.billingType === 'TIME' ? '按次计费' : '按小时计费'
)
const priceLabel = computed(() =>
  formatAmount(Number(detail.value?.pricePerHour || detail.value?.pricePerTime || 0))
)

async function loadBookings(id: number) {
  bookingLoading.value = true
  bookingError.value = ''
  try {
    const res = await getTodayCourtBookings(id)
    todayBookings.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('Failed to load today bookings:', error)
    todayBookings.value = []
    bookingError.value = '今日预约概览加载失败'
  } finally {
    bookingLoading.value = false
  }
}

async function loadDetail() {
  if (!courtId.value) {
    loadError.value = '缺少有效的场地 ID'
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    detail.value = await getCourtDetail(courtId.value)
    await loadBookings(courtId.value)
  } catch (error) {
    console.error('Failed to load court detail:', error)
    detail.value = null
    todayBookings.value = []
    loadError.value = '场地详情加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURT_LIST)
}

function goEdit() {
  if (!detail.value?.id) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURT_FORM}?id=${detail.value.id}` })
}

onLoad((options) => {
  const rawId = Number(options?.id)
  if (!Number.isFinite(rawId) || rawId <= 0) {
    loadError.value = '缺少有效的场地 ID'
    return
  }
  courtId.value = rawId
  loadDetail()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.nav-header {
  position: sticky;
  top: 0;
  z-index: 30;
  padding: 16rpx 24rpx;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(12px);
}

.nav-row,
.nav-left,
.nav-actions,
.hero-tags,
.card-head,
.booking-item {
  display: flex;
  align-items: center;
}

.nav-row,
.card-head,
.booking-item {
  justify-content: space-between;
}

.nav-left,
.nav-actions,
.hero-tags {
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.primary-btn {
  height: 72rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: #ff6600;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.primary-btn-text {
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 600;
}

.main-scroll {
  height: calc(100vh - var(--status-bar-height) - 104rpx);
}

.content {
  padding: 12rpx 24rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.hero-card,
.card,
.state-card {
  padding: 32rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  background: linear-gradient(135deg, #fff3eb 0%, #ffffff 100%);
}

.status-badge,
.code-badge {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}

.status-badge {
  color: #ffffff;
}

.status-badge.available {
  background: #16a34a;
}

.status-badge.inuse {
  background: #ea580c;
}

.status-badge.booked {
  background: #2563eb;
}

.status-badge.maintenance,
.status-badge.unknown {
  background: #6b7280;
}

.code-badge {
  background: rgba(255, 102, 0, 0.12);
  color: #a33e00;
}

.hero-title {
  display: block;
  margin-top: 16rpx;
  font-size: 44rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.hero-subtitle,
.hero-price-label,
.info-label,
.card-subtitle,
.sub-state,
.booking-time,
.state-text {
  color: #71717a;
  font-size: 24rpx;
}

.hero-subtitle {
  display: block;
  margin-top: 10rpx;
}

.hero-price {
  min-width: 180rpx;
  text-align: right;
}

.hero-price-value {
  display: block;
  margin-top: 12rpx;
  font-size: 40rpx;
  font-weight: 800;
  color: #ff6600;
}

.card-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.info-grid,
.booking-summary {
  display: grid;
  gap: 16rpx;
}

.info-grid {
  margin-top: 24rpx;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.info-item,
.summary-item {
  padding: 20rpx 24rpx;
  border-radius: 18rpx;
  background: #f8fafc;
}

.info-label,
.summary-label {
  display: block;
}

.info-value,
.summary-value,
.booking-name {
  display: block;
  margin-top: 10rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  font-weight: 600;
}

.booking-summary {
  margin: 20rpx 0;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.booking-item {
  padding: 24rpx 0;
  border-top: 1rpx solid #f1f5f9;
}

.retry-btn {
  margin-top: 20rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 26rpx;
}

.error .state-text {
  color: #b91c1c;
}
</style>
