<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="icon-btn icon-btn-ghost" @tap="handleBack">
          <uni-icons type="left" size="22" color="#ff6600" />
        </view>
        <text class="header-title">消费记录</text>
        <view class="icon-btn icon-btn-ghost" @tap="openFilter">
          <uni-icons type="bars" size="22" color="#ff6600" />
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      @refresherrefresh="handleRefresh"
      refresher-enabled
      :refresher-triggered="refreshing"
    >
      <view class="content">
        <view class="summary-card">
          <text class="section-kicker">当前账单周期</text>
          <text class="summary-label">总支出 ({{ currentMonthLabel }})</text>
          <view class="summary-money">
            <text class="summary-currency">¥</text>
            <text class="summary-value">{{ expenseParts.int }}</text>
            <text class="summary-decimal">.{{ expenseParts.dec }}</text>
          </view>

          <view class="summary-grid">
            <view v-for="item in summaryItems" :key="item.key" class="summary-item">
              <view class="summary-icon" :class="item.iconClass">
                <uni-icons :type="item.icon" size="22" :color="item.iconColor" />
              </view>
              <text class="summary-item-label">{{ item.label }}</text>
              <text class="summary-item-value">¥{{ item.amount }}</text>
            </view>
          </view>
        </view>

        <view class="section-head">
          <text class="section-title">近期交易</text>
        </view>

        <view v-if="loading && transactionCards.length === 0" class="state-card">
          <view class="spinner" />
          <text class="state-text">加载消费记录中…</text>
        </view>

        <view v-else-if="transactionCards.length === 0" class="state-card">
          <text class="state-text">{{ activeBusinessType ? '当前筛选下暂无记录' : '暂无消费记录' }}</text>
        </view>

        <view v-else class="transactions">
          <view
            v-for="item in transactionCards"
            :key="item.id"
            class="transaction-card"
            :class="{ refunded: item.refunded }"
          >
            <view class="transaction-main">
              <view class="transaction-icon" :class="item.iconBg">
                <uni-icons :type="item.icon" size="24" :color="item.iconColor" />
              </view>

              <view class="transaction-body">
                <view class="transaction-head">
                  <text class="transaction-name" :class="{ through: item.refunded }">{{ item.title }}</text>
                  <text class="badge" :class="item.badgeClass">{{ item.statusLabel }}</text>
                </view>
                <text class="transaction-time">{{ item.timeLine }}</text>
                <view class="transaction-meta">
                  <uni-icons :type="item.paymentIcon" size="14" color="#6b7280" />
                  <text class="transaction-meta-text">{{ item.paymentLabel }}</text>
                </view>
              </view>
            </view>

            <view class="transaction-amounts" :class="{ 'transaction-amounts-refund': item.refunded }">
              <text
                class="amount amount-main"
                :class="{
                  through: item.refunded,
                  income: item.isIncome,
                  muted: item.refunded
                }"
              >
                {{ item.primaryAmount }}
              </text>
              <text v-if="item.secondaryAmount" class="amount amount-secondary income">
                {{ item.secondaryAmount }}
              </text>
            </view>
          </view>
        </view>

        <view v-if="transactionCards.length > 0" class="load-more-wrap">
          <view v-if="hasMore" class="load-more-btn" @tap="loadMore">
            <text>{{ loadingMore ? '加载中…' : '加载更多历史记录' }}</text>
          </view>
          <text v-else class="load-more-end">已展示全部记录</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { getMemberConsumeRecords, type ConsumeRecord } from '@/api/member'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { useCurrentMember } from '@/composables/useCurrentMember'

type BusinessFilter = '' | 'BOOKING' | 'COURSE' | 'EQUIPMENT' | 'REFUND'

type TransactionCard = {
  id: number
  title: string
  timeLine: string
  paymentLabel: string
  paymentIcon: string
  statusLabel: string
  badgeClass: string
  icon: string
  iconBg: string
  iconColor: string
  primaryAmount: string
  secondaryAmount: string
  isIncome: boolean
  refunded: boolean
  businessType: string
}

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)

const records = ref<ConsumeRecord[]>([])
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)
const activeBusinessType = ref<BusinessFilter>('')

const BUSINESS_META: Record<string, { label: string; icon: string; iconBg: string; iconColor: string }> = {
  BOOKING: { label: '场馆', icon: 'location', iconBg: 'icon-peach', iconColor: '#ff6600' },
  COURSE: { label: '课程', icon: 'staff', iconBg: 'icon-peach', iconColor: '#ff6600' },
  EQUIPMENT: { label: '装备', icon: 'shop', iconBg: 'icon-peach', iconColor: '#ff6600' },
  TOURNAMENT: { label: '赛事', icon: 'flag', iconBg: 'icon-blue', iconColor: '#0f766e' },
  STRINGING: { label: '穿线', icon: 'compose', iconBg: 'icon-blue', iconColor: '#0f766e' },
  OTHER: { label: '其他', icon: 'wallet', iconBg: 'icon-gray', iconColor: '#6b7280' }
}

function formatMoney(value: number) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function absMoney(value: number) {
  return formatMoney(Math.abs(Number(value || 0)))
}

function toDate(raw?: string) {
  if (!raw) return new Date()
  return new Date(String(raw).replace(/-/g, '/'))
}

function pad2(value: number) {
  return value < 10 ? `0${value}` : `${value}`
}

function formatDateTime(raw?: string) {
  const date = toDate(raw)
  const year = date.getFullYear()
  const month = pad2(date.getMonth() + 1)
  const day = pad2(date.getDate())
  const hours = pad2(date.getHours())
  const minutes = pad2(date.getMinutes())
  return `${year}年${month}月${day}日 • ${hours}:${minutes}`
}

function resolvePaymentLabel(method?: string) {
  const map: Record<string, string> = {
    BALANCE: '余额支付',
    WECHAT: '微信支付',
    ALIPAY: '支付宝支付',
    CASH: '现金支付',
    BANK: '银行卡支付',
    BANKCARD: '银行卡支付'
  }
  return map[method || ''] || '余额支付'
}

function resolvePaymentIcon(method?: string) {
  if (method === 'WECHAT' || method === 'ALIPAY' || method === 'BANK' || method === 'BANKCARD') {
    return 'wallet'
  }
  return 'wallet-filled'
}

function resolveStatusLabel(record: ConsumeRecord, refunded: boolean) {
  if (refunded) return '已退款'

  const statusMap: Record<number, string> = {
    0: '失败',
    1: '成功',
    2: '处理中',
    3: '已退款'
  }

  return statusMap[record.paymentStatus ?? 1] || '成功'
}

function resolveBadgeClass(statusLabel: string) {
  if (statusLabel === '已退款') return 'badge-refund'
  if (statusLabel === '处理中') return 'badge-pending'
  if (statusLabel === '失败') return 'badge-fail'
  return 'badge-success'
}

function resolveTitle(record: ConsumeRecord) {
  if (record.remark?.trim()) return record.remark.trim()
  if (record.description?.trim()) {
    return record.description
      .replace(/^退款\s*-\s*/u, '')
      .replace(/^退款-\s*/u, '')
      .trim()
  }
  const meta = BUSINESS_META[record.businessType || 'OTHER'] || BUSINESS_META.OTHER
  return `${meta.label}消费`
}

function mapCard(record: ConsumeRecord): TransactionCard {
  const amount = Number(record.amount || 0)
  const refunded = amount < 0 || /退款/u.test(record.description || '') || /退款/u.test(record.remark || '')
  const isIncome = refunded
  const metaKey = refunded && activeBusinessType.value === 'REFUND'
    ? ((record.businessType || 'OTHER') as string)
    : (record.businessType || 'OTHER')
  const meta = BUSINESS_META[metaKey] || BUSINESS_META.OTHER
  const statusLabel = resolveStatusLabel(record, refunded)

  return {
    id: record.id,
    title: resolveTitle(record),
    timeLine: formatDateTime(record.createTime),
    paymentLabel: refunded
      ? resolvePaymentLabel(record.paymentMethod).replace('支付', '退款')
      : resolvePaymentLabel(record.paymentMethod),
    paymentIcon: resolvePaymentIcon(record.paymentMethod),
    statusLabel,
    badgeClass: resolveBadgeClass(statusLabel),
    icon: meta.icon,
    iconBg: meta.iconBg,
    iconColor: refunded ? '#8c8c8c' : meta.iconColor,
    primaryAmount: `${isIncome ? '+' : '-'}¥${absMoney(amount)}`,
    secondaryAmount: refunded ? `+¥${absMoney(amount)}` : '',
    isIncome,
    refunded,
    businessType: record.businessType || 'OTHER'
  }
}

const filteredRecords = computed(() => {
  if (!activeBusinessType.value) return records.value
  if (activeBusinessType.value === 'REFUND') {
    return records.value.filter((item) => Number(item.amount || 0) < 0)
  }
  return records.value.filter((item) => item.businessType === activeBusinessType.value)
})

const transactionCards = computed(() => filteredRecords.value.map(mapCard))

const currentMonthLabel = computed(() => `${new Date().getMonth() + 1}月`)

const monthExpenseTotal = computed(() => {
  const now = new Date()
  return records.value.reduce((sum, item) => {
    const amount = Number(item.amount || 0)
    const date = toDate(item.createTime)
    const sameMonth = date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth()
    if (!sameMonth || amount <= 0) return sum
    return sum + amount
  }, 0)
})

const expenseParts = computed(() => {
  const [int, dec] = formatMoney(monthExpenseTotal.value).split('.')
  return { int, dec: dec || '00' }
})

const summaryItems = computed(() => {
  const keys = ['BOOKING', 'COURSE', 'EQUIPMENT'] as const
  const now = new Date()

  return keys.map((key) => {
    const amount = records.value.reduce((sum, item) => {
      const date = toDate(item.createTime)
      const sameMonth = date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth()
      if (!sameMonth || item.businessType !== key || Number(item.amount || 0) <= 0) return sum
      return sum + Number(item.amount || 0)
    }, 0)

    const meta = BUSINESS_META[key]
    return {
      key,
      label: meta.label,
      amount: formatMoney(amount).replace(/\.00$/, ''),
      icon: meta.icon,
      iconClass: meta.iconBg,
      iconColor: meta.iconColor
    }
  })
})

const hasMore = computed(() => records.value.length < total.value)

async function resolveMemberId() {
  const member = await fetchCurrentMember()
  return Number(member.id || 0)
}

async function fetchRecords(reset = false) {
  const memberId = await resolveMemberId()
  if (!memberId) {
    uni.showToast({ title: '未找到会员信息', icon: 'none' })
    return
  }

  if (reset) {
    currentPage.value = 1
    total.value = 0
  }

  const targetLoading = reset ? loading : loadingMore
  targetLoading.value = true

  try {
    const res = await getMemberConsumeRecords(memberId, {
      page: currentPage.value,
      size: pageSize
    })
    const list = res.data || []
    records.value = reset ? list : [...records.value, ...list]
    total.value = Number(res.total || 0)
  } catch (error) {
    console.error('加载消费记录失败:', error)
    uni.showToast({
      title: '加载消费记录失败',
      icon: 'none'
    })
  } finally {
    targetLoading.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/profile/index')
}

function openFilter() {
  uni.showActionSheet({
    itemList: ['全部记录', '场馆', '课程', '装备', '仅退款'],
    success: (res) => {
      const valueMap: BusinessFilter[] = ['', 'BOOKING', 'COURSE', 'EQUIPMENT', 'REFUND']
      activeBusinessType.value = valueMap[res.tapIndex] || ''
    }
  })
}

function loadMore() {
  if (loadingMore.value || loading.value || !hasMore.value) return
  currentPage.value += 1
  void fetchRecords(false)
}

function handleRefresh() {
  refreshing.value = true
  fetchRecords(true).finally(() => {
    refreshing.value = false
  })
}

onMounted(async () => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  await fetchRecords(true)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fbfbfb 0%, #f4f4f4 100%);
  color: #1a1c1c;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: rgba(249, 249, 249, 0.94);
  backdrop-filter: blur(18px);
}

.header-inner {
  min-height: 112rpx;
  padding: 10rpx 30rpx 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn-ghost {
  border-radius: 9999rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.05);
}

.header-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #ff6600;
  letter-spacing: -1rpx;
}

.main {
  height: 100vh;
}

.content {
  padding: 18rpx 18rpx 54rpx;
}

.summary-card,
.transaction-card,
.state-card {
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12rpx 40rpx rgba(15, 23, 42, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.85);
}

.summary-card {
  padding: 44rpx 30rpx 38rpx;
  border-radius: 28rpx;
}

.section-kicker {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #555555;
  letter-spacing: 1rpx;
}

.summary-label {
  display: block;
  margin-top: 30rpx;
  font-size: 28rpx;
  color: #555555;
}

.summary-money {
  display: flex;
  align-items: baseline;
  margin-top: 14rpx;
  gap: 4rpx;
}

.summary-currency {
  font-size: 46rpx;
  font-weight: 800;
  color: #b53e00;
}

.summary-value {
  font-size: 76rpx;
  line-height: 1;
  font-weight: 900;
  letter-spacing: -3rpx;
}

.summary-decimal {
  font-size: 42rpx;
  font-weight: 800;
}

.summary-grid {
  margin-top: 44rpx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.summary-item {
  padding: 24rpx 14rpx 20rpx;
  border-radius: 20rpx;
  background: #f1f1f1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.summary-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-peach {
  background: rgba(255, 102, 0, 0.12);
}

.icon-blue {
  background: rgba(14, 165, 233, 0.14);
}

.icon-gray {
  background: #eeeeee;
}

.summary-item-label {
  margin-top: 14rpx;
  font-size: 24rpx;
  font-weight: 700;
  color: #555555;
}

.summary-item-value {
  margin-top: 12rpx;
  font-size: 22rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.section-head {
  padding: 56rpx 2rpx 26rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #555555;
}

.transactions {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.transaction-card {
  padding: 28rpx 24rpx 24rpx;
  border-radius: 28rpx;
}

.transaction-card.refunded {
  opacity: 0.82;
}

.transaction-main {
  display: flex;
  align-items: flex-start;
  gap: 22rpx;
}

.transaction-icon {
  width: 86rpx;
  height: 86rpx;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.transaction-body {
  flex: 1;
  min-width: 0;
}

.transaction-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
}

.transaction-name {
  font-size: 34rpx;
  font-weight: 800;
  color: #1f2328;
  line-height: 1.3;
}

.transaction-name.through {
  text-decoration: line-through;
  color: #8c8c8c;
}

.badge {
  padding: 6rpx 14rpx;
  border-radius: 9999rpx;
  font-size: 19rpx;
  font-weight: 700;
}

.badge-success {
  background: #e5e5e5;
  color: #666666;
}

.badge-refund {
  background: #ffe2de;
  color: #d66b62;
}

.badge-pending {
  background: #dcecff;
  color: #255e9c;
}

.badge-fail {
  background: #ffd6d6;
  color: #b42318;
}

.transaction-time {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #555b64;
}

.transaction-meta {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.transaction-meta-text {
  font-size: 23rpx;
  color: #6b7280;
}

.transaction-amounts {
  margin-top: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.transaction-amounts-refund {
  align-items: center;
}

.amount {
  font-size: 28rpx;
  font-weight: 900;
  letter-spacing: -0.6rpx;
}

.amount-main {
  font-size: 30px;
  color: #111111;
}

.amount-secondary {
  color: #c86f2e;
  font-size: 28rpx;
}

.amount.income {
  color: #c86f2e;
}

.amount.muted {
  color: #8c8c8c;
}

.amount.through {
  text-decoration: line-through;
}

.state-card {
  border-radius: 26rpx;
  padding: 80rpx 24rpx;
  text-align: center;
}

.state-text {
  font-size: 28rpx;
  color: #8b8b8b;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  margin: 0 auto 18rpx;
  border-radius: 9999rpx;
  border: 4rpx solid #f0f0f0;
  border-top-color: #ff6600;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.load-more-wrap {
  padding: 56rpx 0 34rpx;
  display: flex;
  justify-content: center;
}

.load-more-btn {
  min-width: 360rpx;
  height: 88rpx;
  padding: 0 40rpx;
  border-radius: 9999rpx;
  background: #e5e5e5;
  color: #111111;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 800;
}

.load-more-end {
  font-size: 24rpx;
  color: #9ca3af;
}
</style>
