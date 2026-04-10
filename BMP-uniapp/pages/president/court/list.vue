<template>
  <PresidentLayout :showTabBar="true" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#1a1c1c" />
            </view>
            <view class="brand-wrap">
              <text class="brand-name">Kinetic Logic</text>
            </view>
          </view>
          <view class="nav-right">
            <view class="icon-btn">
              <uni-icons type="search" size="22" color="#71717a" />
            </view>
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="main-content">
        <view class="hero-section">
          <view>
            <text class="hero-title">场地管理</text>
            <text class="hero-subtitle">COURT OPERATIONS</text>
          </view>
          <view class="hero-badge">
            <view class="hero-dot" />
            <text class="hero-badge-text">统一会长端视图</text>
          </view>
        </view>

        <view class="filter-row">
          <picker class="filter-item" @change="onVenueChange" :value="venueIndex" :range="venues">
            <view class="filter-inner">
              <text class="filter-label">所属场馆</text>
              <text class="filter-value">{{ venues[venueIndex] }}</text>
            </view>
          </picker>
          <picker class="filter-item" @change="onStatusChange" :value="statusIndex" :range="statuses">
            <view class="filter-inner">
              <text class="filter-label">当前状态</text>
              <text class="filter-value">{{ statuses[statusIndex] }}</text>
            </view>
          </picker>
        </view>

        <view class="stats-row">
          <view class="stat-card">
            <text class="stat-label">场地总数</text>
            <text class="stat-value">{{ filteredCourts.length }}</text>
          </view>
          <view class="stat-card accent">
            <text class="stat-label">可立即预订</text>
            <text class="stat-value">{{ availableCount }}</text>
          </view>
        </view>

        <view class="court-grid">
          <view
            v-for="court in filteredCourts"
            :key="court.id"
            class="court-card"
            @click="openDetail(court)"
          >
            <view class="card-top">
              <view>
                <text class="court-id">{{ court.id }}</text>
                <text class="court-name">#{{ court.name }} 场地</text>
                <text class="court-location">{{ court.venue }} · {{ court.zone }}</text>
              </view>
              <view class="status-badge" :class="court.statusClass">
                <text>{{ court.statusName }}</text>
              </view>
            </view>

            <view class="card-bottom">
              <view class="metric-block">
                <text class="metric-label">{{ court.metricLabel }}</text>
                <text class="metric-value">{{ court.metricValue }}</text>
              </view>
              <view class="action-group" @click.stop>
                <view class="action-btn" @click="editCourt(court)">
                  <uni-icons type="compose" size="18" color="#5f5e5e" />
                </view>
                <view class="action-btn delete" @click="deleteCourt(court)">
                  <uni-icons type="trash" size="18" color="#ba1a1a" />
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="bottom-space" />
      </scroll-view>

      <view class="fab-btn" @click="goAdd">
        <uni-icons type="plusempty" size="28" color="#ffffff" />
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeNavigateBack } from '@/utils/navigation'

type CourtRow = {
  id: string
  name: string
  venue: string
  zone: string
  statusName: string
  statusClass: string
  metricLabel: string
  metricValue: string
}

const venues = ['全部场馆', '中心体育馆', '奥体中心', '动力羽毛球馆']
const statuses = ['全部状态', '空闲', '使用中', '已预订', '维护中']

const venueIndex = ref(0)
const statusIndex = ref(0)

const courts = ref<CourtRow[]>([
  { id: 'CRT-001', name: '01', venue: '中心体育馆', zone: 'A区', statusName: '空闲', statusClass: 'available', metricLabel: '计费标准', metricValue: '￥80/小时' },
  { id: 'CRT-002', name: '02', venue: '中心体育馆', zone: 'A区', statusName: '使用中', statusClass: 'inuse', metricLabel: '当前用时', metricValue: '45 分钟' },
  { id: 'CRT-003', name: '03', venue: '中心体育馆', zone: 'B区', statusName: '已预订', statusClass: 'booked', metricLabel: '下一场次', metricValue: '14:00 - 16:00' },
  { id: 'CRT-004', name: '04', venue: '奥体中心', zone: '训练区', statusName: '维护中', statusClass: 'maintenance', metricLabel: '维护说明', metricValue: '地板修整中' },
  { id: 'CRT-005', name: '05', venue: '奥体中心', zone: 'VIP区', statusName: '空闲', statusClass: 'available', metricLabel: '计费标准', metricValue: '￥120/小时' }
])

const filteredCourts = computed(() => {
  return courts.value.filter(court => {
    const venueOk = venueIndex.value === 0 || court.venue === venues[venueIndex.value]
    const statusOk = statusIndex.value === 0 || court.statusName === statuses[statusIndex.value]
    return venueOk && statusOk
  })
})

const availableCount = computed(() => filteredCourts.value.filter(item => item.statusClass === 'available').length)

function onVenueChange(e: any) {
  venueIndex.value = Number(e.detail?.value ?? 0)
}

function onStatusChange(e: any) {
  statusIndex.value = Number(e.detail?.value ?? 0)
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.VENUE_LIST)
}

function openDetail(court: CourtRow) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURT_DETAIL}?id=${encodeURIComponent(court.id)}` })
}

function editCourt(court: CourtRow) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURT_FORM}?id=${encodeURIComponent(court.id)}` })
}

function deleteCourt(court: CourtRow) {
  uni.showModal({
    title: '确认删除',
    content: `确定删除 ${court.id} 吗？`,
    confirmColor: '#ba1a1a',
    success: (res) => {
      if (!res.confirm) return
      courts.value = courts.value.filter(item => item.id !== court.id)
    }
  })
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.COURT_FORM })
}
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
  z-index: 40;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(12px);
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
}

.back-btn,
.icon-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.main-content {
  height: calc(100vh - var(--status-bar-height) - 104rpx);
  padding: 0 24rpx;
}

.hero-section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
  padding: 24rpx 0;
}

.hero-title {
  display: block;
  font-size: 56rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.hero-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  letter-spacing: 4rpx;
  color: #71717a;
}

.hero-badge {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 16rpx 20rpx;
  background: #fff1e6;
  border-radius: 999rpx;
}

.hero-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 999rpx;
  background: #ea580c;
}

.hero-badge-text {
  font-size: 22rpx;
  color: #a33e00;
  font-weight: 600;
}

.filter-row,
.stats-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.filter-item,
.stat-card,
.court-card {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.filter-inner,
.stat-card {
  padding: 24rpx;
}

.filter-label,
.stat-label,
.metric-label,
.court-id,
.court-location {
  display: block;
  color: #71717a;
}

.filter-label,
.stat-label,
.metric-label {
  font-size: 22rpx;
}

.filter-value,
.stat-value,
.metric-value,
.court-name {
  display: block;
  margin-top: 10rpx;
  color: #1a1c1c;
  font-weight: 700;
}

.filter-value,
.metric-value {
  font-size: 28rpx;
}

.stat-value {
  font-size: 42rpx;
}

.stat-card.accent {
  background: linear-gradient(135deg, #fff1e6, #ffedd5);
}

.court-grid {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.court-card {
  padding: 24rpx;
}

.card-top,
.card-bottom {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-bottom {
  margin-top: 28rpx;
  align-items: flex-end;
}

.court-id {
  font-size: 20rpx;
  letter-spacing: 2rpx;
}

.court-name {
  font-size: 42rpx;
}

.court-location {
  margin-top: 6rpx;
  font-size: 24rpx;
}

.status-badge {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.status-badge.available {
  background: #dcfce7;
  color: #166534;
}

.status-badge.inuse {
  background: #ffedd5;
  color: #9a3412;
}

.status-badge.booked {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-badge.maintenance {
  background: #e5e7eb;
  color: #4b5563;
}

.action-group {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn.delete {
  background: #fff1f2;
}

.fab-btn {
  position: fixed;
  right: 28rpx;
  bottom: calc(env(safe-area-inset-bottom) + 116rpx);
  width: 104rpx;
  height: 104rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #c2410c, #ea580c);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 20rpx 36rpx rgba(234, 88, 12, 0.28);
}

.bottom-space {
  height: 160rpx;
}
</style>
