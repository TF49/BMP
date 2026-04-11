<template>
  <view class="home-page">
    <scroll-view scroll-y class="main-content">
      <view class="top-bar" :style="{ paddingTop: `${statusBarHeight || 44}px` }">
        <PresidentBrandLogo width="220rpx" />
        <view class="message-entry" :style="{ marginRight: `${navBarMarginRight}px` }" @tap="navigateTo('/pages/notice/index')">
          <uni-icons type="chatbubble" size="20" color="#5f5e5e" />
        </view>
      </view>

      <view class="content">
        <view class="hero-card">
          <image class="hero-image" mode="aspectFill" src="/static/placeholders/hero.svg" />
          <view class="hero-mask">
            <text class="hero-tag">会长工作台</text>
            <text class="hero-title">BMP 经营概览</text>
            <text class="hero-sub">{{ summaryTimeLabel }}</text>
          </view>
        </view>

        <view v-if="loading" class="state-card">
          <text class="state-text">正在加载经营摘要...</text>
        </view>

        <view v-else-if="loadError" class="state-card">
          <text class="state-text">{{ loadError }}</text>
          <view class="retry-link" @tap="loadSummary">重新加载</view>
        </view>

        <template v-else>
          <view v-if="hasPrimarySummary" class="summary-card">
            <text class="summary-label">{{ primaryMetric.label }}</text>
            <text class="summary-value">{{ primaryMetric.value }}</text>

            <view v-if="secondaryMetrics.length" class="summary-grid">
              <view v-for="item in secondaryMetrics" :key="item.label" class="summary-item">
                <uni-icons :type="item.icon" size="18" color="#a33e00" />
                <text class="summary-item-value">{{ item.value }}</text>
                <text class="summary-item-label">{{ item.label }}</text>
              </view>
            </view>
          </view>

          <view class="section">
            <view class="section-head">
              <text class="section-title">管理中心</text>
              <text class="section-link" @tap="navigateTo(PRESIDENT_PAGES.USER_LIST)">查看全部</text>
            </view>

            <view class="shortcut-grid">
              <view v-for="item in shortcuts" :key="item.label" class="shortcut-item" @tap="navigateTo(item.path)">
                <uni-icons :type="item.icon" size="24" color="#a33e00" />
                <text class="shortcut-label">{{ item.label }}</text>
              </view>
            </view>
          </view>

          <view v-if="alerts.length" class="section">
            <view class="section-head">
              <text class="section-title">实时摘要</text>
            </view>

            <view class="alert-list">
              <view v-for="item in alerts" :key="item.title" class="alert-item" @tap="navigateTo(item.path)">
                <view class="alert-main">
                  <view class="alert-icon" :class="item.bgClass">
                    <uni-icons :type="item.icon" size="20" :color="item.iconColor" />
                  </view>
                  <view class="alert-copy">
                    <text class="alert-title">{{ item.title }}</text>
                    <text class="alert-desc">{{ item.description }}</text>
                  </view>
                </view>
                <uni-icons type="right" size="16" color="#5f5e5e" />
              </view>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PresidentBrandLogo from '@/components/president/PresidentBrandLogo.vue'
import { getPresidentDashboardSummary, type PresidentDashboardSummary } from '@/api/president/dashboard'
import { isPresidentTabPage, PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { formatAmount, formatDate } from '@/utils/format'
import { getSafeSystemInfo } from '@/utils/systemInfo'

interface MetricItem {
  label: string
  value: string
  icon: string
}

interface AlertItem {
  title: string
  description: string
  icon: string
  iconColor: string
  bgClass: string
  path: string
}

const statusBarHeight = ref(44)
const navBarMarginRight = ref(0)
const loading = ref(false)
const loadError = ref('')
const summary = ref<PresidentDashboardSummary | null>(null)

const shortcuts = [
  { label: '场馆管理', path: PRESIDENT_PAGES.VENUE_LIST, icon: 'location' },
  { label: '场地管理', path: PRESIDENT_PAGES.COURT_LIST, icon: 'calendar' },
  { label: '教练管理', path: PRESIDENT_PAGES.COACH_LIST, icon: 'staff' },
  { label: '课程管理', path: PRESIDENT_PAGES.COURSE_LIST, icon: 'compose' },
  { label: '会员中心', path: PRESIDENT_PAGES.MEMBER_LIST, icon: 'person' },
  { label: '员工管理', path: PRESIDENT_PAGES.USER_LIST, icon: 'person' },
  { label: '器材库存', path: PRESIDENT_PAGES.EQUIPMENT_LIST, icon: 'cart' },
  { label: '赛事中心', path: PRESIDENT_PAGES.TOURNAMENT_LIST, icon: 'medal' }
]

function toNumber(value: unknown): number | null {
  if (value === null || value === undefined || value === '') return null
  const num = Number(value)
  return Number.isFinite(num) ? num : null
}

function pickNumber(source: Record<string, unknown> | null | undefined, keys: string[]): number | null {
  if (!source) return null
  for (const key of keys) {
    const result = toNumber(source[key])
    if (result !== null) return result
  }
  return null
}

const financeSummary = computed(() => summary.value?.finance as Record<string, unknown> | null | undefined)
const memberSummary = computed(() => summary.value?.member as Record<string, unknown> | null | undefined)
const courtSummary = computed(() => summary.value?.court as Record<string, unknown> | null | undefined)
const bookingSummary = computed(() => summary.value?.booking as Record<string, unknown> | null | undefined)

const primaryMetric = computed<MetricItem>(() => {
  const income = pickNumber(financeSummary.value, ['totalIncome', 'income', 'todayIncome', 'totalAmount'])
  if (income !== null) {
    return { label: '总营收', value: `¥${formatAmount(income)}`, icon: 'wallet' }
  }

  const netIncome = pickNumber(financeSummary.value, ['netIncome', 'profit'])
  if (netIncome !== null) {
    return { label: '净收益', value: `¥${formatAmount(netIncome)}`, icon: 'wallet' }
  }

  const totalMembers = pickNumber(memberSummary.value, ['totalMembers', 'total'])
  if (totalMembers !== null) {
    return { label: '会员总数', value: String(totalMembers), icon: 'person' }
  }

  return { label: '经营摘要', value: '暂无数据', icon: 'info' }
})

const secondaryMetrics = computed<MetricItem[]>(() => {
  const metrics: MetricItem[] = []

  const utilization = pickNumber(courtSummary.value, ['utilizationRate', 'usageRate', 'courtUsageRate'])
  if (utilization !== null) {
    metrics.push({ label: '场地利用率', value: `${utilization}%`, icon: 'calendar' })
  }

  const newMembers = pickNumber(memberSummary.value, ['newThisWeek', 'newThisMonth', 'newMembers'])
  if (newMembers !== null) {
    metrics.push({ label: '新增会员', value: String(newMembers), icon: 'person' })
  }

  const pendingBookings = pickNumber(bookingSummary.value, ['pendingCount', 'pendingBookings', 'todayBookings'])
  if (pendingBookings !== null) {
    metrics.push({ label: '待处理预约', value: String(pendingBookings), icon: 'compose' })
  }

  const totalCourts = pickNumber(courtSummary.value, ['totalCourts', 'total'])
  if (totalCourts !== null) {
    metrics.push({ label: '场地总数', value: String(totalCourts), icon: 'location' })
  }

  return metrics.slice(0, 4)
})

const hasPrimarySummary = computed(() => primaryMetric.value.value !== '暂无数据' || secondaryMetrics.value.length > 0)

const alerts = computed<AlertItem[]>(() => {
  const items: AlertItem[] = []

  const pendingBookingCount = pickNumber(bookingSummary.value, ['pendingCount', 'pendingBookings'])
  if (pendingBookingCount !== null) {
    items.push({
      title: `待处理预约 ${pendingBookingCount} 条`,
      description: '进入课程预约列表查看真实待处理记录',
      icon: 'calendar',
      iconColor: '#a33e00',
      bgClass: 'bg-soft-orange',
      path: PRESIDENT_PAGES.COURSE_BOOKING_LIST
    })
  }

  const pendingFinanceCount = pickNumber(financeSummary.value, ['pendingCount', 'pendingAuditCount', 'auditCount'])
  if (pendingFinanceCount !== null) {
    items.push({
      title: `财务相关记录 ${pendingFinanceCount} 条`,
      description: '进入财务列表或审计日志查看详情',
      icon: 'wallet',
      iconColor: '#2563eb',
      bgClass: 'bg-soft-blue',
      path: PRESIDENT_PAGES.FINANCE_AUDIT_LOG
    })
  }

  const totalMembers = pickNumber(memberSummary.value, ['totalMembers', 'total'])
  if (totalMembers !== null) {
    items.push({
      title: `会员总数 ${totalMembers} 人`,
      description: '进入会员中心查看当前会员明细',
      icon: 'person',
      iconColor: '#059669',
      bgClass: 'bg-soft-green',
      path: PRESIDENT_PAGES.MEMBER_LIST
    })
  }

  return items
})

const summaryTimeLabel = computed(() => {
  const today = formatDate(new Date())
  return today ? `${today} 数据摘要` : '实时同步后端摘要数据'
})

async function loadSummary() {
  loading.value = true
  loadError.value = ''
  try {
    summary.value = await getPresidentDashboardSummary()
  } catch (error) {
    console.error('Failed to load president dashboard summary:', error)
    summary.value = null
    loadError.value = '经营摘要加载失败'
  } finally {
    loading.value = false
  }
}

function navigateTo(path: string) {
  if (!path) return
  if (isPresidentTabPage(path)) {
    uni.switchTab({ url: path })
    return
  }
  uni.navigateTo({ url: path })
}

onMounted(() => {
  const systemInfo = getSafeSystemInfo()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  navBarMarginRight.value = 0
  loadSummary()
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: #f9f9f9;
}

.main-content {
  height: 100vh;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx 24rpx;
  background: #f3f3f3;
}

.message-entry {
  color: #5f5e5e;
}

.content {
  padding: 12rpx 24rpx 48rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.hero-card,
.state-card,
.summary-card,
.shortcut-item,
.alert-item {
  border-radius: 24rpx;
  overflow: hidden;
}

.hero-card {
  position: relative;
  height: 320rpx;
}

.hero-image,
.hero-mask {
  position: absolute;
  inset: 0;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-mask {
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.18) 0%, rgba(0, 0, 0, 0.58) 100%);
}

.hero-tag {
  color: #ffb183;
  font-size: 22rpx;
  font-weight: 700;
}

.hero-title {
  margin-top: 10rpx;
  color: #ffffff;
  font-size: 48rpx;
  font-weight: 800;
}

.hero-sub {
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.88);
  font-size: 24rpx;
}

.state-card,
.summary-card,
.shortcut-item,
.alert-item {
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.state-card {
  padding: 36rpx;
  text-align: center;
}

.state-text {
  color: #5f5e5e;
  font-size: 28rpx;
}

.retry-link {
  margin-top: 16rpx;
  color: #a33e00;
  font-size: 24rpx;
}

.summary-card {
  padding: 32rpx;
}

.summary-label,
.summary-item-label,
.alert-desc,
.section-link {
  color: #8a8a8a;
}

.summary-label {
  font-size: 24rpx;
}

.summary-value {
  display: block;
  margin-top: 12rpx;
  color: #1a1c1c;
  font-size: 52rpx;
  font-weight: 800;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx;
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f1f1f1;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.summary-item-value {
  color: #1a1c1c;
  font-size: 34rpx;
  font-weight: 700;
}

.summary-item-label {
  font-size: 22rpx;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.section-head,
.alert-item,
.alert-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  color: #1a1c1c;
  font-size: 34rpx;
  font-weight: 800;
}

.section-link {
  font-size: 24rpx;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.shortcut-item {
  aspect-ratio: 1;
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  text-align: center;
}

.shortcut-label {
  color: #1a1c1c;
  font-size: 24rpx;
  font-weight: 700;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.alert-item {
  padding: 24rpx;
}

.alert-main {
  gap: 18rpx;
  justify-content: flex-start;
}

.alert-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.alert-copy {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.alert-title {
  color: #1a1c1c;
  font-size: 28rpx;
  font-weight: 700;
}

.alert-desc {
  font-size: 22rpx;
}

.bg-soft-orange {
  background: #fff3eb;
}

.bg-soft-blue {
  background: #eff6ff;
}

.bg-soft-green {
  background: #ecfdf5;
}
</style>
