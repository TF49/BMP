<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
        <view class="header-inner">
          <view class="header-left" @tap="goBack">
            <view class="menu-icon" aria-hidden="true">
              <text />
              <text />
              <text />
            </view>
            <text class="header-title">场地预约管理</text>
          </view>

          <view class="header-right">
            <view class="header-action" aria-hidden="true">
              <uni-icons type="search" size="20" color="#5f5e5e" />
            </view>
            <view class="header-avatar" @tap="goProfile">
              <image class="header-avatar-image" :src="avatarUrl" mode="aspectFill" />
            </view>
          </view>
        </view>
      </view>

      <scroll-view
        scroll-y
        class="scroll"
        :style="{ height: scrollHeight }"
        :show-scrollbar="false"
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="loadMore"
      >
        <view class="content">
          <view class="hero-copy">
            <text class="hero-eyebrow">概览</text>
            <text class="hero-title">实时预约看板</text>
          </view>

          <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
            <view class="tabs">
              <view
                v-for="tab in tabs"
                :key="tab.key"
                class="tab"
                :class="{ active: statusFilter === tab.key }"
                @tap="changeTab(tab.key)"
              >
                {{ tab.label }}
              </view>
            </view>
          </scroll-view>

          <view v-if="loading && !cards.length" class="state-card">
            <view class="spinner" />
            <text class="state-text">正在加载预约数据...</text>
          </view>

          <view v-else-if="errorText && !cards.length" class="state-card">
            <text class="state-text">{{ errorText }}</text>
            <view class="state-link" @tap="reload">重新加载</view>
          </view>

          <view v-else-if="!cards.length" class="state-card">
            <text class="state-text">当前没有符合条件的场地预约</text>
          </view>

          <view v-else class="card-list">
            <view
              v-for="item in cards"
              :key="item.id"
              class="booking-card"
              :class="{
                'booking-card--completed': item.statusKey === 'completed',
                'booking-card--cancelled': item.statusKey === 'cancelled'
              }"
              @tap="goDetail(item.id)"
            >
              <view class="booking-main">
                <view class="date-box">
                  <text class="date-month">{{ item.monthText }}月</text>
                  <text class="date-day">{{ item.dayText }}日</text>
                </view>

                <view class="booking-copy">
                  <view class="booking-meta">
                    <text class="booking-no">{{ item.bookingNo }}</text>
                    <text class="member-name">{{ item.memberName }}</text>
                  </view>

                  <text class="booking-title">{{ item.title }}</text>

                  <view class="booking-info">
                    <view class="info-item">
                      <uni-icons type="clock" size="14" color="#5f5e5e" />
                      <text>{{ item.timeText }}</text>
                    </view>
                    <view class="info-item">
                      <uni-icons type="wallet" size="14" color="#5f5e5e" />
                      <text class="amount-text">¥{{ item.amountText }}</text>
                    </view>
                  </view>
                </view>
              </view>

              <view class="booking-side">
                <text class="status-pill" :class="`status-pill--${item.statusKey}`">{{ item.statusText }}</text>
                <uni-icons type="right" size="18" color="#2f3131" />
              </view>
            </view>

            <view v-if="loading && cards.length" class="loading-more">
              <view class="spinner spinner--small" />
              <text>加载更多中...</text>
            </view>

            <view v-else-if="hasMore" class="load-more" @tap="loadMore">加载更多</view>
          </view>

          <view class="insight-grid">
            <view class="insight-card insight-card--dark">
              <view class="insight-copy">
                <text class="insight-label insight-label--accent">经营洞察</text>
                <text class="insight-title">
                当前场地利用率已达
                  <text class="insight-emphasis">{{ utilizationPercent }}%</text>
                </text>
                <view class="insight-button" @tap="goCourtManagement">查看场地</view>
              </view>
              <view class="court-graphic" aria-hidden="true">
                <view class="court-graphic__line court-graphic__line--v1" />
                <view class="court-graphic__line court-graphic__line--v2" />
                <view class="court-graphic__line court-graphic__line--h1" />
                <view class="court-graphic__line court-graphic__line--h2" />
              </view>
            </view>

            <view class="insight-card insight-card--light">
              <text class="insight-label">今日预约收入</text>
              <text class="revenue-value">¥{{ todayRevenueText }}</text>

              <view class="progress-wrap">
                <view class="progress-head">
                  <text class="progress-label">支付完成进度</text>
                  <text class="progress-value">{{ paymentProgress }}%</text>
                </view>
                <view class="progress-track">
                  <view class="progress-bar" :style="{ width: `${paymentProgress}%` }" />
                </view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import {
  getBookingList,
  getBookingStatistics,
  type BookingItem,
  type BookingStatistics
} from '@/api/president/booking'
import {
  getPresidentDashboardSummary,
  type PresidentDashboardSummary
} from '@/api/president/dashboard'
import { parsePagedList } from '@/utils/parsePagedList'
import { formatAmount, formatDate } from '@/utils/format'
import { getAvatarImage } from '@/utils/displayImage'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { useUserStore } from '@/store/modules/user'

type StatusKey = 'all' | 'pending' | 'paid' | 'ongoing' | 'completed' | 'cancelled'

type BookingCard = {
  id: number
  bookingNo: string
  memberName: string
  title: string
  monthText: string
  dayText: string
  timeText: string
  amountText: string
  statusText: string
  statusKey: Exclude<StatusKey, 'all'>
}

const PAGE_SIZE = 10
const userStore = useUserStore()
const statusBarHeight = ref(getSafeSystemInfo().statusBarHeight || 20)
const statusFilter = ref<StatusKey>('all')
const cards = ref<BookingCard[]>([])
const refreshing = ref(false)
const loading = ref(false)
const errorText = ref('')
const page = ref(1)
const total = ref(0)
const stats = ref<BookingStatistics | null>(null)
const summary = ref<PresidentDashboardSummary | null>(null)

const tabs: Array<{ key: StatusKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待支付' },
  { key: 'paid', label: '已支付' },
  { key: 'ongoing', label: '进行中' },
  { key: 'completed', label: '已完成' },
  { key: 'cancelled', label: '已取消' }
]

const avatarUrl = computed(() => getAvatarImage(userStore.userInfo?.avatar))
const hasMore = computed(() => cards.value.length < total.value)
const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value}px - 96rpx)`)

const todayRevenueText = computed(() => {
  const finance = summary.value?.finance as Record<string, unknown> | null | undefined
  const amount = Number(finance?.totalIncome ?? 0)
  return formatAmount(Number.isFinite(amount) ? amount : 0)
})

const utilizationPercent = computed(() => {
  const court = summary.value?.court as Record<string, unknown> | null | undefined
  const totalCourts = Number(court?.total ?? 0)
  const reserved = Number(court?.reserved ?? 0)
  const inUse = Number(court?.inUse ?? 0)
  if (!totalCourts || totalCourts <= 0) return 0
  return Math.min(100, Math.round(((reserved + inUse) / totalCourts) * 100))
})

const paymentProgress = computed(() => {
  const pending = Number(stats.value?.pending ?? 0)
  const paid = Number(stats.value?.paid ?? 0)
  const ongoing = Number(stats.value?.ongoing ?? 0)
  const finished = Number(stats.value?.finished ?? 0)
  const totalActive = pending + paid + ongoing + finished
  if (totalActive <= 0) return 0
  return Math.min(100, Math.round(((paid + ongoing + finished) / totalActive) * 100))
})

function getStatusMeta(status?: number | null) {
  switch (status) {
    case 1:
      return { text: '待支付', key: 'pending' as const }
    case 2:
      return { text: '已支付', key: 'paid' as const }
    case 3:
      return { text: '进行中', key: 'ongoing' as const }
    case 4:
      return { text: '已完成', key: 'completed' as const }
    case 0:
      return { text: '已取消', key: 'cancelled' as const }
    default:
      return { text: '未知', key: 'cancelled' as const }
  }
}

function getStatusParam(key: StatusKey) {
  switch (key) {
    case 'pending':
      return 1
    case 'paid':
      return 2
    case 'ongoing':
      return 3
    case 'completed':
      return 4
    case 'cancelled':
      return 0
    default:
      return undefined
  }
}

function mapCard(item: BookingItem): BookingCard {
  const bookingDate = item.bookingDate ? new Date(item.bookingDate) : null
  const monthText = bookingDate && !Number.isNaN(bookingDate.getTime())
    ? String(bookingDate.getMonth() + 1)
    : '--'
  const dayText = bookingDate && !Number.isNaN(bookingDate.getTime())
    ? String(bookingDate.getDate())
    : '--'
  const statusMeta = getStatusMeta(item.status)

  return {
    id: Number(item.id || 0),
    bookingNo: String(item.bookingNo || `BK-${item.id || '--'}`),
    memberName: String(item.memberName || '未命名会员'),
    title: `${String(item.venueName || '未知场馆')} - ${String(item.courtName || '未知场地')}`,
    monthText,
    dayText,
    timeText: `${String(item.startTime || '--:--').slice(0, 5)} - ${String(item.endTime || '--:--').slice(0, 5)}`,
    amountText: formatAmount(Number(item.orderAmount ?? item.amount ?? 0)),
    statusText: statusMeta.text,
    statusKey: statusMeta.key
  }
}

async function fetchSummary() {
  try {
    const today = formatDate(new Date())
    const [statsRes, summaryRes] = await Promise.all([
      getBookingStatistics(),
      getPresidentDashboardSummary({
        startDate: today,
        endDate: today
      })
    ])
    stats.value = statsRes
    summary.value = summaryRes
  } catch (error) {
    console.error('Failed to load booking summary:', error)
  }
}

async function fetchList(reset = false) {
  if (loading.value) return
  loading.value = true
  errorText.value = ''

  if (reset) {
    page.value = 1
  }

  try {
    const res = await getBookingList({
      status: getStatusParam(statusFilter.value),
      page: page.value,
      size: PAGE_SIZE
    })
    const parsed = parsePagedList<BookingItem>(res)
    const nextCards = parsed.list.map(mapCard).filter((item) => item.id > 0)
    total.value = parsed.total
    cards.value = reset ? nextCards : cards.value.concat(nextCards)
  } catch (error) {
    console.error('Failed to load president booking list:', error)
    if (!cards.value.length) {
      errorText.value = error instanceof Error ? error.message : '预约列表加载失败'
    }
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function reload() {
  await Promise.all([fetchSummary(), fetchList(true)])
}

function onRefresh() {
  refreshing.value = true
  void reload()
}

function loadMore() {
  if (loading.value || !hasMore.value) return
  page.value += 1
  void fetchList()
}

function changeTab(key: StatusKey) {
  if (statusFilter.value === key) return
  statusFilter.value = key
  void fetchList(true)
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function goProfile() {
  uni.navigateTo({ url: PRESIDENT_PAGES.PROFILE })
}

function goDetail(id: number) {
  if (!id) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.BOOKING_DETAIL}?id=${id}` })
}

function goCourtManagement() {
  uni.navigateTo({ url: PRESIDENT_PAGES.COURT_LIST })
}

onLoad(() => {
  statusBarHeight.value = getSafeSystemInfo().statusBarHeight || 20
})

onShow(() => {
  void reload()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
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

.header-left,
.header-right {
  display: flex;
  align-items: center;
}

.header-left {
  gap: 18rpx;
}

.header-right {
  gap: 20rpx;
}

.menu-icon {
  width: 36rpx;
  height: 28rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.menu-icon text {
  width: 100%;
  height: 4rpx;
  border-radius: 999rpx;
  background: #ff6600;
  display: block;
}

.header-title {
  color: #ff6600;
  font-size: 34rpx;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.header-action {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
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

.content {
  padding: 32rpx 24rpx 56rpx;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-bottom: 28rpx;
}

.hero-eyebrow {
  color: #a33e00;
  font-size: 18rpx;
  font-weight: 700;
  letter-spacing: 0.2em;
}

.hero-title {
  font-size: 56rpx;
  font-weight: 800;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.tabs-scroll {
  white-space: nowrap;
  margin-bottom: 34rpx;
}

.tabs {
  display: inline-flex;
  gap: 12rpx;
}

.tab {
  min-width: 96rpx;
  padding: 18rpx 24rpx;
  border-radius: 18rpx;
  background: #f0efef;
  color: #5f5e5e;
  font-size: 24rpx;
  font-weight: 500;
  text-align: center;
  white-space: pre-wrap;
  line-height: 1.25;
}

.tab.active {
  background: linear-gradient(180deg, #ff7a1a 0%, #ff6600 100%);
  color: #ffffff;
  font-weight: 700;
  box-shadow: 0 10rpx 24rpx rgba(255, 102, 0, 0.18);
}

.state-card {
  min-height: 320rpx;
  border-radius: 28rpx;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 18rpx 44rpx rgba(15, 23, 42, 0.06);
}

.spinner {
  width: 44rpx;
  height: 44rpx;
  border-radius: 999rpx;
  border: 4rpx solid rgba(255, 102, 0, 0.16);
  border-top-color: #ff6600;
  animation: spin 0.8s linear infinite;
}

.spinner--small {
  width: 32rpx;
  height: 32rpx;
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

.state-link,
.load-more {
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

.card-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.booking-card {
  border-radius: 28rpx;
  background: #ffffff;
  padding: 28rpx 28rpx 26rpx;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
  box-shadow: 0 18rpx 42rpx rgba(15, 23, 42, 0.05);
}

.booking-card--completed {
  opacity: 0.84;
}

.booking-card--cancelled {
  opacity: 0.72;
}

.booking-main {
  flex: 1;
  min-width: 0;
  display: flex;
  gap: 20rpx;
}

.date-box {
  width: 136rpx;
  min-width: 136rpx;
  border-radius: 20rpx;
  background: #f3f3f3;
  padding: 20rpx 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.date-month {
  color: #5f5e5e;
  font-size: 18rpx;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.date-day {
  margin-top: 12rpx;
  color: #1a1c1c;
  font-size: 48rpx;
  font-weight: 900;
  line-height: 1;
}

.booking-copy {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding-top: 6rpx;
}

.booking-meta {
  display: flex;
  align-items: center;
  gap: 14rpx;
  flex-wrap: wrap;
}

.booking-no {
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(163, 62, 0, 0.08);
  color: #a33e00;
  font-size: 18rpx;
  font-weight: 800;
}

.member-name {
  color: #1a1c1c;
  font-size: 28rpx;
  font-weight: 700;
}

.booking-title {
  color: #1a1c1c;
  font-size: 38rpx;
  font-weight: 800;
  line-height: 1.24;
}

.booking-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
  flex-wrap: wrap;
  color: #5f5e5e;
  font-size: 24rpx;
}

.info-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
}

.amount-text {
  color: #1a1c1c;
  font-weight: 800;
}

.booking-side {
  min-width: 92rpx;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: 36rpx;
  padding-top: 4rpx;
}

.status-pill {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 18rpx;
  font-weight: 800;
  line-height: 1.2;
}

.status-pill--pending {
  background: #fff1e4;
  color: #a33e00;
}

.status-pill--paid {
  background: #edf2ff;
  color: #3b82f6;
}

.status-pill--ongoing {
  background: #e7f5ed;
  color: #008a44;
}

.status-pill--completed {
  background: #eeeeee;
  color: #6b7280;
}

.status-pill--cancelled {
  background: #ececec;
  color: #4b5563;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  color: #5f5e5e;
  font-size: 24rpx;
  padding: 12rpx 0 4rpx;
}

.insight-grid {
  margin-top: 40rpx;
  display: grid;
  grid-template-columns: 1fr;
  gap: 28rpx;
}

.insight-card {
  border-radius: 28rpx;
  overflow: hidden;
}

.insight-card--dark {
  position: relative;
  min-height: 314rpx;
  padding: 34rpx 34rpx 32rpx;
  background: linear-gradient(180deg, #353535 0%, #2a2a2a 100%);
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20rpx;
}

.insight-copy {
  position: relative;
  z-index: 1;
  max-width: 380rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.insight-label {
  color: #5f5e5e;
  font-size: 20rpx;
  font-weight: 700;
}

.insight-label--accent {
  color: #ff7a1a;
}

.insight-title {
  color: #ffffff;
  font-size: 54rpx;
  font-weight: 800;
  line-height: 1.18;
}

.insight-emphasis {
  color: #ff6600;
}

.insight-button {
  margin-top: 8rpx;
  width: 168rpx;
  height: 78rpx;
  border-radius: 18rpx;
  background: linear-gradient(180deg, #ff7a1a 0%, #ff6600 100%);
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.court-graphic {
  position: absolute;
  inset: 30rpx 24rpx 24rpx auto;
  width: 280rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.32);
  opacity: 0.42;
}

.court-graphic__line {
  position: absolute;
  background: rgba(255, 255, 255, 0.32);
}

.court-graphic__line--v1,
.court-graphic__line--v2 {
  top: 0;
  bottom: 0;
  width: 2rpx;
}

.court-graphic__line--v1 {
  left: 50%;
}

.court-graphic__line--v2 {
  left: 16%;
}

.court-graphic__line--h1,
.court-graphic__line--h2 {
  left: 0;
  right: 0;
  height: 2rpx;
}

.court-graphic__line--h1 {
  top: 28%;
}

.court-graphic__line--h2 {
  top: 72%;
}

.insight-card--light {
  padding: 36rpx 34rpx 30rpx;
  background: #ffffff;
  border-top: 4rpx solid #a33e00;
  box-shadow: 0 18rpx 42rpx rgba(15, 23, 42, 0.05);
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.revenue-value {
  color: #1a1c1c;
  font-size: 68rpx;
  font-weight: 900;
  line-height: 1;
}

.progress-wrap {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.progress-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.progress-label {
  color: #5f5e5e;
  font-size: 20rpx;
}

.progress-value {
  color: #a33e00;
  font-size: 20rpx;
  font-weight: 800;
}

.progress-track {
  height: 6rpx;
  border-radius: 999rpx;
  background: #f1d0bf;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #a33e00 0%, #ff6600 100%);
}
</style>
