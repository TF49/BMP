<template>
  <PresidentLayout :showTabBar="true" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#ff6600" />
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
            <text class="hero-badge-text">真实接口数据</text>
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
            <text class="stat-value">{{ total }}</text>
          </view>
          <view class="stat-card accent">
            <text class="stat-label">可立即预订</text>
            <text class="stat-value">{{ availableCount }}</text>
          </view>
        </view>

        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载场地列表中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="loadList">重试</view>
        </view>

        <view v-else-if="courts.length === 0" class="state-wrap">
          <text>暂无符合条件的场地</text>
        </view>

        <view v-else class="court-grid">
          <view
            v-for="court in courts"
            :key="court.id"
            class="court-card"
            @click="openDetail(court)"
          >
            <view class="card-top">
              <view>
                <text class="court-id">ID: {{ court.id }}</text>
                <text class="court-name">{{ court.name }}</text>
                <text class="court-location">{{ court.venue }}</text>
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
import { onShow } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeNavigateBack } from '@/utils/navigation'
import { getCourtList, getCourtVenueOptions, deleteCourt as deleteCourtApi, type CourtItem } from '@/api/court'
import { parsePagedList } from '@/utils/parsePagedList'
import { getCourtStatusMeta } from '@/utils/presidentStatus'
import { formatAmount } from '@/utils/format'

type CourtCard = {
  id: number
  name: string
  venue: string
  statusName: string
  statusClass: string
  metricLabel: string
  metricValue: string
}

type VenueOption = {
  id: number
  venueName: string
}

const loading = ref(false)
const errorText = ref('')
const courts = ref<CourtCard[]>([])
const total = ref(0)

const venueOptions = ref<VenueOption[]>([])
const venueIndex = ref(0)
const statusIndex = ref(0)

const venues = computed(() => ['全部场馆', ...venueOptions.value.map(v => v.venueName)])
const statuses = ['全部状态', '可预约', '使用中', '已预约', '维护中']

const filteredCourts = computed(() => courts.value)
const availableCount = computed(() => courts.value.filter(item => item.statusClass === 'available').length)

function mapCourt(item: CourtItem): CourtCard {
  const statusMeta = getCourtStatusMeta(item.status)
  return {
    id: Number(item.id || 0),
    name: item.courtName || item.courtCode || `场地#${item.id}`,
    venue: item.venueName || '未关联场馆',
    statusName: statusMeta.label,
    statusClass: statusMeta.className,
    metricLabel: '价格配置',
    metricValue: `包场¥${formatAmount(item.packagePricePerHour || item.pricePerHour || 0)}/h  拼场¥${formatAmount(item.sharedPricePerHour || item.pricePerHour || 0)}/h  按次¥${formatAmount(item.sharedPricePerTime || item.pricePerTime || 0)}/次`
  }
}

async function loadVenues() {
  try {
    const res = await getCourtVenueOptions()
    venueOptions.value = Array.isArray(res) ? res.filter(v => v.id && v.venueName) : []
  } catch (error) {
    console.error('加载场馆选项失败:', error)
    venueOptions.value = []
  }
}

async function loadList() {
  loading.value = true
  errorText.value = ''
  
  try {
    const selectedVenueId = venueIndex.value > 0 ? venueOptions.value[venueIndex.value - 1]?.id : undefined
    const selectedStatus = statusIndex.value > 0 ? statusIndex.value : undefined
    
    const res = await getCourtList({
      venueId: selectedVenueId,
      status: selectedStatus,
      page: 1,
      size: 100
    })
    
    const parsed = parsePagedList<CourtItem>(res)
    courts.value = parsed.list.map(mapCourt).filter(item => item.id > 0)
    total.value = parsed.total
  } catch (error) {
    console.error('加载场地列表失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
    courts.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onVenueChange(e: any) {
  venueIndex.value = Number(e.detail?.value ?? 0)
  loadList()
}

function onStatusChange(e: any) {
  statusIndex.value = Number(e.detail?.value ?? 0)
  loadList()
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.VENUE_LIST)
}

function openDetail(court: CourtCard) {
  if (!court.id) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURT_DETAIL}?id=${court.id}` })
}

function editCourt(court: CourtCard) {
  if (!court.id) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURT_FORM}?id=${court.id}` })
}

function deleteCourt(court: CourtCard) {
  uni.showModal({
    title: '确认删除',
    content: `确定删除场地"${court.name}"吗？`,
    confirmColor: '#ba1a1a',
    success: async (res) => {
      if (!res.confirm) return
      
      try {
        await deleteCourtApi(court.id)
        uni.showToast({ title: '删除成功', icon: 'success' })
        loadList()
      } catch (error) {
        console.error('删除场地失败:', error)
        uni.showToast({ 
          title: error instanceof Error ? error.message : '删除失败', 
          icon: 'none' 
        })
      }
    }
  })
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.COURT_FORM })
}

onShow(() => {
  loadVenues()
  loadList()
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

.state-wrap {
  min-height: 420rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  color: #6b7280;
  font-size: 26rpx;
}

.spinner {
  width: 44rpx;
  height: 44rpx;
  border: 4rpx solid #e5e7eb;
  border-top-color: #ea580c;
  border-radius: 9999px;
  animation: spin .8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.retry-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  padding: 0 32rpx;
  border-radius: 16rpx;
  background: #ea580c;
  color: #ffffff;
  font-weight: 700;
  font-size: 26rpx;
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
