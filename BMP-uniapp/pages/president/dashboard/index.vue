<template>
  <PresidentLayout :showTabBar="true">
    <view class="dashboard-page">
      <!-- 欢迎区 -->
      <view class="welcome-section glass-card">
        <view class="welcome-row">
          <text class="welcome-title">协会会长工作台</text>
          <text class="welcome-date">{{ dateText }}</text>
        </view>
        <text class="welcome-subtitle">今日数据概览，下拉刷新</text>
      </view>

      <!-- KPI 卡片 -->
      <view class="kpi-section">
        <view class="kpi-grid">
          <view class="kpi-card glass-card" @click="goToList('booking')">
            <view class="kpi-icon-wrap primary">
              <uni-icons type="calendar" size="24" color="#fff"></uni-icons>
            </view>
            <text class="kpi-value">{{ numOrZero(stats.todayBookings) }}</text>
            <text class="kpi-label">今日预约</text>
          </view>
          <view class="kpi-card glass-card" @click="goToList('member')">
            <view class="kpi-icon-wrap success">
              <uni-icons type="person" size="24" color="#fff"></uni-icons>
            </view>
            <text class="kpi-value">{{ numOrZero(stats.todayNewMembers) }}</text>
            <text class="kpi-label">今日新增会员</text>
          </view>
          <view class="kpi-card glass-card" @click="goToList('finance')">
            <view class="kpi-icon-wrap warning">
              <uni-icons type="wallet" size="24" color="#fff"></uni-icons>
            </view>
            <text class="kpi-value">¥{{ formatNum(stats.todayRevenue) }}</text>
            <text class="kpi-label">今日收入</text>
          </view>
          <view class="kpi-card glass-card">
            <view class="kpi-icon-wrap info">
              <uni-icons type="person" size="24" color="#fff"></uni-icons>
            </view>
            <text class="kpi-value">{{ numOrZero(stats.memberTotal) }}</text>
            <text class="kpi-label">会员总数</text>
          </view>
        </view>
      </view>

      <!-- 待办/快捷入口 -->
      <view class="shortcut-section">
        <text class="section-title">快捷入口</text>
        <view class="shortcut-grid">
          <view class="shortcut-item glass-card" @click="goToList('user')">
            <uni-icons type="person" size="28" color="#3cc51f"></uni-icons>
            <text>用户管理</text>
          </view>
          <view class="shortcut-item glass-card" @click="goToList('venue')">
            <uni-icons type="location" size="28" color="#3cc51f"></uni-icons>
            <text>场馆管理</text>
          </view>
          <view class="shortcut-item glass-card" @click="goToList('booking')">
            <uni-icons type="calendar" size="28" color="#3cc51f"></uni-icons>
            <text>预约管理</text>
          </view>
          <view class="shortcut-item glass-card" @click="goToList('finance')">
            <uni-icons type="wallet" size="28" color="#3cc51f"></uni-icons>
            <text>财务管理</text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-wrap">
        <uni-icons type="spinner-cycle" size="32" color="#3cc51f" class="spin"></uni-icons>
        <text>加载中...</text>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import {
  getMemberStatistics,
  getBookingStatistics,
  getFinanceStatistics,
  getCourtStatistics
} from '@/api/president/dashboard'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const loading = ref(true)
const stats = ref({
  todayBookings: 0,
  todayNewMembers: 0,
  todayRevenue: 0,
  memberTotal: 0,
  courtTotal: 0,
  bookingGrowth: 0,
  revenueGrowth: 0
})

const dateText = computed(() => {
  const d = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} 周${week}`
})

function numOrZero(v: unknown): number {
  if (v === null || v === undefined) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

function formatNum(amount: unknown): string {
  const n = numOrZero(amount)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

async function fetchStats() {
  loading.value = true
  const today = new Date().toISOString().split('T')[0]
  try {
    const [memberRes, bookingRes, financeRes, courtRes] = await Promise.all([
      getMemberStatistics().catch(() => ({})),
      getBookingStatistics().catch(() => ({})),
      getFinanceStatistics({ startDate: today, endDate: today }).catch(() => ({})),
      getCourtStatistics().catch(() => ({}))
    ])
    const m = memberRes as any
    const b = bookingRes as any
    const f = financeRes as any
    const c = courtRes as any
    stats.value.todayNewMembers = numOrZero(m?.newToday)
    stats.value.memberTotal = numOrZero(m?.total)
    stats.value.todayBookings = b?.todayBookings != null ? numOrZero(b.todayBookings) : numOrZero(b?.ongoing) + numOrZero(b?.finished)
    stats.value.bookingGrowth = numOrZero(b?.bookingTrend)
    stats.value.todayRevenue = f?.totalIncome != null ? numOrZero(f.totalIncome) : 0
    stats.value.revenueGrowth = numOrZero(f?.incomeChange)
    stats.value.courtTotal = numOrZero(c?.total)
  } catch (e) {
    console.error('Dashboard fetch error', e)
  } finally {
    loading.value = false
  }
}

function goToList(module: string) {
  const map: Record<string, string> = {
    user: PRESIDENT_PAGES.USER_LIST,
    venue: PRESIDENT_PAGES.VENUE_LIST,
    finance: PRESIDENT_PAGES.FINANCE_LIST,
    booking: PRESIDENT_PAGES.BOOKING_LIST,
    member: '/pages/president/member/list'
  }
  const path = map[module]
  if (path) uni.navigateTo({ url: path })
}

onMounted(() => {
  fetchStats()
})

onPullDownRefresh(() => {
  fetchStats().finally(() => uni.stopPullDownRefresh())
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  padding: 24rpx;
  padding-top: 0;
}

.welcome-section {
  padding: 32rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
}
.welcome-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}
.welcome-title {
  font-size: 36rpx;
  font-weight: 600;
  color: var(--color-text, #1E293B);
}
.welcome-date {
  font-size: 24rpx;
  color: var(--color-text-secondary, #475569);
}
.welcome-subtitle {
  font-size: 24rpx;
  color: var(--color-text-secondary, #475569);
}

.kpi-section {
  margin-bottom: 32rpx;
}
.kpi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
}
.kpi-card {
  padding: 28rpx;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.kpi-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}
.kpi-icon-wrap.primary { background: linear-gradient(135deg, #3cc51f, #4ade80); }
.kpi-icon-wrap.success { background: linear-gradient(135deg, #22c55e, #4ade80); }
.kpi-icon-wrap.warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.kpi-icon-wrap.info { background: linear-gradient(135deg, #6366f1, #818cf8); }
.kpi-value {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--color-text, #1E293B);
}
.kpi-label {
  font-size: 24rpx;
  color: var(--color-text-secondary, #475569);
  margin-top: 4rpx;
}

.shortcut-section {
  margin-bottom: 24rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--color-text, #1E293B);
  margin-bottom: 20rpx;
  display: block;
}
.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}
.shortcut-item {
  padding: 24rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}
.shortcut-item text {
  font-size: 22rpx;
  color: var(--color-text, #1E293B);
}

.loading-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 40rpx;
  color: var(--color-text-secondary, #475569);
  font-size: 26rpx;
}
.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
