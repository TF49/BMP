<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="header-left">
          <view class="icon-round" @tap="handleBack">
            <uni-icons type="left" size="22" color="#ff6600" />
          </view>
          <text class="screen-title">穿线服务</text>
        </view>
        <view class="cta-btn" @tap="openCreate">+ 新增穿线</view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
      @scrolltolower="loadMore"
    >
      <view class="content">
        <view class="search-card">
          <view class="search-field">
            <uni-icons class="search-ico" type="search" size="20" color="#5f5e5e" />
            <input
              v-model="keyword"
              class="search-input"
              type="text"
              placeholder="搜索球拍、线材或服务单号..."
              @input="onSearchInput"
            />
            <view v-if="keyword" class="clear-btn" @tap="clearSearch">
              <uni-icons type="clear" size="18" color="#5f5e5e" />
            </view>
          </view>
        </view>

        <view class="summary-block">
          <view class="summary-hero">
            <text class="summary-label">我的穿线工单</text>
            <text class="summary-hero-num">{{ summary.total }}</text>
            <view class="trend-row">
              <uni-icons type="calendar" size="16" color="#a33e00" />
              <text class="trend-text">当前账户真实工单数据</text>
            </view>
          </view>
          <view class="summary-grid">
            <view class="mini-stat border-primary">
              <text class="mini-label">待处理</text>
              <text class="mini-num">{{ pad2(summary.pending) }}</text>
              <text class="mini-sub">等待穿线</text>
            </view>
            <view class="mini-stat border-tertiary">
              <text class="mini-label">进行中</text>
              <text class="mini-num">{{ pad2(summary.inProgress) }}</text>
              <text class="mini-sub">正在穿线</text>
            </view>
            <view class="mini-stat solid-orange">
              <text class="mini-label light">已完成</text>
              <text class="mini-num light">{{ pad2(summary.ready) }}</text>
              <text class="mini-sub light dim">可随时查看详情</text>
            </view>
          </view>
        </view>

        <scroll-view class="status-scroll" scroll-x :show-scrollbar="false">
          <view class="status-pills">
            <view
              v-for="tab in tabs"
              :key="tab.key"
              class="status-pill"
              :class="{ active: currentTab === tab.key }"
              @tap="switchTab(tab.key)"
            >
              <text>{{ tab.label }}</text>
            </view>
          </view>
        </scroll-view>

        <view v-if="loading && jobs.length === 0" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在同步穿线工单…</text>
        </view>

        <view v-else-if="jobs.length === 0" class="state-card">
          <text class="state-text">暂无符合条件的穿线工单</text>
          <view class="state-action" @tap="openCreate">去新增穿线</view>
        </view>

        <view v-else class="list-wrap">
          <view v-for="job in jobs" :key="job.id" class="job-card" @tap="openDetail(job)">
            <view class="job-top">
              <view>
                <text class="job-title">{{ job.racketLine }}</text>
                <text class="job-no">{{ job.serviceNo }}</text>
              </view>
              <view class="status-badge" :class="`st-${job.status}`">
                <text>{{ statusLabel(job.status) }}</text>
              </view>
            </view>

            <view class="job-grid">
              <view class="job-field">
                <text class="job-label">线材</text>
                <text class="job-value">{{ job.stringModel }}</text>
              </view>
              <view class="job-field">
                <text class="job-label">磅数</text>
                <text class="job-value">{{ job.tensionLabel }}</text>
              </view>
              <view class="job-field">
                <text class="job-label">提交时间</text>
                <text class="job-value">{{ job.timeLabel }}</text>
              </view>
              <view class="job-field">
                <text class="job-label">订单金额</text>
                <text class="job-value accent">¥{{ job.totalPrice }}</text>
              </view>
              <view class="job-field">
                <text class="job-label">支付状态</text>
                <text class="job-value">{{ paymentStatusLabel(job.paymentStatus) }}</text>
              </view>
            </view>
          </view>

          <view class="load-more">
            <text>{{ hasMore ? (loadingMore ? '加载更多中...' : '上拉加载更多') : '没有更多工单了' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getStringingList, type StringingService } from '@/api/stringing'
import { useCurrentMember } from '@/composables/useCurrentMember'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { STRINGING_STATUS } from '@/utils/constant'

type JobStatus = 'pending' | 'in_progress' | 'ready' | 'cancelled'
type PaymentStatus = 'unpaid' | 'paid' | 'refunded'

type JobVm = {
  id: number
  serviceNo: string
  racketLine: string
  stringModel: string
  tensionLabel: string
  timeLabel: string
  totalPrice: string
  status: JobStatus
  paymentStatus: PaymentStatus
}

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const refreshing = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const keyword = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)
const rawList = ref<StringingService[]>([])
const currentTab = ref<'all' | 'pending' | 'in_progress' | 'ready' | 'cancelled'>('all')
const hasLoadedOnce = ref(false)

const tabs = [
  { key: 'all' as const, label: '全部' },
  { key: 'pending' as const, label: '等待穿线' },
  { key: 'in_progress' as const, label: '正在穿线' },
  { key: 'ready' as const, label: '已完成' },
  { key: 'cancelled' as const, label: '已取消' }
]

function mapStatus(status: number): JobStatus {
  if (status === STRINGING_STATUS.WAITING) return 'pending'
  if (status === STRINGING_STATUS.IN_PROGRESS) return 'in_progress'
  if (status === STRINGING_STATUS.COMPLETED) return 'ready'
  return 'cancelled'
}

function statusLabel(status: JobStatus) {
  if (status === 'pending') return '等待穿线'
  if (status === 'in_progress') return '正在穿线'
  if (status === 'ready') return '已完成'
  return '已取消'
}

function mapPaymentStatus(status: number): PaymentStatus {
  if (status === 1) return 'paid'
  if (status === 2) return 'refunded'
  return 'unpaid'
}

function paymentStatusLabel(status: PaymentStatus) {
  if (status === 'paid') return '已支付'
  if (status === 'refunded') return '已退款'
  return '待支付'
}

function formatDateTime(value: string) {
  if (!value) return '未知时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${m}-${d} ${hh}:${mm}`
}

function formatMoney(value: number | string | undefined) {
  return (Math.round(Number(value || 0) * 100) / 100).toFixed(2)
}

const jobs = computed<JobVm[]>(() => {
  const keywordText = keyword.value.trim().toLowerCase()
  return rawList.value
    .map((item) => ({
      id: item.id,
      serviceNo: item.serviceNo || `工单 #${item.id}`,
      racketLine: [item.racketBrand, item.racketModel].filter(Boolean).join(' ') || '未填写球拍',
      stringModel: item.ownString === 1 || item.isOwnString === 1 ? '自带线材' : item.stringName || item.stringEquipmentName || '未知线材',
      tensionLabel: `${String(item.pound ?? item.tension ?? '-').replace(/\.0$/, '')} lbs`,
      timeLabel: formatDateTime(item.createTime),
      totalPrice: formatMoney(item.totalPrice || item.servicePrice),
      status: mapStatus(Number(item.status)),
      paymentStatus: mapPaymentStatus(Number(item.paymentStatus ?? 0))
    }))
    .filter((item) => (currentTab.value === 'all' ? true : item.status === currentTab.value))
    .filter((item) => {
      if (!keywordText) return true
      return `${item.serviceNo} ${item.racketLine} ${item.stringModel}`.toLowerCase().includes(keywordText)
    })
})

const summary = computed(() => {
  const pending = rawList.value.filter((item) => mapStatus(Number(item.status)) === 'pending').length
  const inProgress = rawList.value.filter((item) => mapStatus(Number(item.status)) === 'in_progress').length
  const ready = rawList.value.filter((item) => mapStatus(Number(item.status)) === 'ready').length
  return {
    total: rawList.value.length,
    pending,
    inProgress,
    ready
  }
})

const hasMore = computed(() => rawList.value.length < total.value)

async function fetchList(reset = false) {
  let memberId = 0
  try {
    const member = await fetchCurrentMember(reset)
    memberId = Number(member?.id || 0)
  } catch (error) {
    console.error('获取当前会员失败:', error)
    rawList.value = reset ? [] : rawList.value
    total.value = reset ? 0 : total.value
    uni.showToast({ title: '当前会员信息获取失败，请稍后重试', icon: 'none' })
    return
  }

  if (!memberId) {
    rawList.value = reset ? [] : rawList.value
    total.value = reset ? 0 : total.value
    uni.showToast({ title: '当前用户未绑定会员，无法查看穿线记录', icon: 'none' })
    return
  }

  if (reset) {
    page.value = 1
    total.value = 0
  }

  const targetLoading = reset ? loading : loadingMore
  targetLoading.value = true
  try {
    const res = await getStringingList({
      memberId,
      page: page.value,
      size: pageSize
    })
    rawList.value = reset ? (res.data || []) : rawList.value.concat(res.data || [])
    total.value = Number(res.total || 0)
  } catch (error) {
    console.error('加载穿线工单失败:', error)
    uni.showToast({ title: '加载工单失败', icon: 'none' })
  } finally {
    targetLoading.value = false
  }
}

function pad2(value: number) {
  return value < 10 ? `0${value}` : `${value}`
}

function switchTab(key: typeof currentTab.value) {
  currentTab.value = key
}

let searchTimer: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentTab.value = currentTab.value
  }, 200)
}

function clearSearch() {
  keyword.value = ''
}

function openCreate() {
  uni.navigateTo({ url: '/pages/stringing/create' })
}

function openDetail(job: JobVm) {
  uni.navigateTo({ url: `/pages/stringing/detail?id=${job.id}` })
}

function handleBack() {
  safeNavigateBack('/pages/profile/index')
}

function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  page.value += 1
  void fetchList(false)
}

function handleRefresh() {
  refreshing.value = true
  fetchList(true).finally(() => {
    refreshing.value = false
  })
}

onLoad(async () => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  hasLoadedOnce.value = true
  await fetchList(true)
})

onShow(() => {
  if (!userStore.isLoggedIn) return
  if (!hasLoadedOnce.value) return
  void fetchList(true)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(16px);
}

.header-inner {
  min-height: 112rpx;
  padding: 12rpx 28rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.icon-round {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.screen-title {
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.cta-btn {
  height: 72rpx;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 800;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 20rpx 18rpx 80rpx;
}

.search-card,
.summary-block,
.job-card,
.state-card {
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05);
}

.search-card {
  padding: 22rpx 24rpx;
}

.search-field {
  position: relative;
  height: 88rpx;
  border-radius: 18rpx;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  padding: 0 18rpx 0 54rpx;
}

.search-ico {
  position: absolute;
  left: 18rpx;
}

.search-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1a1c1c;
}

.clear-btn {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-block {
  margin-top: 20rpx;
  padding: 28rpx 26rpx;
}

.summary-hero {
  padding-bottom: 20rpx;
}

.summary-label {
  display: block;
  font-size: 22rpx;
  color: #6b7280;
  font-weight: 800;
}

.summary-hero-num {
  display: block;
  margin-top: 12rpx;
  font-size: 72rpx;
  line-height: 1;
  font-weight: 900;
  color: #111111;
}

.trend-row {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.trend-text {
  font-size: 22rpx;
  color: #a33e00;
  font-weight: 700;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.mini-stat {
  min-height: 168rpx;
  border-radius: 22rpx;
  padding: 20rpx 18rpx;
  background: #ffffff;
  border-left: 6rpx solid #a33e00;
  box-shadow: 0 6rpx 18rpx rgba(15, 23, 42, 0.04);
}

.mini-stat.border-tertiary { border-left-color: #0062a1; }
.mini-stat.solid-orange {
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  border-left-color: transparent;
}

.mini-label {
  display: block;
  font-size: 20rpx;
  color: #6b7280;
  font-weight: 800;
}

.mini-num {
  display: block;
  margin-top: 18rpx;
  font-size: 56rpx;
  line-height: 1;
  font-weight: 900;
  color: #111111;
}

.mini-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 20rpx;
  color: #5f5e5e;
}

.light { color: #ffffff; }
.dim { opacity: 0.84; }

.status-scroll {
  margin-top: 18rpx;
  white-space: nowrap;
}

.status-pills {
  display: inline-flex;
  gap: 12rpx;
  padding-bottom: 8rpx;
}

.status-pill {
  min-height: 70rpx;
  padding: 0 28rpx;
  border-radius: 9999rpx;
  background: #e8e8e8;
  color: #1a1c1c;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.status-pill.active {
  background: #a33e00;
  color: #ffffff;
}

.list-wrap {
  margin-top: 20rpx;
}

.job-card {
  padding: 26rpx 24rpx;
  margin-bottom: 18rpx;
}

.job-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14rpx;
}

.job-title {
  display: block;
  font-size: 34rpx;
  font-weight: 900;
  color: #111111;
}

.job-no {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #6b7280;
}

.status-badge {
  min-width: 118rpx;
  height: 46rpx;
  padding: 0 14rpx;
  border-radius: 9999rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  font-weight: 800;
}

.st-pending { background: #fff3ec; color: #a33e00; }
.st-in_progress { background: #e0f2fe; color: #0062a1; }
.st-ready { background: #dcfce7; color: #166534; }
.st-cancelled { background: #f1f5f9; color: #64748b; }

.job-grid {
  margin-top: 22rpx;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx 14rpx;
}

.job-label {
  display: block;
  font-size: 18rpx;
  color: #6b7280;
  font-weight: 800;
}

.job-value {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #111111;
  font-weight: 700;
  line-height: 1.5;
}

.job-value.accent {
  color: #a33e00;
}

.load-more {
  padding: 20rpx 0 10rpx;
  text-align: center;
  font-size: 22rpx;
  color: #94a3b8;
}

.state-card {
  margin-top: 22rpx;
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
