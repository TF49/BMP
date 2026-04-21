<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="icon-btn" @tap="handleBack">
          <uni-icons type="left" size="22" color="#1a1c1c" />
        </view>
        <text class="header-title">充值记录</text>
        <view class="icon-btn" @tap="openFilter">
          <uni-icons type="bars" size="22" color="#5f5e5e" />
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      @scrolltolower="loadMore"
    >
      <view class="pad">
        <!-- 余额概览 -->
        <view class="hero">
          <view class="hero-glow hero-glow-r" />
          <view class="hero-glow hero-glow-l" />
          <view class="hero-inner">
            <text class="hero-cap">当前总余额 (CNY)</text>
            <view class="hero-money">
              <text class="hero-yen">¥</text>
              <text class="hero-int">{{ balanceParts.int }}</text>
              <text class="hero-dec">.{{ balanceParts.dec }}</text>
            </view>
            <view class="hero-foot">
              <view>
                <text class="hero-subcap">本月累计充值</text>
                <text class="hero-subval">+¥{{ monthTotal }}</text>
              </view>
              <view class="hero-cta" @tap="goRecharge">
                <uni-icons type="plusempty" size="14" color="#ffffff" />
                <text>去充值</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="loading && flatRecords.length === 0" class="state">
          <text>加载中…</text>
        </view>

        <template v-else-if="groupedMonths.length === 0">
          <view class="state">
            <text>{{ flatRecords.length ? '当前筛选下暂无记录' : '暂无充值记录' }}</text>
          </view>
        </template>

        <template v-else>
          <view v-for="group in groupedMonths" :key="group.key" class="month-block">
            <text class="month-title">{{ group.label }}</text>
            <view class="list">
              <view
                v-for="row in group.items"
                :key="row.id"
                class="row"
                :class="{ dim: row.status === 2 }"
                @tap="onRowTap(row)"
              >
                <view class="row-left">
                  <view class="ico" :class="row.iconBg">
                    <uni-icons :type="row.icon" size="24" :color="row.iconColor" />
                  </view>
                  <view>
                    <text class="row-title">{{ row.title }}</text>
                    <text class="row-time">{{ row.timeLine }}</text>
                  </view>
                </view>
                <view class="row-right">
                  <text
                    class="row-amt"
                    :class="{ bonus: row.highlightAmount, muted: row.mutedAmount }"
                  >
                    +¥{{ row.amountStr }}
                  </text>
                  <view class="badge" :class="row.badgeClass">
                    <text>{{ row.statusText }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </template>

        <view v-if="!loading && flatRecords.length > 0 && !hasMore" class="end-hint">
          <text>无更多记录</text>
        </view>
        <view class="bottom-space" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import { getRechargeRecords, type RechargeRecord } from '@/api/recharge'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { useCurrentMember } from '@/composables/useCurrentMember'

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 48)

const balance = ref(0)
const flatRecords = ref<RechargeRecord[]>([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 20

/** 全部 | 用户充值 | 奖励活动 */
const filterKind = ref<'all' | 'recharge' | 'bonus'>('all')

type RowVm = {
  id: number
  title: string
  timeLine: string
  amountStr: string
  highlightAmount: boolean
  mutedAmount: boolean
  status: number
  statusText: string
  badgeClass: string
  icon: string
  iconBg: string
  iconColor: string
}

function pad2(n: number) {
  return n < 10 ? `0${n}` : `${n}`
}

function formatMoney(n: number) {
  const v = Number(n) || 0
  return v.toFixed(2)
}

function formatMoneyComma(n: number) {
  const [intPart, dec] = formatMoney(n).split('.')
  const intNum = intPart || '0'
  const withComma = intNum.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return `${withComma}.${dec || '00'}`
}

const balanceParts = computed(() => {
  const s = formatMoney(balance.value)
  const [intPart, dec] = s.split('.')
  return { int: intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ','), dec: dec || '00' }
})

const monthTotal = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth()
  let sum = 0
  for (const r of flatRecords.value) {
    if (r.status !== 1) continue
    const d = new Date(r.createTime)
    if (d.getFullYear() === y && d.getMonth() === m) {
      sum += Number(r.rechargeAmount || 0)
    }
  }
  return formatMoneyComma(sum)
})

function recordCategory(r: RechargeRecord): 'recharge' | 'reward' | 'event' {
  const rm = (r.remark || '').toLowerCase()
  const zh = r.remark || ''
  if (/活动|赠送|福利/.test(zh)) return 'event'
  if (/奖励|升级|赠送|bonus|gift/i.test(zh) || r.isLevelUpgraded === 1 || r.isUpgraded === 1) return 'reward'
  return 'recharge'
}

function mapRow(r: RechargeRecord): RowVm {
  const cat = recordCategory(r)
  const amt = Number(r.rechargeAmount || 0)
  const d = new Date(r.createTime)
  const timeLine = `${pad2(d.getMonth() + 1)}-${pad2(d.getDate())} ${pad2(d.getHours())}:${pad2(d.getMinutes())}`

  let title = '用户充值'
  if (cat === 'reward') title = '系统奖励'
  if (cat === 'event') title = '活动赠送'

  const st = r.status ?? 1
  let statusText = '成功'
  let badgeClass = 'badge-ok'
  if (st === 0) {
    statusText = '失败'
    badgeClass = 'badge-fail'
  } else if (st === 2) {
    statusText = '处理中'
    badgeClass = 'badge-pending'
  }

  const highlightAmount = cat === 'reward' || cat === 'event'
  const mutedAmount = st === 2 && !highlightAmount

  let icon = 'wallet'
  let iconBg = 'bg-wallet'
  let iconColor = '#1a1c1c'
  if (cat === 'reward') {
    icon = 'gift'
    iconBg = 'bg-bonus'
    iconColor = '#ff6600'
  } else if (cat === 'event') {
    icon = 'star-filled'
    iconBg = 'bg-bonus'
    iconColor = '#ff6600'
  }

  return {
    id: r.id,
    title,
    timeLine,
    amountStr: formatMoney(amt),
    highlightAmount,
    mutedAmount,
    status: st,
    statusText,
    badgeClass,
    icon,
    iconBg,
    iconColor
  }
}

const filteredRecords = computed(() => {
  if (filterKind.value === 'all') return flatRecords.value
  return flatRecords.value.filter((r) => {
    const c = recordCategory(r)
    if (filterKind.value === 'recharge') return c === 'recharge'
    return c === 'reward' || c === 'event'
  })
})

const groupedMonths = computed(() => {
  const list = filteredRecords.value
    .slice()
    .sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
  const map = new Map<string, { key: string; label: string; items: RowVm[] }>()
  for (const r of list) {
    const d = new Date(r.createTime)
    const key = `${d.getFullYear()}-${pad2(d.getMonth() + 1)}`
    const label = `${d.getFullYear()}年 ${d.getMonth() + 1}月`
    if (!map.has(key)) {
      map.set(key, { key, label, items: [] })
    }
    map.get(key)!.items.push(mapRow(r))
  }
  const keys = Array.from(map.keys()).sort((a, b) => (a > b ? -1 : 1))
  return keys.map((k) => map.get(k)!)
})

async function loadBalance() {
  try {
    const m = await fetchCurrentMember(true)
    balance.value = Number(m.balance || 0)
  } catch {
    balance.value = 0
  }
}

async function loadRecords(reset: boolean) {
  if (loading.value) return
  if (reset) {
    currentPage.value = 1
    flatRecords.value = []
    hasMore.value = true
  }
  if (!hasMore.value && !reset) return

  loading.value = true
  try {
    const result = await getRechargeRecords({
      page: currentPage.value,
      size: pageSize
    })
    const chunk = result.data || []
    if (reset) {
      flatRecords.value = chunk
    } else {
      flatRecords.value = [...flatRecords.value, ...chunk]
    }
    const total = result.total ?? 0
    hasMore.value = flatRecords.value.length < total
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (!hasMore.value || loading.value) return
  currentPage.value += 1
  void loadRecords(false)
}

function handleBack() {
  safeNavigateBack()
}

function goRecharge() {
  uni.navigateTo({ url: '/pages/recharge/index' })
}

function openFilter() {
  uni.showActionSheet({
    itemList: ['全部记录', '仅用户充值', '奖励与活动'],
    success: (res) => {
      if (res.tapIndex === 0) filterKind.value = 'all'
      else if (res.tapIndex === 1) filterKind.value = 'recharge'
      else filterKind.value = 'bonus'
    }
  })
}

function onRowTap(_row: RowVm) {
  // 预留：跳转详情
}

onMounted(async () => {
  try {
    const sys = getSafeSystemInfo()
    statusBarHeight.value = sys.statusBarHeight || 44
  } catch {
    statusBarHeight.value = 44
  }

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  await loadBalance()
  await loadRecords(true)
})

onPullDownRefresh(() => {
  Promise.all([loadBalance(), loadRecords(true)]).finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f9f9f9;
  color: #1a1c1c;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background-color: rgba(249, 249, 249, 0.88);
  backdrop-filter: blur(24px);
}

.header-inner {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 48rpx 20rpx;
}

.icon-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 9999rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.02);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  letter-spacing: -0.5rpx;
  color: #1a1c1c;
}

.main {
  box-sizing: border-box;
  height: 100vh;
}

.pad {
  padding: 0 48rpx 48rpx;
}

.hero {
  position: relative;
  border-radius: 32rpx;
  overflow: hidden;
  margin-bottom: 48rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 40rpx 80rpx rgba(163, 62, 0, 0.15);
}

.hero-glow {
  position: absolute;
  border-radius: 9999rpx;
  pointer-events: none;
}

.hero-glow-r {
  right: -64rpx;
  top: -64rpx;
  width: 256rpx;
  height: 256rpx;
  background: rgba(255, 255, 255, 0.1);
  filter: blur(40rpx);
}

.hero-glow-l {
  left: -32rpx;
  bottom: -32rpx;
  width: 192rpx;
  height: 192rpx;
  background: rgba(0, 0, 0, 0.05);
  filter: blur(32rpx);
}

.hero-inner {
  position: relative;
  z-index: 2;
  padding: 40rpx;
}

.hero-cap {
  font-size: 20rpx;
  font-weight: 700;
  color: #ffdbcd;
  letter-spacing: 3rpx;
  text-transform: uppercase;
  opacity: 0.95;
}

.hero-money {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  margin-top: 12rpx;
  gap: 4rpx;
}

.hero-yen {
  font-size: 48rpx;
  font-weight: 700;
  color: #ffffff;
}

.hero-int {
  font-size: 80rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -2rpx;
}

.hero-dec {
  font-size: 40rpx;
  font-weight: 700;
  color: #ffffff;
  opacity: 0.75;
}

.hero-foot {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-top: 40rpx;
  padding-top: 28rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.12);
}

.hero-subcap {
  display: block;
  font-size: 20rpx;
  color: #ffdbcd;
  opacity: 0.85;
}

.hero-subval {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #ffffff;
}

.hero-cta {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 28rpx;
  border-radius: 9999rpx;
  background-color: rgba(255, 255, 255, 0.22);
  font-size: 22rpx;
  font-weight: 700;
  color: #ffffff;
}

.state {
  padding: 120rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #9ca3af;
}

.month-block {
  margin-bottom: 48rpx;
}

.month-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #5f5e5e;
  margin-bottom: 24rpx;
  padding: 0 4rpx;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  background-color: #ffffff;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.02);
}

.row.dim {
  opacity: 0.85;
}

.row-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 32rpx;
  flex: 1;
  min-width: 0;
}

.ico {
  width: 96rpx;
  height: 96rpx;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bg-wallet {
  background-color: #f3f3f3;
}

.bg-bonus {
  background-color: rgba(255, 181, 150, 0.35);
}

.row-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.row-time {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #5f5e5e;
  letter-spacing: 0.5rpx;
}

.row-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
}

.row-amt {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.row-amt.bonus {
  color: #ff6600;
}

.row-amt.muted {
  color: #5f5e5e;
}

.badge {
  margin-top: 8rpx;
  padding: 4rpx 16rpx;
  border-radius: 9999rpx;
  font-size: 18rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
  text-transform: uppercase;
}

.badge-ok {
  background-color: #eeeeee;
  color: #5f5e5e;
}

.badge-pending {
  background-color: #d0e4ff;
  color: #003155;
}

.badge-fail {
  background-color: #ffdad6;
  color: #93000a;
}

.end-hint {
  margin-top: 48rpx;
  text-align: center;
  font-size: 22rpx;
  font-weight: 700;
  color: #9ca3af;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.bottom-space {
  height: 48rpx;
}
</style>
